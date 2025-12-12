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
public class RecentModelDTO {
    private Long id;
    private String name;
    private String type;
    private String algorithm;
    private Double accuracy;
    private String status;
    private LocalDateTime createdAt;
    private String datasetName;
    private Long predictionCount;
}
