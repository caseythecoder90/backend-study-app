package com.flashcards.backend.flashcards.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flashcards")
public class Flashcard {
    @Id
    private String id;

    private String deckId;
    private String userId;

    private CardContent front;
    private CardContent back;

    private String hint;
    private List<String> tags;

    private DifficultyLevel difficulty;
    private int timesStudied;
    private int timesCorrect;
    private int timesIncorrect;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastStudiedAt;

    /**
     * Card content containing an ordered list of content blocks.
     * Blocks can be text, code, images, or diagrams in any sequence.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardContent {
        private List<ContentBlock> blocks;
    }

    /**
     * Base interface for polymorphic content blocks.
     * Uses Jackson annotations for proper JSON serialization/deserialization.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TextBlock.class, name = "TEXT"),
        @JsonSubTypes.Type(value = CodeBlock.class, name = "CODE"),
        @JsonSubTypes.Type(value = ImageBlock.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = MermaidBlock.class, name = "MERMAID")
    })
    public interface ContentBlock {
        BlockType getType();
    }

    /**
     * Text content block for regular text content.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextBlock implements ContentBlock {
        private String content;

        @Override
        public BlockType getType() {
            return BlockType.TEXT;
        }
    }

    /**
     * Code block with syntax highlighting support.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeBlock implements ContentBlock {
        private String language;
        private String code;
        private String fileName;
        private boolean highlighted;
        private List<Integer> highlightedLines;

        @Override
        public BlockType getType() {
            return BlockType.CODE;
        }
    }

    /**
     * Image block for visual content.
     * Supports both uploaded images (GridFS) and external URLs.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageBlock implements ContentBlock {
        private String url;
        private String caption;
        private ImageSize size;
        private ImageAlignment alignment;
        private String altText;

        @Override
        public BlockType getType() {
            return BlockType.IMAGE;
        }
    }

    /**
     * Mermaid diagram block for future diagram support.
     * Placeholder for MVP - not yet implemented.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MermaidBlock implements ContentBlock {
        private String diagram;
        private String caption;

        @Override
        public BlockType getType() {
            return BlockType.MERMAID;
        }
    }

    /**
     * Types of content blocks that can be included in flashcards.
     */
    public enum BlockType {
        TEXT,
        CODE,
        IMAGE,
        MERMAID
    }

    /**
     * Image size options for controlling display dimensions.
     */
    public enum ImageSize {
        SMALL,
        MEDIUM,
        LARGE,
        FULL_WIDTH
    }

    /**
     * Image alignment options for positioning.
     */
    public enum ImageAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Difficulty level based on study performance.
     */
    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD,
        NOT_SET
    }
}