package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for generating flashcards from images using AI vision models")
public class AIImageGenerateRequestDto {

    @NotBlank(message = "Deck ID is required")
    @Schema(description = "ID of the deck to add flashcards to", example = "507f1f77bcf86cd799439011")
    private String deckId;

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user making the request", example = "507f191e810c19729de860ea")
    private String userId;

    @Schema(description = "Optional prompt to guide AI in generating flashcards from the image",
            example = "Generate flashcards focusing on the Java code patterns shown in this screenshot")
    @Size(max = 1000, message = "Prompt must not exceed 1000 characters")
    private String prompt;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Maximum 20 flashcards can be generated at once")
    @Schema(description = "Number of flashcards to generate", example = "5", defaultValue = "5")
    private int count = 5;

    @Schema(description = "Difficulty level for generated flashcards", example = "INTERMEDIATE")
    @Size(max = 20, message = "Difficulty must not exceed 20 characters")
    private String difficulty;

    @Schema(description = "Category for the flashcards", example = "Java Programming")
    @Size(max = 500, message = "Category must not exceed 500 characters")
    private String category;

    @Schema(description = "AI model to use for generation (must support vision). Use enum constant name.",
            type = "string",
            example = "GPT_4O",
            allowableValues = {"GPT_4O", "GPT_4_TURBO", "GEMINI_2_0_FLASH_EXP", "GEMINI_2_5_FLASH"},
            defaultValue = "GPT_4O")
    private AIModelEnum model;

    @NotNull(message = "Image file is required")
    @Schema(description = "Image file to analyze and generate flashcards from",
            type = "string",
            format = "binary",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile image;
}