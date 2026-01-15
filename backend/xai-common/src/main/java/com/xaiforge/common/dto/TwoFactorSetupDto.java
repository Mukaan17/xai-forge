package com.xaiforge.common.dto;

import java.util.List;

/**
 * DTO for 2FA setup response containing secret, QR code, and backup codes.
 */
public record TwoFactorSetupDto(
    String secret,
    String qrCodeDataUri,
    List<String> backupCodes
) {}
