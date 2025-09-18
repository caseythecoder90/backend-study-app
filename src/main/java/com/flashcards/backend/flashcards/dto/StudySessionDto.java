package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.model.StudySession;
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
public class StudySessionDto {
    private String id;
    private String userId;
    private String deckId;
    private StudySession.SessionType sessionType;
    private int totalCards;
    private int correctAnswers;
    private int incorrectAnswers;
    private int skippedCards;
    private long durationInSeconds;
    private double accuracyPercentage;
    private List<CardResultDto> cardResults;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardResultDto {
        private String flashcardId;
        private boolean correct;
        private long timeSpentSeconds;
        private int attemptNumber;
    }
}