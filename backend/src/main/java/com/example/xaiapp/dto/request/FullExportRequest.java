package com.example.xaiapp.dto.request;

import com.example.xaiapp.entity.ExportJob.ExportFormat;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullExportRequest {

    @NotEmpty(message = "At least one item must be selected for export")
    private Set<String> includeItems; // datasets, models, predictions, activity

    private ExportFormat format; // ZIP, JSON, CSV
}
