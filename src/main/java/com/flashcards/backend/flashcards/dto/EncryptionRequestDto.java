package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for encrypting plain text values")
public class EncryptionRequestDto {

    @NotBlank(message = "Plain text value is required")
    @Size(max = 5000, message = "Plain text must not exceed 5000 characters")
    @Schema(
            description = "Plain text value to be encrypted",
            example = "mySecretPassword123"
    )
    private String plainText;
}