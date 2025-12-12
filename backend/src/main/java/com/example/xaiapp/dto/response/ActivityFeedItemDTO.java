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
public class ActivityFeedItemDTO {
    private Long id;
    private String type;
    private String icon;
    private String title;
    private String subtitle;
    private LocalDateTime timestamp;
    private String actionUrl;
}
