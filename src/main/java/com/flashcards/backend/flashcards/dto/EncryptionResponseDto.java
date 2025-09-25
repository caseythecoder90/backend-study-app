package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing encrypted text value")
public class EncryptionResponseDto {

    @Schema(
            description = "Encrypted text value suitable for storage in configuration files",
            example = "ENC(fVJhR8L5dqp0TEZlOvVuQz9RzY8bfW8G)"
    )
    private String encryptedText;

    @Schema(
            description = "Formatted value ready to be used in application properties",
            example = "ENC(fVJhR8L5dqp0TEZlOvVuQz9RzY8bfW8G)"
    )
    private String formattedValue;
}