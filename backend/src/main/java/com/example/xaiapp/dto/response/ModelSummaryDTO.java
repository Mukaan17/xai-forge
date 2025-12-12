package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelSummaryDTO {
    private Long id;
    private String name;
    private String version;
    private String algorithm;
    private Double accuracy;
    private LocalDateTime trainedAt;
    private Integer featureCount;
}
