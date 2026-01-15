package com.xaiforge.common.dto;

import java.util.List;

public record PaginatedResponse<T>(
    List<T> content,
    int currentPage,
    long totalItems,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static <T> PaginatedResponse<T> of(
            List<T> content,
            int currentPage,
            int pageSize,
            long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PaginatedResponse<>(
            content,
            currentPage,
            totalItems,
            totalPages,
            currentPage < totalPages - 1,
            currentPage > 0
        );
    }
}
