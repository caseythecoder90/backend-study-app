package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.service.ai.parser.FlashcardResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AIConstants.FLASHCARD_GENERATION_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_FLASHCARD_COUNT_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_REQUEST_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_TEXT_LENGTH_EXCEEDED;
import static java.util.Objects.requireNonNull;

/**
 * Strategy for generating flashcards from plain text input.
 * Composes FlashcardResponseParser for parsing logic (composition over inheritance).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextToFlashcardsStrategy implements AIOperationStrategy<AIGenerateRequestDto, List<CreateFlashcardDto>> {

    private final AIConfigProperties aiProperties;
    private final FlashcardResponseParser flashcardResponseParser;

    @Override
    public List<CreateFlashcardDto> execute(AIGenerateRequestDto input, AIModelEnum model) {
        throw new UnsupportedOperationException("Use AIExecutionService.executeOperation() instead");
    }

    @Override
    public Message buildMessage(AIGenerateRequestDto input) {
        Map<String, Object> promptVariables = Map.of(
                "text", input.getText(),
                "count", input.getCount()
        );

        PromptTemplate promptTemplate = new PromptTemplate(FLASHCARD_GENERATION_TEMPLATE);
        String promptText = promptTemplate.render(promptVariables);

        return new UserMessage(promptText);
    }

    @Override
    public List<CreateFlashcardDto> parseResponse(String response, AIGenerateRequestDto input) {
        // Delegate to composed parser - clean separation of concerns
        return flashcardResponseParser.parse(
            response,
            input.getUserId(),
            input.getDeckId(),
            input.getCount()
        );
    }

    @Override
    public AIModelEnum getDefaultModel() {
        return AIModelEnum.GPT_4O_MINI;
    }

    @Override
    public void validateInput(AIGenerateRequestDto input) {
        // Note: Basic validation (@NotBlank, @Size, @Min, @Max) is handled by Jakarta Bean Validation on the DTO
        // Only business logic validation that depends on runtime configuration is performed here

        // Validate text length against configuration limits (DTO has @Size but this checks config)
        if (input.getText().length() > aiProperties.getLimits().getMaxTextLength()) {
            throw new ServiceException(
                    AI_TEXT_LENGTH_EXCEEDED.formatted(aiProperties.getLimits().getMaxTextLength()),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }

        // Validate flashcard count against configuration limits
        if (input.getCount() > aiProperties.getLimits().getMaxFlashcardsPerRequest()) {
            throw new ServiceException(
                    AI_FLASHCARD_COUNT_EXCEEDED.formatted(aiProperties.getLimits().getMaxFlashcardsPerRequest()),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }
    }

    @Override
    public String getOperationName() {
        return "TextToFlashcards";
    }
}