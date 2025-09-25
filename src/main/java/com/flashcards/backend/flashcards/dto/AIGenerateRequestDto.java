package com.flashcards.backend.flashcards.dto;

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
public class AIGenerateRequestDto {
    @NotBlank(message = "Deck ID is required")
    private String deckId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Text content is required for AI generation")
    @Size(min = 50, max = 100000, message = "Text must be between 50 and 10000 characters")
    private String text;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 30, message = "Maximum 20 flashcards can be generated at once")
    private int count;

    @Size(max = 20, message = "Difficulty must not exceed 20 characters")
    private String difficulty;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
}