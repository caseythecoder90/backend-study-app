package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request DTO for generating flashcards from a prompt using AI")
public class AIPromptGenerateRequestDto {

    @NotBlank(message = "Deck ID is required")
    @Schema(description = "ID of the deck to add flashcards to", example = "507f1f77bcf86cd799439011")
    private String deckId;

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user making the request", example = "507f191e810c19729de860ea")
    private String userId;

    @NotBlank(message = "Prompt is required for AI generation")
    @Size(min = 10, max = 2000, message = "Prompt must be between 10 and 2000 characters")
    @Schema(description = "Prompt describing what flashcards to generate",
            example = "Create flashcards about the key concepts of object-oriented programming in Java, including inheritance, polymorphism, encapsulation, and abstraction")
    private String prompt;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 30, message = "Maximum 30 flashcards can be generated at once")
    @Schema(description = "Number of flashcards to generate", example = "10", defaultValue = "5")
    private int count = 5;

    @Schema(description = "Difficulty level for generated flashcards", example = "INTERMEDIATE")
    @Size(max = 20, message = "Difficulty must not exceed 20 characters")
    private String difficulty;

    @Schema(description = "Category for the flashcards", example = "Java OOP")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Schema(description = "AI model to use for generation. Use enum constant name.",
            type = "string",
            example = "GPT_4O_MINI",
            allowableValues = {"GPT_4O_MINI", "GPT_4O", "CLAUDE_3_5_SONNET", "CLAUDE_SONNET_4", "GEMINI_2_0_FLASH_EXP", "GEMINI_2_5_FLASH"},
            defaultValue = "GPT_4O_MINI")
    private AIModelEnum model;

    @Schema(description = "Topic or subject area for more focused generation",
            example = "Computer Science")
    @Size(max = 100, message = "Topic must not exceed 100 characters")
    private String topic;
}