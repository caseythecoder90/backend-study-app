package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login request")
public class LoginDto {
    @Schema(description = "Username or email address", example = "johndoe123")
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @Schema(description = "User password", example = "SecurePass123")
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(description = "TOTP code for 2FA (required if user has 2FA enabled)", example = "123456")
    private String totpCode;
}