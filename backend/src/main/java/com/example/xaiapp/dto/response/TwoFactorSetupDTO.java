package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupDTO {
    private String secret;
    private String qrCodeDataUri;
    private List<String> backupCodes;
}
