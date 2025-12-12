package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickStatsDTO {
    private Long predictionsToday;
    private Long modelsInTraining;
    private Long storageUsedBytes;
}
