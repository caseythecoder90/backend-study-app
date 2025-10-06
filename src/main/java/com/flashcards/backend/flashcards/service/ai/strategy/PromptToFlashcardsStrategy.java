package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AIPromptGenerateRequestDto;
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

import static com.flashcards.backend.flashcards.constants.AIConstants.PROMPT_FLASHCARD_GENERATION_TEMPLATE;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Strategy for generating flashcards from a prompt without text or image input.
 * Composes FlashcardResponseParser for parsing logic (composition over inheritance).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptToFlashcardsStrategy implements AIOperationStrategy<AIPromptGenerateRequestDto, List<CreateFlashcardDto>> {

    private final AIConfigProperties aiProperties;
    private final FlashcardResponseParser flashcardResponseParser;

    @Override
    public List<CreateFlashcardDto> execute(AIPromptGenerateRequestDto input, AIModelEnum model) {
        throw new UnsupportedOperationException("Use AIExecutionService.executeOperation() instead");
    }

    @Override
    public Message buildMessage(AIPromptGenerateRequestDto input) {
        Map<String, Object> promptVariables = Map.of(
            "count", input.getCount(),
            "prompt", input.getPrompt(),
            "topic", isNotBlank(input.getTopic()) ? input.getTopic() : "General"
        );

        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_FLASHCARD_GENERATION_TEMPLATE);
        String promptText = promptTemplate.render(promptVariables);

        return new UserMessage(promptText);
    }

    @Override
    public List<CreateFlashcardDto> parseResponse(String response, AIPromptGenerateRequestDto input) {
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
    public void validateInput(AIPromptGenerateRequestDto input) {
        requireNonNull(input, "Prompt generation request cannot be null");
        requireNonNull(input.getPrompt(), "Prompt cannot be null");

        // Validate flashcard count
        if (input.getCount() > aiProperties.getLimits().getMaxFlashcardsPerRequest()) {
            throw new ServiceException(
                    "Flashcard count exceeds maximum allowed: " + aiProperties.getLimits().getMaxFlashcardsPerRequest(),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }
    }

    @Override
    public String getOperationName() {
        return "PromptToFlashcards";
    }
}