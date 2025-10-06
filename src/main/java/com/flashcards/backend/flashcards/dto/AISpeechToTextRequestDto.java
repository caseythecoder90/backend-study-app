package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AudioOutputType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to convert speech to text and optionally process it")
public class AISpeechToTextRequestDto {

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user requesting transcription", example = "user123")
    private String userId;

    @NotNull(message = "Audio file is required")
    @Schema(description = "Audio file to transcribe", type = "string", format = "binary")
    private MultipartFile audioFile;

    @Schema(description = "Language of the audio (optional, auto-detected if not provided)", example = "en")
    private String language;

    @Schema(description = "Prompt to help guide the transcription", example = "This is about machine learning concepts")
    private String prompt;

    @Schema(description = "What to do with transcribed text - generate summary or flashcards", example = "SUMMARY")
    private String action;

    @Schema(description = "Deck ID if generating flashcards from the transcribed text")
    private String deckId;

    @Schema(description = "Number of flashcards to generate (if action is to generate flashcards)", example = "5")
    private Integer flashcardCount;
}