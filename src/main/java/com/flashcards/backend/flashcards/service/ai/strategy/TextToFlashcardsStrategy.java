package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.model.Flashcard;
import com.flashcards.backend.flashcards.service.ai.response.AICardContent;
import com.flashcards.backend.flashcards.service.ai.response.AICodeBlock;
import com.flashcards.backend.flashcards.service.ai.response.AIContentBlock;
import com.flashcards.backend.flashcards.service.ai.response.AIFlashcardResponse;
import com.flashcards.backend.flashcards.service.ai.response.AIGeneratedCard;
import com.flashcards.backend.flashcards.service.ai.response.AITextBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AIConstants.FLASHCARD_GENERATION_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_FLASHCARD_COUNT_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_TEXT_LENGTH_EXCEEDED;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Strategy for generating flashcards from plain text input.
 * Uses BeanOutputConverter for structured JSON parsing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextToFlashcardsStrategy implements AIOperationStrategy<AIGenerateRequestDto, List<CreateFlashcardDto>> {

    private final AIConfigProperties aiProperties;

    @Override
    // why do we have these execute methods on the strategy interface when they aren't being used for anything?
    // we should probably remove these
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
        BeanOutputConverter<AIFlashcardResponse> converter =
            new BeanOutputConverter<>(AIFlashcardResponse.class);

        AIFlashcardResponse aiResponse = converter.convert(response);

        return aiResponse.flashcards().stream()
            .map(card -> convertToCreateDto(card, input.getUserId(), input.getDeckId()))
            .toList();
    }

    private CreateFlashcardDto convertToCreateDto(AIGeneratedCard card, String userId, String deckId) {
        return CreateFlashcardDto.builder()
            .userId(userId)
            .deckId(deckId)
            .front(convertCardContent(card.front()))
            .back(convertCardContent(card.back()))
            .hint(card.hint())
            .tags(card.tags())
            .difficulty(parseDifficulty(card.difficulty()))
            .build();
    }

    private FlashcardDto.CardContentDto convertCardContent(AICardContent content) {
        List<FlashcardDto.ContentBlockDto> blockDtos = content.blocks().stream()
            .map(this::convertBlock)
            .toList();

        return FlashcardDto.CardContentDto.builder()
            .blocks(blockDtos)
            .build();
    }

    private FlashcardDto.ContentBlockDto convertBlock(AIContentBlock block) {
        return switch (block) {
            case AITextBlock text -> FlashcardDto.TextBlockDto.builder()
                .content(text.content())
                .build();
            case AICodeBlock code -> FlashcardDto.CodeBlockDto.builder()
                .language(code.language())
                .code(code.code())
                .fileName(code.fileName())
                .highlighted(isNull(code.highlighted()) ? false : code.highlighted())
                .highlightedLines(code.highlightedLines())
                .build();
        };
    }

    private Flashcard.DifficultyLevel parseDifficulty(String difficultyStr) {
        if (isBlank(difficultyStr)) {
            return Flashcard.DifficultyLevel.NOT_SET;
        }
        try {
            return Flashcard.DifficultyLevel.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Flashcard.DifficultyLevel.NOT_SET;
        }
    }

    @Override
    public AIModelEnum getDefaultModel() {
        return AIModelEnum.GPT_4O_MINI;
    }

    @Override
    public void validateInput(AIGenerateRequestDto input) {
        // *** NOTE FOR AI HELP: I am already validating the text length as well as the flashcard count in the @Valid annotation
        // based validation. Do I really need to add this here as well?
        // In some of the code reviews there was security concerns about lack of input validation and sanitization on text data

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