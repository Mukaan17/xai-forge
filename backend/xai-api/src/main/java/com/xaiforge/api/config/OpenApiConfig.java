package com.xaiforge.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration
 * 
 * Provides API documentation accessible at /swagger-ui.html
 * 
 * @since 1.0.0
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "XAI-Forge API",
        version = "1.0.0",
        description = """
            XAI-Forge is a comprehensive ML Operations Platform focused on Explainable AI (XAI).
            
            ## Features
            - **Dataset Management**: Upload, preview, and manage CSV/Excel datasets
            - **Model Training**: Train classification and regression models with multiple algorithms
            - **Predictions & Explanations**: Generate predictions with LIME-style explanations
            - **Dashboard & Analytics**: View statistics, charts, and activity logs
            - **User Management**: Profile, preferences, security settings, and session management
            
            ## Authentication
            Most endpoints require JWT authentication. Include the token in the Authorization header:
            ```
            Authorization: Bearer <your-jwt-token>
            ```
            
            ## Rate Limiting
            API requests are rate-limited to prevent abuse. Check response headers for rate limit information.
            
            ## Error Responses
            All errors follow RFC 7807 Problem Details format with consistent error codes.
            """,
        contact = @Contact(
            name = "XAI-Forge Support",
            email = "support@xaiforge.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://api.xaiforge.com",
            description = "Production Server"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT Authentication. Obtain a token by logging in at /api/v1/auth/login"
)
public class OpenApiConfig {
    // Configuration class - annotations provide the configuration
}
