package com.flashcards.backend.flashcards.model;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardContent {
        private String text;
        private List<CodeBlock> codeBlocks;
        private ContentType type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeBlock {
        private String language;
        private String code;
        private String fileName;
        private boolean highlighted;
        private List<Integer> highlightedLines;
    }

    public enum ContentType {
        TEXT_ONLY,
        CODE_ONLY,
        MIXED
    }

    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD,
        NOT_SET
    }
}