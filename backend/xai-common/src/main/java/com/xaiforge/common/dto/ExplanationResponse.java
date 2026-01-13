package com.xaiforge.common.dto;

import java.util.List;
import java.util.Map;

public record ExplanationResponse(
    String summary,
    List<FeatureImpact> featureImpacts,
    Map<String, Object> metadata
) {
    public record FeatureImpact(
        String feature,
        Double impact,
        String direction,
        Double contribution
    ) {}
}

