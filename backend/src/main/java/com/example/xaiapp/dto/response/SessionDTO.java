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
public class SessionDTO {
    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private String location;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private Boolean isCurrentSession;
}
