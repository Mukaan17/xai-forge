package com.example.xaiapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWebhookRequest {

    @NotBlank(message = "Webhook name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must be at most 500 characters")
    private String url;

    @NotEmpty(message = "At least one event must be selected")
    private Set<String> events;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}
