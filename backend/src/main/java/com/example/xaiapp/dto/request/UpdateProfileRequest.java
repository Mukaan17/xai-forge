package com.example.xaiapp.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 200, message = "Organization must be at most 200 characters")
    private String organization;

    @Size(max = 100, message = "Role must be at most 100 characters")
    private String role;

    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;
}
