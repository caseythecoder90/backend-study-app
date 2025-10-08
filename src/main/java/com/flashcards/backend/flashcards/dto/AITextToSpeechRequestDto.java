package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AudioOutputType;
import com.flashcards.backend.flashcards.enums.TTSVoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to convert text to speech audio")
public class AITextToSpeechRequestDto {

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user requesting audio generation", example = "user123")
    private String userId;

    @NotBlank(message = "Text is required")
    @Size(max = 10000, message = "Text must not exceed 10000 characters")
    @Schema(description = "Text content to convert to speech", example = "This is the content I want to listen to")
    private String text;

    @NotNull(message = "Output type is required")
    @Schema(description = "Type of audio output - full recitation or summary", example = "RECITATION")
    private AudioOutputType outputType;

    @Schema(description = "Voice to use for speech synthesis", example = "ALLOY")
    @Builder.Default
    private TTSVoice voice = TTSVoice.ALLOY;

    @DecimalMin(value = "0.25", message = "Speed must be at least 0.25")
    @DecimalMax(value = "4.0", message = "Speed must not exceed 4.0")
    @Schema(description = "Speech speed (0.25 to 4.0, default 1.0)", example = "1.0")
    @Builder.Default
    private Double speed = 1.0;

    @Schema(description = "Target word count for summary (only used when outputType is SUMMARY)", example = "250")
    @Builder.Default
    private Integer summaryWordCount = 250;
}