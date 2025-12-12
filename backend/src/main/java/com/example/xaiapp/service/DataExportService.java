package com.example.xaiapp.service;

import com.example.xaiapp.entity.*;
import com.example.xaiapp.entity.ExportJob.ExportFormat;
import com.example.xaiapp.entity.ExportJob.ExportStatus;
import com.example.xaiapp.entity.ExportJob.ExportType;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for exporting user data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRepository predictionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${app.export.temp-directory:/tmp/xai-exports}")
    private String exportTempDirectory;

    @Value("${app.export.retention-days:7}")
    private int exportRetentionDays;

    /**
     * Request a full data export.
     */
    @Transactional
    public Map<String, Object> requestFullExport(Long userId, Set<String> includeItems, ExportFormat format) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ExportJob> pendingJobs = exportJobRepository.findByUserIdAndStatus(userId, ExportStatus.PENDING);
        if (!pendingJobs.isEmpty()) {
            throw new ValidationException("An export is already in progress");
        }

        ExportJob job = ExportJob.builder()
            .user(user)
            .exportType(ExportType.FULL)
            .includeItems(includeItems)
            .format(format != null ? format : ExportFormat.ZIP)
            .status(ExportStatus.PENDING)
            .progress(0)
            .build();

        job = exportJobRepository.save(job);
        processExportAsync(job.getId());

        log.info("Export job created: userId={}, jobId={}", userId, job.getId());
        return mapToDTO(job);
    }

    /**
     * Get export job status.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getExportStatus(Long userId, Long jobId) {
        ExportJob job = exportJobRepository.findByIdAndUserId(jobId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Export job not found"));
        return mapToDTO(job);
    }

    /**
     * Download completed export file.
     */
    @Transactional
    public Resource downloadExport(Long userId, Long jobId) {
        ExportJob job = exportJobRepository.findByIdAndUserId(jobId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Export job not found"));

        if (job.getStatus() != ExportStatus.COMPLETED) {
            throw new ValidationException("Export is not ready for download");
        }

        if (job.getFilePath() == null || !Files.exists(Path.of(job.getFilePath()))) {
            throw new ResourceNotFoundException("Export file not found");
        }

        job.setDownloadCount(job.getDownloadCount() + 1);
        exportJobRepository.save(job);

        return new FileSystemResource(job.getFilePath());
    }

    /**
     * Process export asynchronously.
     */
    @Async
    @Transactional
    public void processExportAsync(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Export job not found: {}", jobId);
            return;
        }

        try {
            job.startProcessing();
            exportJobRepository.save(job);

            Path exportDir = Paths.get(exportTempDirectory, job.getUser().getId().toString());
            Files.createDirectories(exportDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path zipPath = exportDir.resolve("xai_export_" + timestamp + ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                int totalSteps = job.getIncludeItems().size();
                int currentStep = 0;

                for (String item : job.getIncludeItems()) {
                    currentStep++;
                    int progress = (currentStep * 100) / totalSteps;
                    job.updateProgress(progress, "Exporting " + item + "...");
                    exportJobRepository.save(job);

                    switch (item.toLowerCase()) {
                        case "datasets" -> exportDatasets(zos, job.getUser().getId());
                        case "models" -> exportModels(zos, job.getUser().getId());
                        case "predictions" -> exportPredictions(zos, job.getUser().getId());
                        case "activity" -> exportActivityLogs(zos, job.getUser().getId());
                    }
                }

                addMetadataFile(zos, job);
            }

            long fileSize = Files.size(zipPath);
            job.complete(zipPath.toString(), fileSize);
            exportJobRepository.save(job);

            notificationService.notifyExportReady(job.getUser().getId(), job.getId(), job.getExportType().name());
            log.info("Export completed: jobId={}, size={}", jobId, fileSize);

        } catch (Exception e) {
            log.error("Export failed: jobId={}", jobId, e);
            job.fail(e.getMessage());
            exportJobRepository.save(job);
        }
    }

    /**
     * Clean up expired export files.
     */
    @Transactional
    public int cleanupExpiredExports() {
        List<ExportJob> expired = exportJobRepository.findExpiredJobs(LocalDateTime.now());
        int cleaned = 0;

        for (ExportJob job : expired) {
            try {
                if (job.getFilePath() != null) {
                    Files.deleteIfExists(Path.of(job.getFilePath()));
                }
                job.setStatus(ExportStatus.EXPIRED);
                exportJobRepository.save(job);
                cleaned++;
            } catch (IOException e) {
                log.warn("Failed to delete expired export file: {}", job.getFilePath(), e);
            }
        }

        log.info("Cleaned up {} expired exports", cleaned);
        return cleaned;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void exportDatasets(ZipOutputStream zos, Long userId) throws IOException {
        List<Dataset> datasets = datasetRepository.findByUserIdAndDeletedFalse(userId);
        List<Map<String, Object>> datasetData = new ArrayList<>();
        for (Dataset d : datasets) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", d.getId());
            data.put("name", d.getName());
            data.put("description", d.getDescription());
            data.put("originalFilename", d.getOriginalFilename());
            data.put("rowCount", d.getRowCount());
            data.put("columnCount", d.getColumnCount());
            data.put("columnNames", d.getColumnNames());
            data.put("columnMetadata", d.getColumnMetadata());
            data.put("createdAt", d.getCreatedAt().toString());
            datasetData.add(data);
        }
        addJsonEntry(zos, "datasets/datasets.json", datasetData);

        for (Dataset d : datasets) {
            if (d.getFilePath() != null && Files.exists(Path.of(d.getFilePath()))) {
                addFileEntry(zos, "datasets/files/" + d.getOriginalFilename(), Path.of(d.getFilePath()));
            }
        }
    }

    private void exportModels(ZipOutputStream zos, Long userId) throws IOException {
        List<MLModel> models = modelRepository.findByUserId(userId);
        List<Map<String, Object>> modelData = new ArrayList<>();
        for (MLModel m : models) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", m.getId());
            data.put("name", m.getName());
            data.put("description", m.getDescription());
            data.put("modelType", m.getModelType().name());
            data.put("algorithm", m.getAlgorithm());
            data.put("targetColumn", m.getTargetColumn());
            data.put("featureColumns", m.getFeatureColumns());
            data.put("accuracy", m.getAccuracy());
            data.put("status", m.getStatus().name());
            data.put("createdAt", m.getCreatedAt().toString());
            data.put("trainedAt", m.getTrainedAt() != null ? m.getTrainedAt().toString() : null);
            modelData.add(data);
        }
        addJsonEntry(zos, "models/models.json", modelData);
    }

    private void exportPredictions(ZipOutputStream zos, Long userId) throws IOException {
        List<Prediction> predictions = predictionRepository
            .findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        List<Map<String, Object>> predictionData = new ArrayList<>();
        for (Prediction p : predictions) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", p.getId());
            data.put("modelId", p.getModel().getId());
            data.put("modelName", p.getModel().getName());
            data.put("inputData", p.getInputData());
            data.put("predictionResult", p.getPredictionResult());
            data.put("confidence", p.getConfidence());
            data.put("explanation", p.getExplanation());
            data.put("createdAt", p.getCreatedAt().toString());
            predictionData.add(data);
        }
        addJsonEntry(zos, "predictions/predictions.json", predictionData);
    }

    private void exportActivityLogs(ZipOutputStream zos, Long userId) throws IOException {
        List<ActivityLog> logs = activityLogRepository
            .findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        List<Map<String, Object>> logData = new ArrayList<>();
        for (ActivityLog l : logs) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", l.getId());
            data.put("action", l.getAction().name());
            data.put("resourceType", l.getResourceType());
            data.put("resourceId", l.getResourceId());
            data.put("description", l.getDescription());
            data.put("success", l.getSuccess());
            data.put("createdAt", l.getCreatedAt().toString());
            logData.add(data);
        }
        addJsonEntry(zos, "activity/activity_log.json", logData);
    }

    private void addMetadataFile(ZipOutputStream zos, ExportJob job) throws IOException {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("exportedAt", LocalDateTime.now().toString());
        metadata.put("exportType", job.getExportType().name());
        metadata.put("includedItems", job.getIncludeItems());
        metadata.put("userId", job.getUser().getId());
        metadata.put("userEmail", job.getUser().getEmail());
        metadata.put("xaiForgeVersion", "1.0.0");
        addJsonEntry(zos, "export_metadata.json", metadata);
    }

    private void addJsonEntry(ZipOutputStream zos, String entryName, Object data) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, data);
        zos.closeEntry();
    }

    private void addFileEntry(ZipOutputStream zos, String entryName, Path filePath) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    private Map<String, Object> mapToDTO(ExportJob job) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", job.getId());
        dto.put("status", job.getStatus().name());
        dto.put("exportType", job.getExportType().name());
        dto.put("format", job.getFormat().name());
        dto.put("includeItems", job.getIncludeItems());
        dto.put("progress", job.getProgress());
        dto.put("currentStep", job.getCurrentStep());
        dto.put("fileSizeBytes", job.getFileSizeBytes());
        dto.put("errorMessage", job.getErrorMessage());
        dto.put("createdAt", job.getCreatedAt());
        dto.put("startedAt", job.getStartedAt());
        dto.put("completedAt", job.getCompletedAt());
        dto.put("expiresAt", job.getExpiresAt());
        dto.put("downloadCount", job.getDownloadCount());
        return dto;
    }
}
