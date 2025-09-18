package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.FlashcardApiDocumentation;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.model.Flashcard;
import com.flashcards.backend.flashcards.service.FlashcardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Tag(name = "Flashcard Management", description = "APIs for managing flashcards, study sessions, and learning progress")
public class FlashcardController {
    private final FlashcardService flashcardService;

    @GetMapping("/{id}")
    @FlashcardApiDocumentation.GetFlashcardById
    public ResponseEntity<FlashcardDto> getFlashcardById(
            @FlashcardApiDocumentation.FlashcardIdParam @PathVariable String id) {
        log.debug("GET /api/flashcards/{} - Finding flashcard by id", id);

        Optional<FlashcardDto> flashcard = flashcardService.findById(id);

        return flashcard.map(flashcardDto -> {
            log.debug("GET /api/flashcards/{} - Flashcard found", id);
            return ResponseEntity.ok(flashcardDto);
        }).orElseGet(() -> {
            log.debug("GET /api/flashcards/{} - Flashcard not found", id);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/deck/{deckId}")
    @FlashcardApiDocumentation.GetFlashcardsByDeck
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByDeck(
            @FlashcardApiDocumentation.DeckIdParam @PathVariable String deckId) {
        log.debug("GET /api/flashcards/deck/{} - Finding flashcards by deck", deckId);

        List<FlashcardDto> flashcards = flashcardService.findByDeckId(deckId);

        log.debug("GET /api/flashcards/deck/{} - Found {} flashcards", deckId, flashcards.size());
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/user/{userId}")
    @FlashcardApiDocumentation.GetFlashcardsByUser
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByUser(
            @FlashcardApiDocumentation.UserIdParam @PathVariable String userId) {
        log.debug("GET /api/flashcards/user/{} - Finding flashcards by user", userId);

        List<FlashcardDto> flashcards = flashcardService.findByUserId(userId);

        log.debug("GET /api/flashcards/user/{} - Found {} flashcards", userId, flashcards.size());
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/deck/{deckId}/difficulty/{difficulty}")
    @FlashcardApiDocumentation.GetFlashcardsByDeck
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByDeckAndDifficulty(
            @FlashcardApiDocumentation.DeckIdParam @PathVariable String deckId,
            @FlashcardApiDocumentation.DifficultyParam @PathVariable Flashcard.DifficultyLevel difficulty) {
        log.debug("GET /api/flashcards/deck/{}/difficulty/{} - Finding flashcards by deck and difficulty", deckId, difficulty);

        List<FlashcardDto> flashcards = flashcardService.findByDeckIdAndDifficulty(deckId, difficulty);

        log.debug("GET /api/flashcards/deck/{}/difficulty/{} - Found {} flashcards", deckId, difficulty, flashcards.size());
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/search")
    @FlashcardApiDocumentation.SearchFlashcards
    public ResponseEntity<List<FlashcardDto>> searchFlashcardsByTag(
            @FlashcardApiDocumentation.TagsParam @RequestParam String tag) {
        log.debug("GET /api/flashcards/search?tag={} - Searching flashcards by tag", tag);

        List<FlashcardDto> flashcards = flashcardService.findByTagsContaining(tag);

        log.debug("GET /api/flashcards/search - Found {} flashcards with tag: {}", flashcards.size(), tag);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping
    @FlashcardApiDocumentation.SearchFlashcards
    public ResponseEntity<List<FlashcardDto>> getAllFlashcards() {
        log.debug("GET /api/flashcards - Finding all flashcards");

        List<FlashcardDto> flashcards = flashcardService.findAll();

        log.debug("GET /api/flashcards - Found {} flashcards", flashcards.size());
        return ResponseEntity.ok(flashcards);
    }

    @PostMapping
    @FlashcardApiDocumentation.CreateFlashcard
    public ResponseEntity<FlashcardDto> createFlashcard(
            @FlashcardApiDocumentation.CreateFlashcardBody @Valid @RequestBody CreateFlashcardDto createFlashcardDto) {
        log.info("POST /api/flashcards - Creating new flashcard for deck: {}", createFlashcardDto.getDeckId());

        FlashcardDto createdFlashcard = flashcardService.createFlashcard(createFlashcardDto);

        log.info("POST /api/flashcards - Flashcard created successfully with id: {}", createdFlashcard.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFlashcard);
    }

    @PostMapping("/batch")
    @FlashcardApiDocumentation.CreateFlashcard
    public ResponseEntity<List<FlashcardDto>> createMultipleFlashcards(
            @FlashcardApiDocumentation.CreateFlashcardBody @Valid @RequestBody List<CreateFlashcardDto> createFlashcardDtos) {
        log.info("POST /api/flashcards/batch - Creating {} flashcards", createFlashcardDtos.size());

        List<FlashcardDto> createdFlashcards = flashcardService.createMultipleFlashcards(createFlashcardDtos);

        log.info("POST /api/flashcards/batch - {} flashcards created successfully", createdFlashcards.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFlashcards);
    }

    @PutMapping("/{id}")
    @FlashcardApiDocumentation.UpdateFlashcard
    public ResponseEntity<FlashcardDto> updateFlashcard(
            @FlashcardApiDocumentation.FlashcardIdParam @PathVariable String id,
            @FlashcardApiDocumentation.UpdateFlashcardBody @Valid @RequestBody FlashcardDto flashcardDto) {
        log.info("PUT /api/flashcards/{} - Updating flashcard", id);

        FlashcardDto updatedFlashcard = flashcardService.updateFlashcard(id, flashcardDto);

        log.info("PUT /api/flashcards/{} - Flashcard updated successfully", id);
        return ResponseEntity.ok(updatedFlashcard);
    }

    @DeleteMapping("/{id}")
    @FlashcardApiDocumentation.DeleteFlashcard
    public ResponseEntity<Void> deleteFlashcard(
            @FlashcardApiDocumentation.FlashcardIdParam @PathVariable String id) {
        log.info("DELETE /api/flashcards/{} - Deleting flashcard", id);

        flashcardService.deleteFlashcard(id);

        log.info("DELETE /api/flashcards/{} - Flashcard deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deck/{deckId}")
    @FlashcardApiDocumentation.DeleteFlashcard
    public ResponseEntity<Void> deleteFlashcardsByDeck(
            @FlashcardApiDocumentation.DeckIdParam @PathVariable String deckId) {
        log.info("DELETE /api/flashcards/deck/{} - Deleting all flashcards in deck", deckId);

        flashcardService.deleteFlashcardsByDeckId(deckId);

        log.info("DELETE /api/flashcards/deck/{} - All flashcards deleted successfully", deckId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/study")
    @FlashcardApiDocumentation.UpdateStudyProgress
    public ResponseEntity<Void> updateStudyProgress(
            @FlashcardApiDocumentation.FlashcardIdParam @PathVariable String id,
            @FlashcardApiDocumentation.StudyProgressBody @RequestParam boolean correct) {
        log.info("POST /api/flashcards/{}/study - Recording study result: {}", id, correct ? "correct" : "incorrect");

        flashcardService.updateStudyStats(id, correct);

        log.info("POST /api/flashcards/{}/study - Study progress updated successfully", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deck/{deckId}/count")
    @FlashcardApiDocumentation.GetFlashcardCount
    public ResponseEntity<Long> getFlashcardCountByDeck(
            @FlashcardApiDocumentation.DeckIdParam @PathVariable String deckId) {
        log.debug("GET /api/flashcards/deck/{}/count - Getting flashcard count for deck", deckId);

        long count = flashcardService.countByDeckId(deckId);

        log.debug("GET /api/flashcards/deck/{}/count - Found {} flashcards", deckId, count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count")
    @FlashcardApiDocumentation.GetFlashcardCount
    public ResponseEntity<Long> getTotalFlashcardCount() {
        log.debug("GET /api/flashcards/count - Getting total flashcard count");

        long count = flashcardService.countAll();

        log.debug("GET /api/flashcards/count - Total flashcards: {}", count);
        return ResponseEntity.ok(count);
    }
}