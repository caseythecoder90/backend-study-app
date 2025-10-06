package com.flashcards.backend.flashcards.service.ai;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.service.ai.strategy.AIImageOperationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ALL_MODELS_UNAVAILABLE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Central orchestrator for AI image generation operations.
 * Handles model selection, fallback logic, error handling, and delegates to image strategies.
 * Separate from AIExecutionService because it uses ImageModel API instead of ChatModel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIImageGenerationService {

    private final OpenAiImageModel openAiImageModel; // DALL-E
    // Future: Inject other image models (Stability AI, etc.)

    private final AIConfigProperties aiProperties;

    /**
     * Execute an AI image generation operation using the specified strategy.
     *
     * @param strategy The operation strategy to execute
     * @param input The input for the operation
     * @param model The AI model to use (or null for default)
     * @param <I> Input type
     * @param <O> Output type
     * @return The operation result
     */
    public <I, O> O executeImageOperation(AIImageOperationStrategy<I, O> strategy, I input, AIModelEnum model) {
        // Validate input
        strategy.validateInput(input);

        // Use default model if not specified
        AIModelEnum selectedModel = model != null ? model : strategy.getDefaultModel();

        log.info("Executing {} with model: {}", strategy.getOperationName(), selectedModel.getDisplayName());

        // Execute with fallback support
        return executeWithFallback(strategy, input, selectedModel);
    }

    /**
     * Execute the operation with automatic fallback to alternative models on failure.
     */
    private <I, O> O executeWithFallback(AIImageOperationStrategy<I, O> strategy, I input, AIModelEnum primaryModel) {
        try {
            return attemptExecution(strategy, input, primaryModel);
        } catch (Exception primaryException) {
            log.warn("Primary model {} failed for {}: {}",
                primaryModel.getDisplayName(), strategy.getOperationName(), primaryException.getMessage());

            // Check if fallback is enabled
            if (isFalse(aiProperties.getFallback().isEnabled())) {
                log.error("Fallback disabled, throwing original exception");
                throw new ServiceException(
                    AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED.formatted(primaryModel.getDisplayName()),
                    ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE,
                    primaryException
                );
            }

            // Try fallback models
            return tryFallbackModels(strategy, input, primaryModel, primaryException);
        }
    }

    /**
     * Attempt execution with a single model.
     */
    private <I, O> O attemptExecution(AIImageOperationStrategy<I, O> strategy, I input, AIModelEnum model) {
        // Select the appropriate image model
        ImageModel imageModel = selectImageModel(model);

        // Build the image prompt
        ImagePrompt imagePrompt = strategy.buildImagePrompt(input);

        // Call the image model
        ImageResponse imageResponse = imageModel.call(imagePrompt);

        log.debug("Received image generation response from {} for {}",
            model.getDisplayName(), strategy.getOperationName());

        // Parse and return the response
        return strategy.parseResponse(imageResponse, input);
    }

    /**
     * Try fallback models in sequence.
     */
    private <I, O> O tryFallbackModels(
            AIImageOperationStrategy<I, O> strategy,
            I input,
            AIModelEnum primaryModel,
            Exception primaryException) {

        String[] fallbackModelNames = aiProperties.getFallback().getFallbackModels();

        for (String fallbackModelName : fallbackModelNames) {
            try {
                AIModelEnum fallbackModel = AIModelEnum.valueOf(fallbackModelName);

                // Skip if same as primary or doesn't support image generation
                if (Objects.equals(fallbackModel, primaryModel) ||
                    !supportsImageGeneration(fallbackModel)) {
                    continue;
                }

                log.info("Attempting fallback with model: {} for {}",
                    fallbackModel.getDisplayName(), strategy.getOperationName());

                return attemptExecution(strategy, input, fallbackModel);

            } catch (IllegalArgumentException e) {
                log.warn("Invalid fallback model name in configuration: {}", fallbackModelName);
            } catch (Exception fallbackException) {
                log.warn("Fallback model {} failed for {}: {}",
                    fallbackModelName, strategy.getOperationName(), fallbackException.getMessage());
            }
        }

        // All fallback attempts failed
        throw new ServiceException(
            AI_ALL_MODELS_UNAVAILABLE,
            ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE,
            primaryException
        );
    }

    /**
     * Select the appropriate ImageModel based on the AI model enum.
     */
    private ImageModel selectImageModel(AIModelEnum model) {
        return switch (model.getProvider()) {
            case OPENAI -> openAiImageModel;
            // Future: Add other providers
            // case STABILITY_AI -> stabilityAiImageModel;
            default -> throw new ServiceException(
                "Image generation not supported for provider: " + model.getProvider(),
                ErrorCode.SERVICE_AI_MODEL_ERROR
            );
        };
    }

    /**
     * Check if the model supports image generation.
     */
    private boolean supportsImageGeneration(AIModelEnum model) {
        // Currently only OpenAI DALL-E models support image generation
        // Gemini does NOT support image generation (only vision input)
        return model.getModelId().contains("dall-e");
    }
}