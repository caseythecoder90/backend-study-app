package com.flashcards.backend.flashcards.dao;

import com.flashcards.backend.flashcards.model.Deck;

import java.util.List;
import java.util.Optional;

public interface DeckDao {
    Optional<Deck> findById(String id);
    List<Deck> findByUserId(String userId);
    List<Deck> findByIsPublicTrue();
    List<Deck> findByCategory(String category);
    List<Deck> findByTagsContaining(String tag);
    List<Deck> findByUserIdAndIsPublic(String userId, boolean isPublic);
    List<Deck> findAll();
    Deck save(Deck deck);
    Deck update(Deck deck);
    void deleteById(String id);
    long count();
}