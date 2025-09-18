package com.flashcards.backend.flashcards.repository;

import com.flashcards.backend.flashcards.model.StudySession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudySessionRepository extends MongoRepository<StudySession, String> {
    List<StudySession> findByUserId(String userId);
    List<StudySession> findByDeckId(String deckId);
    List<StudySession> findByUserIdAndDeckId(String userId, String deckId);
    List<StudySession> findByUserIdAndCompletedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
}