package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request using recovery code for TOTP bypass")
public class RecoveryCodeLoginDto {
    @NotBlank(message = "Username or email is required")
    @Size(min = 3, max = 100, message = "Username or email must be between 3 and 100 characters")
    @Schema(description = "Username or email address", example = "casquinn")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "User password", example = "SecurePassword123!")
    private String password;

    @NotBlank(message = "Recovery code is required")
    @Pattern(regexp = "^[A-Z0-9]{4}-?[A-Z0-9]{4}$",
            message = "Recovery code must be in format XXXX-XXXX (8 alphanumeric characters)")
    @Schema(description = "One-time recovery code (with or without dash)", example = "ABCD-1234")
    private String recoveryCode;
}