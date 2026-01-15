package com.xaiforge.common.dto;

import java.util.Set;

public record ExportRequest(
    Set<String> includeItems // "datasets", "models", "predictions", "activity", "profile", "preferences"
) {
    public ExportRequest {
        if (includeItems == null || includeItems.isEmpty()) {
            includeItems = Set.of("datasets", "models", "predictions", "activity", "profile", "preferences");
        }
    }
}
