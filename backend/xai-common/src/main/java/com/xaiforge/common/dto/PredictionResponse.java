package com.xaiforge.common.dto;

import java.util.Map;

public record PredictionResponse(
    String prediction,
    Double confidence,
    Map<String, Double> featureImportance
) {}

