package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AIImageGenerateRequestDto;
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
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AIConstants.IMAGE_FLASHCARD_GENERATION_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_FLASHCARD_COUNT_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_IMAGE_PROCESSING_FAILED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Strategy for generating flashcards from images using vision models.
 * Composes FlashcardResponseParser for parsing logic (composition over inheritance).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageToFlashcardsStrategy implements AIOperationStrategy<AIImageGenerateRequestDto, List<CreateFlashcardDto>> {

    private final AIConfigProperties aiProperties;
    private final FlashcardResponseParser flashcardResponseParser;
    public static final String COUNT = "count";
    public static final String PROMPT = "prompt";
    public static final String FALLBACK_IMAGE_TO_FLASHCARDS_PROMPT = "Analyze the image content";
    
    @Override
    public List<CreateFlashcardDto> execute(AIImageGenerateRequestDto input, AIModelEnum model) {
        throw new UnsupportedOperationException("Use AIExecutionService.executeOperation() instead");
    }

    @Override
    public Message buildMessage(AIImageGenerateRequestDto input) {
        
        Map<String, Object> promptVariables = Map.of(
            COUNT, input.getCount(),
            PROMPT, isNotBlank(input.getPrompt()) ? input.getPrompt() : FALLBACK_IMAGE_TO_FLASHCARDS_PROMPT
        );

        PromptTemplate promptTemplate = new PromptTemplate(IMAGE_FLASHCARD_GENERATION_TEMPLATE);
        String promptText = promptTemplate.render(promptVariables);

        try {
            byte[] imageBytes = input.getImage().getBytes();
            String contentType = input.getImage().getContentType();

            ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
            Media imageMedia = new Media(MimeTypeUtils.parseMimeType(contentType), imageResource);

            return UserMessage.builder()
                    .text(promptText)
                    .media(imageMedia)
                    .build();
        } catch (Exception e) {
            log.error("Failed to read image file from multipart", e);
            throw new ServiceException(
                    AI_IMAGE_PROCESSING_FAILED,
                    ErrorCode.SERVICE_AI_INVALID_CONTENT, 
                    e
            );
        }
    }

    @Override
    public List<CreateFlashcardDto> parseResponse(String response, AIImageGenerateRequestDto input) {
        return flashcardResponseParser.parse(
            response,
            input.getUserId(),
            input.getDeckId(),
            input.getCount()
        );
    }

    @Override
    public boolean requiresVision() {
        return true;
    }

    @Override
    public AIModelEnum getDefaultModel() {
        return AIModelEnum.GPT_4O;
    }

    @Override
    public void validateInput(AIImageGenerateRequestDto input) {
        // Note: Image validation (@NotNull, @ValidImage) is handled by Jakarta Bean Validation on the DTO
        // Only business logic validation that depends on runtime configuration is performed here

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
        return "ImageToFlashcards";
    }
}