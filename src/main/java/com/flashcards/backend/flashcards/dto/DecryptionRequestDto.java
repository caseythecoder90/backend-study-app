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
@Schema(description = "Request object for decrypting encrypted values")
public class DecryptionRequestDto {

    @NotBlank(message = "Encrypted text value is required")
    @Size(max = 10000, message = "Encrypted text must not exceed 10000 characters")
    @Schema(
            description = "Encrypted text value to be decrypted",
            example = "ENC(fVJhR8L5dqp0TEZlOvVuQz9RzY8bfW8G)",
            required = true
    )
    private String encryptedText;
}