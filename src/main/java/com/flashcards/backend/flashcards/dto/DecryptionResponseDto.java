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
@Schema(description = "Response object containing decrypted plain text value")
public class DecryptionResponseDto {

    @Schema(
            description = "Decrypted plain text value",
            example = "mySecretPassword123"
    )
    private String plainText;
}