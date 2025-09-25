package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.AIApiDocumentation;
import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@AIApiDocumentation
public class AIController {

    private final FlashcardService flashcardService;

    @PostMapping("/flashcards/generate-text")
    @PreAuthorize("hasRole('USER')")
    @AIApiDocumentation.GenerateFlashcardsFromText
    public ResponseEntity<List<FlashcardDto>> generateFlashcardsFromText(
            @Valid @RequestBody AIGenerateRequestDto request) {

        log.info("Generating flashcards from text for user: {}, deck: {}, count: {}",
                request.getUserId(), request.getDeckId(), request.getCount());

        List<FlashcardDto> generatedFlashcards = flashcardService.generateFlashcardsFromText(request);

        log.info("Successfully generated {} flashcards from text", generatedFlashcards.size());
        return ResponseEntity.ok(generatedFlashcards);
    }
}