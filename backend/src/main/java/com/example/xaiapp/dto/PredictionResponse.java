package com.example.xaiapp.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    
    private String prediction;
    private Double confidence;
    private Map<String, Object> probabilities;
    private Map<String, String> inputData;
}
