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
@Document(collection = "study_sessions")
public class StudySession {
    @Id
    private String id;

    private String userId;
    private String deckId;

    private SessionType sessionType;
    private int totalCards;
    private int correctAnswers;
    private int incorrectAnswers;
    private int skippedCards;

    private long durationInSeconds;
    private double accuracyPercentage;

    private List<CardResult> cardResults;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardResult {
        private String flashcardId;
        private boolean correct;
        private long timeSpentSeconds;
        private int attemptNumber;
    }

    public enum SessionType {
        PRACTICE,
        TEST,
        REVIEW,
        LEARN
    }
}