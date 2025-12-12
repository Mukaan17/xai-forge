package com.example.xaiapp.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteRequest {

    @NotEmpty(message = "At least one ID must be provided")
    private List<Long> ids;
}
