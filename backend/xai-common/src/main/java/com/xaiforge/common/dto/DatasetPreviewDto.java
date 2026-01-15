package com.xaiforge.common.dto;

import java.util.List;
import java.util.Map;

public record DatasetPreviewDto(
    List<Map<String, String>> rows,
    int totalRows,
    int offset,
    int limit,
    boolean hasMore
) {}
