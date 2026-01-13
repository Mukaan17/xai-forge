package com.xaiforge.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ModelDto(
    Long id,
    String modelName,
    String modelType,
    LocalDateTime trainingDate,
    String targetVariable,
    List<String> featureNames,
    Double accuracy,
    Double precision,
    Double recall,
    Double f1Score,
    Long trainingTime,
    String status,
    Long datasetId
) {}

