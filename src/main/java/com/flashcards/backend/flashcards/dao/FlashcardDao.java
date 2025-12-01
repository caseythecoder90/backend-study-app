package com.flashcards.backend.flashcards.dao;

import com.flashcards.backend.flashcards.model.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FlashcardDao {
    // Non-paginated query methods
    Optional<Flashcard> findById(String id);
    List<Flashcard> findByDeckId(String deckId);
    List<Flashcard> findByUserId(String userId);
    List<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty);
    List<Flashcard> findByTagsContaining(String tag);
    List<Flashcard> findAll();

    // Paginated query methods
    Page<Flashcard> findByUserId(String userId, Pageable pageable);
    Page<Flashcard> findByDeckId(String deckId, Pageable pageable);
    Page<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty, Pageable pageable);
    Page<Flashcard> findByUserIdAndTagsContaining(String userId, String tag, Pageable pageable);
    Page<Flashcard> findAll(Pageable pageable);

    // Write methods
    Flashcard save(Flashcard flashcard);
    Flashcard update(Flashcard flashcard);
    List<Flashcard> saveAll(List<Flashcard> flashcards);
    void deleteById(String id);
    void deleteByDeckId(String deckId);

    // Count methods
    long countByDeckId(String deckId);
    long count();
}