package com.flashcards.backend.flashcards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.flashcards.backend.flashcards.model.Flashcard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /**
     * Card content containing an ordered list of content blocks.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Content for one side of a flashcard with ordered text, code, and image blocks")
    public static class CardContentDto {
        @Schema(description = "Ordered list of content blocks (text, code, images)",
                example = "[{\"type\": \"TEXT\", \"content\": \"What is a closure?\"}, {\"type\": \"CODE\", \"language\": \"javascript\", \"code\": \"function example() {}\"}]")
        @Valid
        @NotNull(message = "Content blocks are required")
        @Size(min = 1, max = 20, message = "Must have between 1 and 20 content blocks")
        private List<ContentBlockDto> blocks;
    }

    /**
     * Base interface for polymorphic content block DTOs.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TextBlockDto.class, name = "TEXT"),
        @JsonSubTypes.Type(value = CodeBlockDto.class, name = "CODE"),
        @JsonSubTypes.Type(value = ImageBlockDto.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = MermaidBlockDto.class, name = "MERMAID")
    })
    public interface ContentBlockDto {
        Flashcard.BlockType getType();
    }

    /**
     * Text content block DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Text content block")
    public static class TextBlockDto implements ContentBlockDto {
        @Schema(description = "Text content", example = "What is a JavaScript closure?")
        @NotBlank(message = "Text content is required")
        @Size(max = 5000, message = "Text content must not exceed 5000 characters")
        private String content;

        @Override
        public Flashcard.BlockType getType() {
            return Flashcard.BlockType.TEXT;
        }
    }

    /**
     * Code block DTO with syntax highlighting support.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Code block with syntax highlighting and metadata")
    public static class CodeBlockDto implements ContentBlockDto {
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

        @Override
        public Flashcard.BlockType getType() {
            return Flashcard.BlockType.CODE;
        }
    }

    /**
     * Image block DTO for visual content.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Image block with display options")
    public static class ImageBlockDto implements ContentBlockDto {
        @Schema(description = "Image URL (can be GridFS path like /api/images/{id} or external URL)", example = "/api/images/507f1f77bcf86cd799439011")
        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String url;

        @Schema(description = "Optional caption displayed below image", example = "UML diagram showing interface inheritance")
        @Size(max = 200, message = "Caption must not exceed 200 characters")
        private String caption;

        @Schema(description = "Display size of the image", example = "MEDIUM")
        private Flashcard.ImageSize size;

        @Schema(description = "Alignment of the image", example = "CENTER")
        private Flashcard.ImageAlignment alignment;

        @Schema(description = "Alt text for accessibility", example = "Interface inheritance diagram")
        @Size(max = 200, message = "Alt text must not exceed 200 characters")
        private String altText;

        @Override
        public Flashcard.BlockType getType() {
            return Flashcard.BlockType.IMAGE;
        }
    }

    /**
     * Mermaid diagram block DTO (placeholder for future feature).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Mermaid diagram block (not yet implemented)")
    public static class MermaidBlockDto implements ContentBlockDto {
        @Schema(description = "Mermaid diagram syntax", example = "graph TD; A-->B;")
        @Size(max = 5000, message = "Diagram must not exceed 5000 characters")
        private String diagram;

        @Schema(description = "Optional caption", example = "Flowchart showing process")
        @Size(max = 200, message = "Caption must not exceed 200 characters")
        private String caption;

        @Override
        public Flashcard.BlockType getType() {
            return Flashcard.BlockType.MERMAID;
        }
    }
}