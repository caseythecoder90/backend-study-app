package com.flashcards.backend.flashcards.repository;

import com.flashcards.backend.flashcards.model.Flashcard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends MongoRepository<Flashcard, String> {
    List<Flashcard> findByDeckId(String deckId);
    List<Flashcard> findByUserId(String userId);
    List<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty);
    List<Flashcard> findByTagsContaining(String tag);
    long countByDeckId(String deckId);
    void deleteByDeckId(String deckId);
}