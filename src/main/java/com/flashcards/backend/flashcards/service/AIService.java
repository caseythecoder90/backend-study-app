package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.model.Flashcard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_BILLING;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_MODEL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_QUOTA;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_RATE_LIMIT;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_SERVICE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_TIMEOUT;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_TIMED_OUT;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_TOO_MANY_REQUESTS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ERROR_UNAVAILABLE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_FLASHCARD_COUNT_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_REQUEST_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_RESPONSE_PARSE_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_TEXT_LENGTH_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_OPERATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;
    private final AIConfigProperties aiProperties;
    private final ObjectMapper objectMapper;

    private static final String FLASHCARD_GENERATION_TEMPLATE = """
            Generate exactly {count} flashcards from the following text content:

            {text}

            Requirements:
            - Create educational flashcards that focus on key concepts
            - Each flashcard should have distinct front and back content
            - Front should be a question or prompt
            - Back should be a clear, concise answer
            - Include code examples where relevant (format as JSON with language and code fields)
            - Vary difficulty levels appropriately
            - Generate relevant tags for categorization

            Return ONLY a valid JSON array with this exact structure (no additional text):
            [
              {{
                "front": {{
                  "text": "Question or prompt text",
                  "codeBlocks": [
                    {{
                      "language": "java",
                      "code": "example code",
                      "fileName": "optional filename",
                      "highlighted": false
                    }}
                  ],
                  "type": "TEXT_ONLY"
                }},
                "back": {{
                  "text": "Answer or explanation text",
                  "codeBlocks": [],
                  "type": "TEXT_ONLY"
                }},
                "hint": "Optional helpful hint",
                "tags": ["tag1", "tag2"],
                "difficulty": "MEDIUM"
              }}
            ]

            Valid difficulty levels: EASY, MEDIUM, HARD, NOT_SET
            Valid content types: TEXT_ONLY, CODE_ONLY, MIXED
            """;

    public List<CreateFlashcardDto> generateFlashcardsFromText(AIGenerateRequestDto request) {
        return executeWithExceptionHandling(() -> {
            validateGenerationRequest(request);

            String promptText = buildPrompt(request);
            log.debug("Sending prompt to AI service: {}", promptText);

            String response = chatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            log.debug("Received AI response: {}", response);

            return parseFlashcardResponse(response, request);
        }, () -> SERVICE_OPERATION_FAILED.formatted("generate flashcards from AI", "text"));
    }

    private String buildPrompt(AIGenerateRequestDto request) {
        Map<String, Object> promptVariables = Map.of(
                "text", request.getText(),
                "count", request.getCount()
        );

        PromptTemplate promptTemplate = new PromptTemplate(FLASHCARD_GENERATION_TEMPLATE);
        return promptTemplate.render(promptVariables);
    }

    private List<CreateFlashcardDto> parseFlashcardResponse(String response, AIGenerateRequestDto request) {
        try {
            String cleanResponse = cleanJsonResponse(response);
            List<Map<String, Object>> flashcardMaps = objectMapper.readValue(
                    cleanResponse,
                    new TypeReference<>() {}
            );

            return flashcardMaps.stream()
                    .map(flashcardMap -> convertToCreateFlashcardDto(flashcardMap, request))
                    .filter(Objects::nonNull)
                    .toList();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON: {}", response, e);
            throw new ServiceException(
                    AI_RESPONSE_PARSE_FAILED,
                    ErrorCode.SERVICE_AI_GENERATION_ERROR,
                    e
            );
        }
    }

    private String cleanJsonResponse(String response) {
        // Remove any markdown code block markers or extra text
        String cleaned = response.trim();

        // Remove markdown code block markers if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        // Find the JSON array start and end
        int jsonStart = cleaned.indexOf('[');
        int jsonEnd = cleaned.lastIndexOf(']') + 1;

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd);
        }

        return cleaned.trim();
    }

    @SuppressWarnings("unchecked")
    private CreateFlashcardDto convertToCreateFlashcardDto(Map<String, Object> flashcardMap, AIGenerateRequestDto request) {
        try {
            CreateFlashcardDto.CreateFlashcardDtoBuilder builder = CreateFlashcardDto.builder()
                    .deckId(request.getDeckId())
                    .userId(request.getUserId());

            // Parse front content
            Map<String, Object> frontMap = (Map<String, Object>) flashcardMap.get("front");
            if (Objects.nonNull(frontMap)) {
                builder.front(parseCardContent(frontMap));
            }

            // Parse back content
            Map<String, Object> backMap = (Map<String, Object>) flashcardMap.get("back");
            if (Objects.nonNull(backMap)) {
                builder.back(parseCardContent(backMap));
            }

            // Parse other fields
            if (flashcardMap.containsKey("hint")) {
                builder.hint((String) flashcardMap.get("hint"));
            }

            if (flashcardMap.containsKey("tags")) {
                List<String> tags = (List<String>) flashcardMap.get("tags");
                builder.tags(tags);
            }

            if (flashcardMap.containsKey("difficulty")) {
                String difficultyStr = (String) flashcardMap.get("difficulty");
                builder.difficulty(parseDifficulty(difficultyStr));
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Failed to convert flashcard map to DTO: {}", flashcardMap, e);
            return null; // Skip invalid flashcards
        }
    }

    @SuppressWarnings("unchecked")
    private FlashcardDto.CardContentDto parseCardContent(Map<String, Object> contentMap) {
        FlashcardDto.CardContentDto.CardContentDtoBuilder builder = FlashcardDto.CardContentDto.builder();

        if (contentMap.containsKey("text")) {
            builder.text((String) contentMap.get("text"));
        }

        if (contentMap.containsKey("type")) {
            String typeStr = (String) contentMap.get("type");
            builder.type(parseContentType(typeStr));
        }

        if (contentMap.containsKey("codeBlocks")) {
            List<Map<String, Object>> codeBlockMaps = (List<Map<String, Object>>) contentMap.get("codeBlocks");
            List<FlashcardDto.CodeBlockDto> codeBlocks = codeBlockMaps.stream()
                    .map(this::parseCodeBlock)
                    .filter(Objects::nonNull)
                    .toList();
            builder.codeBlocks(codeBlocks);
        }

        return builder.build();
    }

    private FlashcardDto.CodeBlockDto parseCodeBlock(Map<String, Object> codeBlockMap) {
        try {
            FlashcardDto.CodeBlockDto.CodeBlockDtoBuilder builder = FlashcardDto.CodeBlockDto.builder();

            if (codeBlockMap.containsKey("language")) {
                builder.language((String) codeBlockMap.get("language"));
            }

            if (codeBlockMap.containsKey("code")) {
                builder.code((String) codeBlockMap.get("code"));
            }

            if (codeBlockMap.containsKey("fileName")) {
                builder.fileName((String) codeBlockMap.get("fileName"));
            }

            if (codeBlockMap.containsKey("highlighted")) {
                builder.highlighted(BooleanUtils.isTrue((Boolean) codeBlockMap.get("highlighted")));
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Failed to parse code block: {}", codeBlockMap, e);
            return null;
        }
    }

    private Flashcard.DifficultyLevel parseDifficulty(String difficultyStr) {
        if (StringUtils.isBlank(difficultyStr)) {
            return Flashcard.DifficultyLevel.NOT_SET;
        }

        try {
            return Flashcard.DifficultyLevel.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid difficulty level: {}, defaulting to NOT_SET", difficultyStr);
            return Flashcard.DifficultyLevel.NOT_SET;
        }
    }

    private Flashcard.ContentType parseContentType(String typeStr) {
        if (StringUtils.isBlank(typeStr)) {
            return Flashcard.ContentType.TEXT_ONLY;
        }

        try {
            return Flashcard.ContentType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid content type: {}, defaulting to TEXT_ONLY", typeStr);
            return Flashcard.ContentType.TEXT_ONLY;
        }
    }

    private void validateGenerationRequest(AIGenerateRequestDto request) {
        Objects.requireNonNull(request, AI_REQUEST_NULL);

        // Additional business logic validation beyond annotations
        if (request.getText().length() > aiProperties.getLimits().getMaxTextLength()) {
            throw new ServiceException(
                    AI_TEXT_LENGTH_EXCEEDED.formatted(aiProperties.getLimits().getMaxTextLength()),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }

        if (request.getCount() > aiProperties.getLimits().getMaxFlashcardsPerRequest()) {
            throw new ServiceException(
                    AI_FLASHCARD_COUNT_EXCEEDED.formatted(aiProperties.getLimits().getMaxFlashcardsPerRequest()),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, Supplier<String> errorMessageSupplier) {
        try {
            return operation.get();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in AIService: {}", e.getMessage(), e);

            // Map common AI service exceptions to appropriate error codes
            ErrorCode errorCode = determineErrorCode(e);
            throw new ServiceException(errorMessageSupplier.get(), errorCode, e);
        }
    }

    private ErrorCode determineErrorCode(Exception e) {
        String message = e.getMessage();
        if (Objects.isNull(message)) {
            return ErrorCode.SERVICE_AI_GENERATION_ERROR;
        }

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains(AI_ERROR_RATE_LIMIT) || lowerMessage.contains(AI_ERROR_TOO_MANY_REQUESTS)) {
            return ErrorCode.SERVICE_AI_RATE_LIMIT_EXCEEDED;
        }

        if (lowerMessage.contains(AI_ERROR_QUOTA) || lowerMessage.contains(AI_ERROR_BILLING)) {
            return ErrorCode.SERVICE_AI_QUOTA_EXCEEDED;
        }

        if (lowerMessage.contains(AI_ERROR_TIMEOUT) || lowerMessage.contains(AI_ERROR_TIMED_OUT)) {
            return ErrorCode.SERVICE_AI_PROCESSING_TIMEOUT;
        }

        if (lowerMessage.contains(AI_ERROR_UNAVAILABLE) || lowerMessage.contains(AI_ERROR_SERVICE)) {
            return ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE;
        }

        if (lowerMessage.contains(AI_ERROR_MODEL) || lowerMessage.contains(AI_ERROR_INVALID)) {
            return ErrorCode.SERVICE_AI_MODEL_ERROR;
        }

        return ErrorCode.SERVICE_AI_GENERATION_ERROR;
    }
}