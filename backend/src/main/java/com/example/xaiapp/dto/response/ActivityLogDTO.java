package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private Long id;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String resourceName;
    private String description;
    private Boolean success;
    private String ipAddress;
    private String location;
    private LocalDateTime createdAt;
    private Map<String, Object> metadata;
}
