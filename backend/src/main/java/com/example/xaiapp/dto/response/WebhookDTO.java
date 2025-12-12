package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDTO {
    private Long id;
    private String name;
    private String url;
    private Set<String> events;
    private String description;
    private Boolean active;
    private Boolean autoDisabled;
    private LocalDateTime lastTriggeredAt;
    private Integer lastResponseCode;
    private Long failureCount;
    private Long successCount;
    private LocalDateTime createdAt;
}
