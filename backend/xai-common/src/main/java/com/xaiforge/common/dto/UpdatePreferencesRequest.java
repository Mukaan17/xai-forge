package com.xaiforge.common.dto;

public record UpdatePreferencesRequest(
    String theme,
    String accentColor,
    String notificationPreferences
) {}
