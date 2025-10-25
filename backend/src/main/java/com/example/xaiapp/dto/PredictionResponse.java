/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:06:19
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:59
 */
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
