package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.AIApiDocumentation;
import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.AIImageGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.AIPromptGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.AISummaryRequestDto;
import com.flashcards.backend.flashcards.dto.AISummaryResponseDto;
import com.flashcards.backend.flashcards.dto.AITextToImageRequestDto;
import com.flashcards.backend.flashcards.dto.AITextToImageResponseDto;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.service.FlashcardService;
import com.flashcards.backend.flashcards.service.ai.AIExecutionService;
import com.flashcards.backend.flashcards.service.ai.AIImageGenerationService;
import com.flashcards.backend.flashcards.service.ai.strategy.ContentToSummaryStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.ImageToFlashcardsStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.PromptToFlashcardsStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.TextToFlashcardsStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.TextToImageStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final AIExecutionService aiExecutionService;
    private final AIImageGenerationService aiImageGenerationService;
    private final TextToFlashcardsStrategy textToFlashcardsStrategy;
    private final ImageToFlashcardsStrategy imageToFlashcardsStrategy;
    private final PromptToFlashcardsStrategy promptToFlashcardsStrategy;
    private final ContentToSummaryStrategy contentToSummaryStrategy;
    private final TextToImageStrategy textToImageStrategy;

    @PostMapping("/flashcards/generate-text")
    @PreAuthorize("hasRole('USER')")
    @AIApiDocumentation.GenerateFlashcardsFromText
    public ResponseEntity<List<FlashcardDto>> generateFlashcardsFromText(
            @Valid @RequestBody AIGenerateRequestDto request) {

        log.info("Generating flashcards from text for user: {}, deck: {}, count: {}",
                request.getUserId(), request.getDeckId(), request.getCount());

        List<CreateFlashcardDto> generatedFlashcards = aiExecutionService.executeOperation(
                textToFlashcardsStrategy, request, request.getModel());

        List<FlashcardDto> savedFlashcards = flashcardService.createMultipleFlashcards(generatedFlashcards);

        log.info("Successfully generated and saved {} flashcards from text", savedFlashcards.size());
        return ResponseEntity.ok(savedFlashcards);
    }

    @PostMapping(value = "/flashcards/generate-image", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('USER')")
    @AIApiDocumentation.GenerateFlashcardsFromImage
    public ResponseEntity<List<CreateFlashcardDto>> generateFlashcardsFromImage(
            @Valid @ModelAttribute AIImageGenerateRequestDto request) {

        log.info("Generating flashcards from image for user: {}, deck: {}, count: {}",
                request.getUserId(), request.getDeckId(), request.getCount());

        List<CreateFlashcardDto> generatedFlashcards = aiExecutionService.executeOperation(
                imageToFlashcardsStrategy, request, request.getModel());

        log.info("Successfully generated {} flashcards from image", generatedFlashcards.size());
        return ResponseEntity.ok(generatedFlashcards);
    }

    @PostMapping("/images/generate")
    @PreAuthorize("hasRole('USER')")
    @AIApiDocumentation.GenerateImageFromText
    public ResponseEntity<AITextToImageResponseDto> generateImageFromText(
            @Valid @RequestBody AITextToImageRequestDto request) {

        log.info("Generating educational image for user: {}, description: {}",
                request.getUserId(), request.getDescription());

        AITextToImageResponseDto response = aiImageGenerationService.executeImageOperation(
                textToImageStrategy, request, request.getModel());

        log.info("Successfully generated {} images", response.getImages().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/flashcards/generate-prompt")
    @PreAuthorize("hasRole('USER')")
    @AIApiDocumentation.GenerateFlashcardsFromPrompt
    public ResponseEntity<List<CreateFlashcardDto>> generateFlashcardsFromPrompt(
            @Valid @RequestBody AIPromptGenerateRequestDto request) {

        log.info("Generating flashcards from prompt for user: {}, deck: {}, count: {}",
                request.getUserId(), request.getDeckId(), request.getCount());

        List<CreateFlashcardDto> generatedFlashcards = aiExecutionService.executeOperation(
                promptToFlashcardsStrategy, request, request.getModel());

        log.info("Successfully generated {} flashcards from prompt", generatedFlashcards.size());
        return ResponseEntity.ok(generatedFlashcards);
    }

    @PostMapping("/summary/generate")
    @PreAuthorize("hasRole('USER')")
    @AIApiDocumentation.GenerateSummary
    public ResponseEntity<AISummaryResponseDto> generateSummary(
            @Valid @RequestBody AISummaryRequestDto request) {

        log.info("Generating summary for user: {}, source type: {}",
                request.getUserId(), request.getSourceType());

        AISummaryResponseDto response = aiExecutionService.executeOperation(
                contentToSummaryStrategy, request, request.getModel());

        log.info("Successfully generated summary with {} words", response.getWordCount());
        return ResponseEntity.ok(response);
    }

}