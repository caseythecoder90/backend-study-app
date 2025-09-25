package com.flashcards.backend.flashcards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flashcards.backend.flashcards.model.Flashcard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flashcard information with content and study statistics")
public class FlashcardDto {
    @Schema(description = "Unique identifier of the flashcard", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "ID of the deck this flashcard belongs to", example = "507f1f77bcf86cd799439012")
    private String deckId;

    @Schema(description = "ID of the user who owns this flashcard", example = "507f1f77bcf86cd799439013")
    private String userId;

    @Schema(description = "Front content of the flashcard (question/prompt)")
    private CardContentDto front;

    @Schema(description = "Back content of the flashcard (answer/explanation)")
    private CardContentDto back;

    @Schema(description = "Optional hint to help with recall", example = "Think about variable scope")
    private String hint;

    @Schema(description = "Tags for categorizing and searching flashcards", example = "[\"javascript\", \"functions\", \"programming\"]")
    private List<String> tags;

    @Schema(description = "Current difficulty level based on study performance")
    private Flashcard.DifficultyLevel difficulty;

    @Schema(description = "Total number of times this flashcard has been studied", example = "15")
    private int timesStudied;

    @Schema(description = "Number of times answered correctly", example = "12")
    private int timesCorrect;

    @Schema(description = "Number of times answered incorrectly", example = "3")
    private int timesIncorrect;

    @Schema(description = "Timestamp when the flashcard was created", example = "2024-01-15T10:30:00.000Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the flashcard was last updated", example = "2024-01-15T10:30:00.000Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    @Schema(description = "Timestamp when the flashcard was last studied", example = "2024-01-15T10:30:00.000Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lastStudiedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Content for one side of a flashcard supporting text and code blocks")
    public static class CardContentDto {
        @Schema(description = "Main text content of the card side", example = "What is a JavaScript closure?")
        @NotBlank(message = "Card content text is required")
        @Size(max = 2000, message = "Card content must not exceed 2000 characters")
        private String text;

        @Schema(description = "Optional code blocks with syntax highlighting support")
        @Valid
        @Size(max = 5, message = "Maximum 5 code blocks allowed per card side")
        private List<CodeBlockDto> codeBlocks;

        @Schema(description = "Type of content for rendering optimization")
        private Flashcard.ContentType type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Code block with syntax highlighting and metadata")
    public static class CodeBlockDto {
        @Schema(description = "Programming language for syntax highlighting", example = "javascript")
        @Size(max = 50, message = "Programming language name must not exceed 50 characters")
        private String language;

        @Schema(description = "The actual code content", example = "function closure() { return function() { console.log('Hello'); }; }")
        @NotBlank(message = "Code content is required")
        @Size(max = 5000, message = "Code block must not exceed 5000 characters")
        private String code;

        @Schema(description = "Optional filename for context", example = "closure-example.js")
        @Size(max = 100, message = "File name must not exceed 100 characters")
        private String fileName;

        @Schema(description = "Whether this code block should be highlighted", example = "true")
        private boolean highlighted;

        @Schema(description = "Specific line numbers to highlight (1-based)", example = "[1, 3, 5]")
        @Size(max = 100, message = "Maximum 100 highlighted lines allowed")
        private List<@Min(value = 1, message = "Line numbers must be positive") Integer> highlightedLines;
    }
}