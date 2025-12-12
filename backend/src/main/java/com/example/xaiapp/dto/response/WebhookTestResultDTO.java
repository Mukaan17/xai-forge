package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookTestResultDTO {
    private Boolean success;
    private Integer statusCode;
    private String responseBody;
    private Long responseTimeMs;
    private String errorMessage;
}
