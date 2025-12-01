package com.flashcards.backend.flashcards.repository;

import com.flashcards.backend.flashcards.model.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends MongoRepository<Flashcard, String> {
    // Non-paginated methods (for internal use)
    List<Flashcard> findByDeckId(String deckId);
    List<Flashcard> findByUserId(String userId);
    List<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty);
    List<Flashcard> findByTagsContaining(String tag);
    long countByDeckId(String deckId);
    void deleteByDeckId(String deckId);

    // Paginated methods (for API endpoints)
    Page<Flashcard> findByUserId(String userId, Pageable pageable);
    Page<Flashcard> findByDeckId(String deckId, Pageable pageable);
    Page<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty, Pageable pageable);
    Page<Flashcard> findByUserIdAndTagsContaining(String userId, String tag, Pageable pageable);
}