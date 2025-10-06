package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for generating summaries using AI")
public class AISummaryRequestDto {

    @Schema(description = "Type of summary source", example = "DECK")
    private SummarySourceType sourceType;

    @Schema(description = "ID of the deck to summarize (if sourceType is DECK)")
    private String deckId;

    @Schema(description = "List of flashcard IDs to summarize (if sourceType is FLASHCARDS)")
    private List<String> flashcardIds;

    @Schema(description = "Text content to summarize (if sourceType is TEXT)")
    @Size(max = 100000, message = "Text must not exceed 100000 characters")
    private String text;

    @Schema(description = "Prompt to provide additional context or specific requirements for the summary",
            example = "Create a concise summary focusing on the key programming concepts")
    @Size(max = 1000, message = "Prompt must not exceed 1000 characters")
    private String prompt;

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user making the request", example = "507f191e810c19729de860ea")
    private String userId;

    @Schema(description = "Format of the summary", example = "BULLET_POINTS", defaultValue = "PARAGRAPH")
    private SummaryFormat format = SummaryFormat.PARAGRAPH;

    @Schema(description = "Length of the summary", example = "MEDIUM", defaultValue = "MEDIUM")
    private SummaryLength length = SummaryLength.MEDIUM;

    @Schema(description = "AI model to use for generation. Use enum constant name.",
            type = "string",
            example = "GPT_4O_MINI",
            allowableValues = {"GPT_4O_MINI", "GPT_4O", "CLAUDE_3_5_SONNET", "CLAUDE_SONNET_4", "GEMINI_2_0_FLASH_EXP", "GEMINI_2_5_FLASH"},
            defaultValue = "GPT_4O_MINI")
    private AIModelEnum model;

    public enum SummarySourceType {
        DECK,
        FLASHCARDS,
        TEXT,
        PROMPT
    }

    public enum SummaryFormat {
        PARAGRAPH,
        BULLET_POINTS,
        NUMBERED_LIST,
        OUTLINE,
        MARKDOWN
    }

    public enum SummaryLength {
        SHORT,   // ~100 words
        MEDIUM,  // ~250 words
        LONG,    // ~500 words
        DETAILED // ~1000 words
    }
}