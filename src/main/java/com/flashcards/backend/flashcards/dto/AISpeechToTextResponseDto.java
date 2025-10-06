package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing transcribed text and optional processing results")
public class AISpeechToTextResponseDto {

    @Schema(description = "Transcribed text from the audio", example = "This is the transcribed content from the audio")
    private String transcribedText;

    @Schema(description = "Detected language of the audio", example = "en")
    private String detectedLanguage;

    @Schema(description = "Duration of the audio file in seconds", example = "45.5")
    private Double durationSeconds;

    @Schema(description = "Model used for transcription", example = "whisper-1")
    private String model;

    @Schema(description = "Generated summary (if action was SUMMARY)")
    private AISummaryResponseDto summary;

    @Schema(description = "Generated flashcards (if action was FLASHCARDS)")
    private List<CreateFlashcardDto> flashcards;

    @Schema(description = "Action performed on the transcribed text", example = "SUMMARY")
    private String actionPerformed;
}