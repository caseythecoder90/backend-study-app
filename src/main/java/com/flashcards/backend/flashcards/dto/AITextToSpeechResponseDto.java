package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AudioOutputType;
import com.flashcards.backend.flashcards.enums.TTSVoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing generated audio data")
public class AITextToSpeechResponseDto {

    @Schema(description = "Base64 encoded audio data", example = "UklGRiQAAABXQVZFZm10...")
    private String audioData;

    @Schema(description = "Audio format", example = "mp3")
    private String format;

    @Schema(description = "Duration of audio in seconds", example = "45.5")
    private Double durationSeconds;

    @Schema(description = "Voice used for generation", example = "ALLOY")
    private TTSVoice voice;

    @Schema(description = "Type of output generated", example = "RECITATION")
    private AudioOutputType outputType;

    @Schema(description = "Text that was converted to speech (may be summarized)")
    private String processedText;

    @Schema(description = "Model used for TTS", example = "tts-1")
    private String model;
}