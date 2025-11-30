package com.example.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for user registration")
public class RegisterRequest {

    @Schema(description = "User email address", example = "string", defaultValue = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "User password (minimum 8 characters)", example = "string", defaultValue = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}