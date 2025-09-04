package com.example.xaiapp.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainRequestDto {
    
    @NotNull(message = "Dataset ID is required")
    private Long datasetId;
    
    @NotBlank(message = "Model name is required")
    private String modelName;
    
    @NotBlank(message = "Target variable is required")
    private String targetVariable;
    
    @NotEmpty(message = "Feature names are required")
    private List<String> featureNames;
    
    @NotBlank(message = "Model type is required")
    private String modelType; // "CLASSIFICATION" or "REGRESSION"
}
