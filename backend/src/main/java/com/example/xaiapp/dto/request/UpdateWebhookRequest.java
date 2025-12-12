package com.example.xaiapp.dto.request;

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
public class UpdateWebhookRequest {

    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "URL must be at most 500 characters")
    private String url;

    private Set<String> events;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Boolean active;
}
