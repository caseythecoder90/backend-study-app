package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.enums.AIProviderEnum;
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
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.AIConstants.DEFAULT_TEMPERATURE;
import static com.flashcards.backend.flashcards.constants.AIConstants.FLASHCARD_GENERATION_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_BACK;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_CODE;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_CODE_BLOCKS;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_DIFFICULTY;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_FILE_NAME;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_FRONT;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_HIGHLIGHTED;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_HINT;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_LANGUAGE;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_TAGS;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_TEXT;
import static com.flashcards.backend.flashcards.constants.AIConstants.JSON_FIELD_TYPE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ALL_MODELS_UNAVAILABLE;
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
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_NO_VALID_FLASHCARDS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_REQUEST_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_RESPONSE_INCOMPLETE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_RESPONSE_MALFORMED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_RESPONSE_PARSE_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_RESPONSE_TRUNCATED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_TEXT_LENGTH_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_OPERATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ModelSelectorService modelSelectorService;
    private final AIConfigProperties aiProperties;
    private final ObjectMapper objectMapper;

    public List<CreateFlashcardDto> generateFlashcardsFromText(AIGenerateRequestDto request) {
        return executeWithExceptionHandling(() -> {
            validateGenerationRequest(request);

            String promptText = buildPrompt(request);
            log.debug("Sending prompt to AI service: {}", promptText);

            // Get the selected model enum (with default)
            AIModelEnum selectedModel = Objects.nonNull(request.getModel()) ? request.getModel() : AIModelEnum.GPT_4O_MINI;

            // Try the primary model with fallback support
            return generateWithFallback(selectedModel, promptText, request);
        }, () -> SERVICE_OPERATION_FAILED.formatted("generate flashcards from AI", "text"));
    }

    private List<CreateFlashcardDto> generateWithFallback(AIModelEnum primaryModel, String promptText, AIGenerateRequestDto request) {
        // First try with the requested model
        try {
            return attemptGeneration(primaryModel, promptText, request);
        } catch (Exception primaryException) {
            log.warn("Primary model {} failed: {}", primaryModel.getDisplayName(), primaryException.getMessage());

            // Check if fallback is enabled
            if (BooleanUtils.isFalse(aiProperties.getFallback().isEnabled())) {
                log.error("Fallback disabled, throwing original exception");
                throw new ServiceException(
                    AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED.formatted(primaryModel.getDisplayName()),
                    ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE,
                    primaryException
                );
            }

            // Try fallback models
            String[] fallbackModelNames = aiProperties.getFallback().getFallbackModels();
            for (String fallbackModelName : fallbackModelNames) {
                try {
                    AIModelEnum fallbackModel = AIModelEnum.valueOf(fallbackModelName);

                    // Skip if it's the same as the primary model that already failed
                    if (Objects.equals(fallbackModel, primaryModel)) {
                        continue;
                    }

                    log.info("Attempting fallback with model: {}", fallbackModel.getDisplayName());
                    List<CreateFlashcardDto> result = attemptGeneration(fallbackModel, promptText, request);
                    log.info("Successfully generated flashcards using fallback model: {}", fallbackModel.getDisplayName());
                    return result;

                } catch (IllegalArgumentException e) {
                    log.warn("Invalid fallback model name in configuration: {}", fallbackModelName);
                } catch (Exception fallbackException) {
                    log.warn("Fallback model {} failed: {}", fallbackModelName, fallbackException.getMessage());
                }
            }

            // All fallback attempts failed
            throw new ServiceException(
                AI_ALL_MODELS_UNAVAILABLE,
                ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE,
                primaryException
            );
        }
    }

    private List<CreateFlashcardDto> attemptGeneration(AIModelEnum model, String promptText, AIGenerateRequestDto request) {
        ChatModel selectedChatModel = modelSelectorService.selectChatModel(model);
        modelSelectorService.validateModelForText(model, request.getText());
        log.debug("Attempting generation with AI model: {} ({})", model.getDisplayName(), model.getModelId());

        // Create ChatOptions with the specific model ID
        ChatOptions chatOptions = createChatOptions(model);
        Prompt prompt = new Prompt(promptText, chatOptions);

        String response = selectedChatModel.call(prompt).getResult().getOutput().getContent();

        log.debug("Received AI response from {}: {}", model.getDisplayName(), response);

        return parseFlashcardResponse(response, request);
    }

    private String buildPrompt(AIGenerateRequestDto request) {
        Map<String, Object> promptVariables = Map.of(
                "text", request.getText(),
                "count", request.getCount()
        );

        PromptTemplate promptTemplate = new PromptTemplate(FLASHCARD_GENERATION_TEMPLATE);
        return promptTemplate.render(promptVariables);
    }

    /**
     * Creates ChatOptions with the specific model ID for the given AI model.
     * This ensures we use the exact model requested, not just the provider's default.
     */
    private ChatOptions createChatOptions(AIModelEnum selectedModel) {
        AIProviderEnum provider = selectedModel.getProvider();

        return switch (provider) {
            case OPENAI -> OpenAiChatOptions.builder()
                    .withModel(selectedModel.getModelId())
                    .withTemperature(DEFAULT_TEMPERATURE)
                    .withMaxTokens(selectedModel.getMaxOutputTokens())
                    .build();

            case ANTHROPIC -> AnthropicChatOptions.builder()
                    .withModel(selectedModel.getModelId())
                    .withTemperature(DEFAULT_TEMPERATURE)
                    .withMaxTokens(selectedModel.getMaxOutputTokens())
                    .build();

            case GOOGLE -> VertexAiGeminiChatOptions.builder()
                    .withModel(selectedModel.getModelId())
                    .withTemperature(DEFAULT_TEMPERATURE)
                    .withMaxOutputTokens(selectedModel.getMaxOutputTokens())
                    .build();
        };
    }

    private List<CreateFlashcardDto> parseFlashcardResponse(String response, AIGenerateRequestDto request) {
        try {
            String cleanResponse = cleanJsonResponse(response);

            // Validate JSON is not empty or incomplete
            if (StringUtils.isBlank(cleanResponse) || BooleanUtils.isFalse(cleanResponse.trim().endsWith("]"))) {
                log.error("AI response appears to be incomplete or empty. Response length: {}, ends with ]: {}",
                    cleanResponse.length(), cleanResponse.trim().endsWith("]"));
                throw new ServiceException(
                    AI_RESPONSE_INCOMPLETE,
                    ErrorCode.SERVICE_AI_GENERATION_ERROR
                );
            }

            List<Map<String, Object>> flashcardMaps = objectMapper.readValue(
                    cleanResponse,
                    new TypeReference<>() {}
            );

            List<CreateFlashcardDto> flashcards = flashcardMaps.stream()
                    .map(flashcardMap -> convertToCreateFlashcardDto(flashcardMap, request))
                    .filter(Objects::nonNull)
                    .toList();

            // Validate we got the expected number of flashcards
            if (flashcards.size() < request.getCount()) {
                log.warn("AI generated {} flashcards but {} were requested. Some flashcards may be invalid or incomplete.",
                    flashcards.size(), request.getCount());
            }

            if (flashcards.isEmpty()) {
                throw new ServiceException(
                    AI_NO_VALID_FLASHCARDS,
                    ErrorCode.SERVICE_AI_GENERATION_ERROR
                );
            }

            log.info("Successfully parsed {} flashcards from AI response (requested: {})",
                flashcards.size(), request.getCount());

            return flashcards;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON. Response length: {}, Error: {}",
                    response.length(), e.getMessage());
            log.debug("Full AI response that failed parsing: {}", response);

            String errorMessage = AI_RESPONSE_PARSE_FAILED;
            if (e.getMessage().contains("Unexpected end-of-input")) {
                errorMessage += AI_RESPONSE_TRUNCATED;
            } else if (e.getMessage().contains("expected close marker")) {
                errorMessage += AI_RESPONSE_MALFORMED;
            }

            throw new ServiceException(
                    errorMessage,
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
            Map<String, Object> frontMap = (Map<String, Object>) flashcardMap.get(JSON_FIELD_FRONT);
            if (Objects.nonNull(frontMap)) {
                builder.front(parseCardContent(frontMap));
            }

            // Parse back content
            Map<String, Object> backMap = (Map<String, Object>) flashcardMap.get(JSON_FIELD_BACK);
            if (Objects.nonNull(backMap)) {
                builder.back(parseCardContent(backMap));
            }

            // Parse other fields
            if (flashcardMap.containsKey(JSON_FIELD_HINT)) {
                builder.hint((String) flashcardMap.get(JSON_FIELD_HINT));
            }

            if (flashcardMap.containsKey(JSON_FIELD_TAGS)) {
                List<String> tags = (List<String>) flashcardMap.get(JSON_FIELD_TAGS);
                builder.tags(tags);
            }

            if (flashcardMap.containsKey(JSON_FIELD_DIFFICULTY)) {
                String difficultyStr = (String) flashcardMap.get(JSON_FIELD_DIFFICULTY);
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

        if (contentMap.containsKey(JSON_FIELD_TEXT)) {
            builder.text((String) contentMap.get(JSON_FIELD_TEXT));
        }

        if (contentMap.containsKey(JSON_FIELD_TYPE)) {
            String typeStr = (String) contentMap.get(JSON_FIELD_TYPE);
            builder.type(parseContentType(typeStr));
        }

        if (contentMap.containsKey(JSON_FIELD_CODE_BLOCKS)) {
            List<Map<String, Object>> codeBlockMaps = (List<Map<String, Object>>) contentMap.get(JSON_FIELD_CODE_BLOCKS);
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

            if (codeBlockMap.containsKey(JSON_FIELD_LANGUAGE)) {
                builder.language((String) codeBlockMap.get(JSON_FIELD_LANGUAGE));
            }

            if (codeBlockMap.containsKey(JSON_FIELD_CODE)) {
                builder.code((String) codeBlockMap.get(JSON_FIELD_CODE));
            }

            if (codeBlockMap.containsKey(JSON_FIELD_FILE_NAME)) {
                builder.fileName((String) codeBlockMap.get(JSON_FIELD_FILE_NAME));
            }

            if (codeBlockMap.containsKey(JSON_FIELD_HIGHLIGHTED)) {
                builder.highlighted(BooleanUtils.isTrue((Boolean) codeBlockMap.get(JSON_FIELD_HIGHLIGHTED)));
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