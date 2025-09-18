package com.flashcards.backend.flashcards.dao;

import com.flashcards.backend.flashcards.model.Flashcard;

import java.util.List;
import java.util.Optional;

public interface FlashcardDao {
    Optional<Flashcard> findById(String id);
    List<Flashcard> findByDeckId(String deckId);
    List<Flashcard> findByUserId(String userId);
    List<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty);
    List<Flashcard> findByTagsContaining(String tag);
    List<Flashcard> findAll();
    Flashcard save(Flashcard flashcard);
    Flashcard update(Flashcard flashcard);
    List<Flashcard> saveAll(List<Flashcard> flashcards);
    void deleteById(String id);
    void deleteByDeckId(String deckId);
    long countByDeckId(String deckId);
    long count();
}