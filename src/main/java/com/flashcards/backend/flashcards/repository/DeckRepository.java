package com.flashcards.backend.flashcards.repository;

import com.flashcards.backend.flashcards.model.Deck;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends MongoRepository<Deck, String> {
    List<Deck> findByUserId(String userId);
    List<Deck> findByIsPublicTrue();
    List<Deck> findByCategory(String category);
    List<Deck> findByTagsContaining(String tag);
    List<Deck> findByUserIdAndIsPublic(String userId, boolean isPublic);
}