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
public class LoginHistoryDTO {
    private Boolean success;
    private String deviceInfo;
    private String ipAddress;
    private String location;
    private LocalDateTime timestamp;
}
