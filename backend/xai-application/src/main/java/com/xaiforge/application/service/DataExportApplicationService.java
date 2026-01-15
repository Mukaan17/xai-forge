package com.xaiforge.application.service;

import com.xaiforge.common.dto.ExportJobDto;
import com.xaiforge.common.dto.ExportRequest;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.export.entity.ExportJob;
import com.xaiforge.domain.model.entity.MLModel;
import com.xaiforge.domain.prediction.entity.PredictionRecord;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.domain.user.entity.UserPreferences;
import com.xaiforge.infrastructure.persistence.activity.ActivityLogRepository;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.export.ExportJobRepository;
import com.xaiforge.infrastructure.persistence.model.MLModelRepository;
import com.xaiforge.infrastructure.persistence.prediction.PredictionRecordRepository;
import com.xaiforge.infrastructure.persistence.user.UserPreferencesRepository;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for GDPR-compliant data export
 * 
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataExportApplicationService {
    
    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRecordRepository predictionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    
    @Value("${app.export.temp-directory:exports}")
    private String exportTempDirectory;
    
    @Value("${app.export.retention-days:7}")
    private int exportRetentionDays;
    
    /**
     * Request a full data export
     */
    public ExportJobDto requestExport(Long userId, ExportRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Check for pending exports
        List<ExportJob> pendingJobs = exportJobRepository.findByUserIdAndStatus(
            userId, ExportJob.ExportStatus.PENDING);
        if (!pendingJobs.isEmpty()) {
            throw new RuntimeException("An export is already in progress");
        }
        
        List<ExportJob> processingJobs = exportJobRepository.findByUserIdAndStatus(
            userId, ExportJob.ExportStatus.PROCESSING);
        if (!processingJobs.isEmpty()) {
            throw new RuntimeException("An export is already in progress");
        }
        
        ExportJob job = new ExportJob();
        job.setUser(user);
        job.setStatus(ExportJob.ExportStatus.PENDING);
        job.setProgress(0);
        job.setCurrentStep("Initializing export...");
        
        job = exportJobRepository.save(job);
        
        // Start async processing
        processExportAsync(job.getId(), request.includeItems());
        
        log.info("Export job created: userId={}, jobId={}", userId, job.getId());
        return toDto(job);
    }
    
    /**
     * Get export job status
     */
    @Transactional(readOnly = true)
    public ExportJobDto getExportStatus(Long userId, Long jobId) {
        ExportJob job = exportJobRepository.findByIdAndUserId(jobId, userId)
            .orElseThrow(() -> new RuntimeException("Export job not found: " + jobId));
        
        // Check if expired
        if (job.isExpired() && job.getStatus() == ExportJob.ExportStatus.COMPLETED) {
            job.setStatus(ExportJob.ExportStatus.EXPIRED);
            exportJobRepository.save(job);
        }
        
        return toDto(job);
    }
    
    /**
     * Get all export jobs for user
     */
    @Transactional(readOnly = true)
    public List<ExportJobDto> getUserExports(Long userId) {
        return exportJobRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }
    
    /**
     * Download completed export
     */
    @Transactional(readOnly = true)
    public Resource downloadExport(Long userId, Long jobId) {
        ExportJob job = exportJobRepository.findByIdAndUserId(jobId, userId)
            .orElseThrow(() -> new RuntimeException("Export job not found: " + jobId));
        
        if (job.getStatus() != ExportJob.ExportStatus.COMPLETED) {
            throw new RuntimeException("Export job is not completed");
        }
        
        if (job.isExpired()) {
            throw new RuntimeException("Export has expired");
        }
        
        if (job.getFilePath() == null) {
            throw new RuntimeException("Export file not found");
        }
        
        Path filePath = Paths.get(job.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Export file not found on disk");
        }
        
        return new FileSystemResource(filePath);
    }
    
    /**
     * Process export asynchronously
     */
    @Async
    @Transactional
    public void processExportAsync(Long jobId, Set<String> includeItems) {
        ExportJob job = exportJobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Export job not found: " + jobId));
        
        try {
            job.startProcessing();
            exportJobRepository.save(job);
            
            // Create export directory
            Path exportDir = Paths.get(exportTempDirectory, job.getUser().getId().toString());
            Files.createDirectories(exportDir);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path zipPath = exportDir.resolve("xai_export_" + timestamp + ".zip");
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                int totalSteps = includeItems.size();
                int currentStep = 0;
                
                for (String item : includeItems) {
                    currentStep++;
                    int progress = (currentStep * 100) / totalSteps;
                    job.updateProgress(progress, "Exporting " + item + "...");
                    exportJobRepository.save(job);
                    
                    switch (item.toLowerCase()) {
                        case "datasets" -> exportDatasets(zos, job.getUser().getId());
                        case "models" -> exportModels(zos, job.getUser().getId());
                        case "predictions" -> exportPredictions(zos, job.getUser().getId());
                        case "activity" -> exportActivityLogs(zos, job.getUser().getId());
                        case "profile" -> exportProfile(zos, job.getUser());
                        case "preferences" -> exportPreferences(zos, job.getUser().getId());
                    }
                }
                
                // Add metadata file
                addMetadataFile(zos, job, includeItems);
            }
            
            long fileSize = Files.size(zipPath);
            job.complete(zipPath.toString(), fileSize);
            exportJobRepository.save(job);
            
            log.info("Export completed: jobId={}, size={}", jobId, fileSize);
            
        } catch (Exception e) {
            log.error("Export failed: jobId={}, error={}", jobId, e.getMessage(), e);
            job.fail(e.getMessage());
            exportJobRepository.save(job);
        }
    }
    
    private void exportDatasets(ZipOutputStream zos, Long userId) throws IOException {
        List<Dataset> datasets = datasetRepository.findByOwnerId(userId);
        
        ZipEntry entry = new ZipEntry("datasets/datasets.json");
        zos.putNextEntry(entry);
        
        List<Map<String, Object>> datasetData = datasets.stream().map(dataset -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", dataset.getId());
            data.put("fileName", dataset.getFileName());
            data.put("headers", dataset.getHeaders());
            data.put("rowCount", dataset.getRowCount());
            data.put("uploadDate", dataset.getUploadDate().toString());
            return data;
        }).toList();
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, datasetData);
        zos.closeEntry();
    }
    
    private void exportModels(ZipOutputStream zos, Long userId) throws IOException {
        List<MLModel> models = modelRepository.findByDatasetOwnerId(userId);
        
        ZipEntry entry = new ZipEntry("models/models.json");
        zos.putNextEntry(entry);
        
        List<Map<String, Object>> modelData = models.stream().map(model -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", model.getId());
            data.put("modelName", model.getModelName());
            data.put("modelType", model.getModelType().name());
            data.put("targetVariable", model.getTargetVariable());
            data.put("featureNames", model.getFeatureNames());
            data.put("accuracy", model.getAccuracy());
            data.put("precision", model.getPrecision());
            data.put("recall", model.getRecall());
            data.put("f1Score", model.getF1Score());
            data.put("mse", model.getMse());
            data.put("rmse", model.getRmse());
            data.put("mae", model.getMae());
            data.put("r2Score", model.getR2Score());
            data.put("trainingDate", model.getTrainingDate().toString());
            data.put("status", model.getStatus().name());
            return data;
        }).toList();
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, modelData);
        zos.closeEntry();
    }
    
    private void exportPredictions(ZipOutputStream zos, Long userId) throws IOException {
        // Get all predictions (up to 10000)
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 10000);
        List<PredictionRecord> predictions = predictionRepository
            .findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .getContent();
        
        ZipEntry entry = new ZipEntry("predictions/predictions.json");
        zos.putNextEntry(entry);
        
        List<Map<String, Object>> predictionData = predictions.stream().map(pred -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", pred.getId());
            data.put("modelId", pred.getModel().getId());
            data.put("inputData", parseJson(pred.getInputData()));
            data.put("prediction", pred.getPrediction());
            data.put("confidence", pred.getConfidence());
            data.put("explanation", parseJson(pred.getExplanation()));
            data.put("createdAt", pred.getCreatedAt().toString());
            return data;
        }).toList();
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, predictionData);
        zos.closeEntry();
    }
    
    private void exportActivityLogs(ZipOutputStream zos, Long userId) throws IOException {
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 10000);
        var activities = activityLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        
        ZipEntry entry = new ZipEntry("activity/activity_logs.json");
        zos.putNextEntry(entry);
        
        List<Map<String, Object>> activityData = activities.getContent().stream().map(activity -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", activity.getId());
            data.put("eventType", activity.getEventType().name());
            data.put("details", activity.getDetails());
            data.put("timestamp", activity.getTimestamp().toString());
            return data;
        }).toList();
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, activityData);
        zos.closeEntry();
    }
    
    private void exportProfile(ZipOutputStream zos, User user) throws IOException {
        ZipEntry entry = new ZipEntry("profile/profile.json");
        zos.putNextEntry(entry);
        
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", user.getId());
        profileData.put("username", user.getUsername());
        profileData.put("email", user.getEmail());
        profileData.put("emailVerified", user.isEmailVerified());
        profileData.put("twoFactorEnabled", user.isTwoFactorEnabled());
        profileData.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        
        if (user.getProfile() != null) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("firstName", user.getProfile().getFirstName());
            profile.put("lastName", user.getProfile().getLastName());
            profile.put("organization", user.getProfile().getOrganization());
            profile.put("role", user.getProfile().getRole());
            profileData.put("profile", profile);
        }
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, profileData);
        zos.closeEntry();
    }
    
    private void exportPreferences(ZipOutputStream zos, Long userId) throws IOException {
        Optional<UserPreferences> prefsOpt = preferencesRepository.findByUserId(userId);
        
        ZipEntry entry = new ZipEntry("preferences/preferences.json");
        zos.putNextEntry(entry);
        
        Map<String, Object> prefsData = new HashMap<>();
        if (prefsOpt.isPresent()) {
            UserPreferences prefs = prefsOpt.get();
            prefsData.put("theme", prefs.getTheme());
            prefsData.put("accentColor", prefs.getAccentColor());
            prefsData.put("notificationPreferences", prefs.getNotificationPreferences());
        }
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, prefsData);
        zos.closeEntry();
    }
    
    private void addMetadataFile(ZipOutputStream zos, ExportJob job, Set<String> includeItems) throws IOException {
        ZipEntry entry = new ZipEntry("metadata.json");
        zos.putNextEntry(entry);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportDate", LocalDateTime.now().toString());
        metadata.put("userId", job.getUser().getId());
        metadata.put("username", job.getUser().getUsername());
        metadata.put("email", job.getUser().getEmail());
        metadata.put("includeItems", includeItems);
        metadata.put("format", "JSON");
        metadata.put("version", "1.0");
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, metadata);
        zos.closeEntry();
    }
    
    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json; // Return as string if parsing fails
        }
    }
    
    private ExportJobDto toDto(ExportJob job) {
        return new ExportJobDto(
            job.getId(),
            job.getStatus().name(),
            job.getProgress(),
            job.getCurrentStep(),
            job.getErrorMessage(),
            job.getFileSize(),
            job.getCreatedAt(),
            job.getCompletedAt(),
            job.getExpiresAt()
        );
    }
}
