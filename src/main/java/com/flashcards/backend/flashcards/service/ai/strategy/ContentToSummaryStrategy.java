package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.dto.AISummaryRequestDto;
import com.flashcards.backend.flashcards.dto.AISummaryResponseDto;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.service.ai.parser.SummaryResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AIConstants.SUMMARY_DETAILED_WORDS;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUMMARY_GENERATION_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUMMARY_LONG_WORDS;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUMMARY_MEDIUM_WORDS;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUMMARY_SHORT_WORDS;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Strategy for generating summaries of content (text, deck, or flashcards).
 * Composes SummaryResponseParser for parsing logic (composition over inheritance).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentToSummaryStrategy implements AIOperationStrategy<AISummaryRequestDto, AISummaryResponseDto> {

    private final SummaryResponseParser summaryResponseParser;
    private long startTime; // Track generation time

    @Override
    public AISummaryResponseDto execute(AISummaryRequestDto input, AIModelEnum model) {
        throw new UnsupportedOperationException("Use AIExecutionService.executeOperation() instead");
    }

    @Override
    public Message buildMessage(AISummaryRequestDto input) {
        // Track start time for metrics
        startTime = System.currentTimeMillis();

        // Build content to summarize
        String contentToSummarize = buildContentForSummary(input);

        // Build prompt
        int wordCount = getTargetWordCount(input.getLength());

        Map<String, Object> promptVariables = Map.of(
            "length", input.getLength().toString().toLowerCase(),
            "format", input.getFormat().toString().toLowerCase().replace("_", " "),
            "content", contentToSummarize,
            "prompt", isNotBlank(input.getPrompt()) ? input.getPrompt() : "",
            "wordCount", wordCount
        );

        PromptTemplate promptTemplate = new PromptTemplate(SUMMARY_GENERATION_TEMPLATE);
        String promptText = promptTemplate.render(promptVariables);

        return new UserMessage(promptText);
    }

    @Override
    public AISummaryResponseDto parseResponse(String response, AISummaryRequestDto input) {
        // Delegate to composed parser
        String summary = summaryResponseParser.parse(response);
        int wordCount = summaryResponseParser.countWords(summary);

        // Build response DTO with metadata
        return AISummaryResponseDto.builder()
            .summary(summary)
            .format(input.getFormat())
            .length(input.getLength())
            .wordCount(wordCount)
            .modelUsed(input.getModel() != null ? input.getModel() : getDefaultModel())
            .sourceType(input.getSourceType())
            .deckId(input.getDeckId())
            .flashcardCount(input.getFlashcardIds() != null ? input.getFlashcardIds().size() : null)
            .generatedAt(Instant.now())
            .generationTimeMs(System.currentTimeMillis() - startTime)
            .build();
    }

    @Override
    public AIModelEnum getDefaultModel() {
        return AIModelEnum.GPT_4O_MINI;
    }

    @Override
    public void validateInput(AISummaryRequestDto input) {
        requireNonNull(input, "Summary request cannot be null");
        requireNonNull(input.getSourceType(), "Source type cannot be null");
        requireNonNull(input.getFormat(), "Format cannot be null");
        requireNonNull(input.getLength(), "Length cannot be null");

        // Validate based on source type
        switch (input.getSourceType()) {
            case TEXT -> requireNonNull(input.getText(), "Text content cannot be null for TEXT source type");
            case DECK -> requireNonNull(input.getDeckId(), "Deck ID cannot be null for DECK source type");
            case FLASHCARDS -> requireNonNull(input.getFlashcardIds(), "Flashcard IDs cannot be null for FLASHCARDS source type");
            case PROMPT -> requireNonNull(input.getPrompt(), "Prompt cannot be null for PROMPT source type");
        }
    }

    @Override
    public String getOperationName() {
        return "ContentToSummary";
    }

    private String buildContentForSummary(AISummaryRequestDto request) {
        return switch (request.getSourceType()) {
            case TEXT -> request.getText();
            case DECK -> fetchDeckContent(request.getDeckId());
            case FLASHCARDS -> fetchFlashcardsContent(request.getFlashcardIds());
            case PROMPT -> request.getPrompt();
        };
    }

    private String fetchDeckContent(String deckId) {
        // TODO: Implement fetching deck content from database
        // This would call DeckService to get all flashcards in the deck
        log.warn("Deck content fetching not yet implemented for deck: {}", deckId);
        return "Deck content placeholder";
    }

    private String fetchFlashcardsContent(java.util.List<String> flashcardIds) {
        // TODO: Implement fetching flashcards content from database
        // This would call FlashcardService to get specific flashcards
        log.warn("Flashcards content fetching not yet implemented for {} cards",
            flashcardIds != null ? flashcardIds.size() : 0);
        return "Flashcards content placeholder";
    }

    private int getTargetWordCount(AISummaryRequestDto.SummaryLength length) {
        return switch (length) {
            case SHORT -> SUMMARY_SHORT_WORDS;
            case MEDIUM -> SUMMARY_MEDIUM_WORDS;
            case LONG -> SUMMARY_LONG_WORDS;
            case DETAILED -> SUMMARY_DETAILED_WORDS;
        };
    }
}