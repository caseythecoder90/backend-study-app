package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.DeckApiDocumentation;
import com.flashcards.backend.flashcards.dto.CreateDeckDto;
import com.flashcards.backend.flashcards.dto.DeckDto;
import com.flashcards.backend.flashcards.service.DeckService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
@Tag(name = "Deck Management", description = "APIs for managing flashcard decks, collections, and deck discovery")
public class DeckController {
    private final DeckService deckService;

    @GetMapping("/{id}")
    @DeckApiDocumentation.GetDeckById
    public ResponseEntity<DeckDto> getDeckById(
            @DeckApiDocumentation.DeckIdParam @PathVariable String id) {
        log.debug("GET /api/decks/{} - Finding deck by id", id);

        Optional<DeckDto> deck = deckService.findById(id);

        return deck.map(deckDto -> {
            log.debug("GET /api/decks/{} - Deck found: {}", id, deckDto.getTitle());
            return ResponseEntity.ok(deckDto);
        }).orElseGet(() -> {
            log.debug("GET /api/decks/{} - Deck not found", id);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/user/{userId}")
    @DeckApiDocumentation.GetDecksByUser
    public ResponseEntity<List<DeckDto>> getDecksByUser(
            @DeckApiDocumentation.UserIdParam @PathVariable String userId) {
        log.debug("GET /api/decks/user/{} - Finding decks by user", userId);

        List<DeckDto> decks = deckService.findByUserId(userId);

        log.debug("GET /api/decks/user/{} - Found {} decks", userId, decks.size());
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/user/{userId}/visibility/{isPublic}")
    @DeckApiDocumentation.GetDecksByVisibility
    public ResponseEntity<List<DeckDto>> getDecksByUserAndVisibility(
            @DeckApiDocumentation.UserIdParam @PathVariable String userId,
            @DeckApiDocumentation.VisibilityParam @PathVariable boolean isPublic) {
        log.debug("GET /api/decks/user/{}/visibility/{} - Finding decks by user and visibility", userId, isPublic);

        List<DeckDto> decks = deckService.findByUserIdAndVisibility(userId, isPublic);

        log.debug("GET /api/decks/user/{}/visibility/{} - Found {} decks", userId, isPublic, decks.size());
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/public")
    @DeckApiDocumentation.GetPublicDecks
    public ResponseEntity<List<DeckDto>> getPublicDecks() {
        log.debug("GET /api/decks/public - Finding all public decks");

        List<DeckDto> decks = deckService.findPublicDecks();

        log.debug("GET /api/decks/public - Found {} public decks", decks.size());
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/category/{category}")
    @DeckApiDocumentation.GetDecksByCategory
    public ResponseEntity<List<DeckDto>> getDecksByCategory(
            @DeckApiDocumentation.CategoryParam @PathVariable String category) {
        log.debug("GET /api/decks/category/{} - Finding decks by category", category);

        List<DeckDto> decks = deckService.findByCategory(category);

        log.debug("GET /api/decks/category/{} - Found {} decks", category, decks.size());
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/search")
    @DeckApiDocumentation.SearchDecksByTag
    public ResponseEntity<List<DeckDto>> searchDecksByTag(
            @DeckApiDocumentation.TagParam @RequestParam String tag) {
        log.debug("GET /api/decks/search?tag={} - Searching decks by tag", tag);

        List<DeckDto> decks = deckService.findByTag(tag);

        log.debug("GET /api/decks/search - Found {} decks with tag: {}", decks.size(), tag);
        return ResponseEntity.ok(decks);
    }

    @GetMapping
    @DeckApiDocumentation.GetAllDecks
    public ResponseEntity<List<DeckDto>> getAllDecks() {
        log.debug("GET /api/decks - Finding all decks");

        List<DeckDto> decks = deckService.findAll();

        log.debug("GET /api/decks - Found {} decks", decks.size());
        return ResponseEntity.ok(decks);
    }

    @PostMapping
    @DeckApiDocumentation.CreateDeck
    public ResponseEntity<DeckDto> createDeck(
            @DeckApiDocumentation.CreateDeckBody @Valid @RequestBody CreateDeckDto createDeckDto) {
        log.info("POST /api/decks - Creating new deck: {} for user: {}",
                createDeckDto.getTitle(), createDeckDto.getUserId());

        DeckDto createdDeck = deckService.createDeck(createDeckDto);

        log.info("POST /api/decks - Deck created successfully with id: {} and name: {}",
                createdDeck.getId(), createdDeck.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeck);
    }

    @PutMapping("/{id}")
    @DeckApiDocumentation.UpdateDeck
    public ResponseEntity<DeckDto> updateDeck(
            @DeckApiDocumentation.DeckIdParam @PathVariable String id,
            @DeckApiDocumentation.UpdateDeckBody @Valid @RequestBody DeckDto deckDto) {
        log.info("PUT /api/decks/{} - Updating deck", id);

        DeckDto updatedDeck = deckService.updateDeck(id, deckDto);

        log.info("PUT /api/decks/{} - Deck updated successfully: {}", id, updatedDeck.getTitle());
        return ResponseEntity.ok(updatedDeck);
    }

    @DeleteMapping("/{id}")
    @DeckApiDocumentation.DeleteDeck
    public ResponseEntity<Void> deleteDeck(
            @DeckApiDocumentation.DeckIdParam @PathVariable String id) {
        log.info("DELETE /api/decks/{} - Deleting deck", id);

        deckService.deleteDeck(id);

        log.info("DELETE /api/decks/{} - Deck deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @DeckApiDocumentation.GetDeckCount
    public ResponseEntity<Long> getTotalDeckCount() {
        log.debug("GET /api/decks/count - Getting total deck count");

        long count = deckService.countDecks();

        log.debug("GET /api/decks/count - Total decks: {}", count);
        return ResponseEntity.ok(count);
    }

    // Additional convenience endpoints for better UX

    @GetMapping("/categories")
    @DeckApiDocumentation.GetDecksByCategory
    public ResponseEntity<List<String>> getAvailableCategories() {
        log.debug("GET /api/decks/categories - Getting available categories");

        // This would ideally come from a dedicated service method that aggregates unique categories
        // For now, return a static list of common categories
        List<String> categories = List.of(
            "Programming", "Mathematics", "Science", "Language Learning",
            "History", "Geography", "Medicine", "Business", "Personal Development"
        );

        log.debug("GET /api/decks/categories - Found {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/user/{userId}/summary")
    @DeckApiDocumentation.GetDeckStats
    public ResponseEntity<UserDeckSummary> getUserDeckSummary(
            @DeckApiDocumentation.UserIdParam @PathVariable String userId) {
        log.debug("GET /api/decks/user/{}/summary - Getting deck summary for user", userId);

        List<DeckDto> userDecks = deckService.findByUserId(userId);
        List<DeckDto> publicDecks = deckService.findByUserIdAndVisibility(userId, true);
        List<DeckDto> privateDecks = deckService.findByUserIdAndVisibility(userId, false);

        UserDeckSummary summary = UserDeckSummary.builder()
                .totalDecks(userDecks.size())
                .publicDecks(publicDecks.size())
                .privateDecks(privateDecks.size())
                .totalFlashcards(userDecks.stream().mapToInt(DeckDto::getFlashcardCount).sum())
                .build();

        log.debug("GET /api/decks/user/{}/summary - Summary: {} total decks", userId, summary.getTotalDecks());
        return ResponseEntity.ok(summary);
    }

    // Helper class for user deck summary
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Summary statistics of a user's deck collection")
    public static class UserDeckSummary {
        @Schema(description = "Total number of decks owned by user", example = "15")
        private int totalDecks;

        @Schema(description = "Number of public decks", example = "8")
        private int publicDecks;

        @Schema(description = "Number of private decks", example = "7")
        private int privateDecks;

        @Schema(description = "Total flashcards across all decks", example = "347")
        private int totalFlashcards;
    }
}