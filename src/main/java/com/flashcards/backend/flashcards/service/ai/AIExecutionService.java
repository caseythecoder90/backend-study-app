package com.flashcards.backend.flashcards.service.ai;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.service.ModelSelectorService;
import com.flashcards.backend.flashcards.service.ai.strategy.AIOperationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.flashcards.backend.flashcards.constants.AIConstants.DEFAULT_TEMPERATURE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_ALL_MODELS_UNAVAILABLE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_VISION_NOT_SUPPORTED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Central orchestrator for AI operations.
 * Handles model selection, fallback logic, error handling, and delegates to strategies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIExecutionService {

    private final ModelSelectorService modelSelectorService;
    private final AIConfigProperties aiProperties;

    /**
     * Execute an AI operation using the specified strategy.
     *
     * @param strategy The operation strategy to execute
     * @param input The input for the operation
     * @param model The AI model to use (or null for default)
     * @param <I> Input type
     * @param <O> Output type
     * @return The operation result
     */
    public <I, O> O executeOperation(AIOperationStrategy<I, O> strategy, I input, AIModelEnum model) {
        
        strategy.validateInput(input);
        
        AIModelEnum selectedModel = Objects.nonNull(model) ? model : strategy.getDefaultModel();

        if (strategy.requiresVision() && isFalse(selectedModel.isSupportsVision())) {
            throw new ServiceException(
                    AI_VISION_NOT_SUPPORTED.formatted(selectedModel.getDisplayName()),
                    ErrorCode.SERVICE_AI_MODEL_ERROR
            );
        }

        log.info("Executing {} with model: {}", strategy.getOperationName(), selectedModel.getDisplayName());

        return executeWithFallback(strategy, input, selectedModel);
    }

    /**
     * Execute the operation with automatic fallback to alternative models on failure.
     */
    private <I, O> O executeWithFallback(AIOperationStrategy<I, O> strategy, I input, AIModelEnum primaryModel) {
        try {
            return attemptExecution(strategy, input, primaryModel);
        } catch (Exception primaryException) {
            log.warn("Primary model {} failed for {}: {}",
                primaryModel.getDisplayName(), strategy.getOperationName(), primaryException.getMessage());
            
            if (isFalse(aiProperties.getFallback().isEnabled())) {
                log.error("Fallback disabled, throwing original exception");
                throw new ServiceException(
                    AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED.formatted(primaryModel.getDisplayName()),
                    ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE,
                    primaryException
                );
            }
            
            return tryFallbackModels(strategy, input, primaryModel, primaryException);
        }
    }

    /**
     * Attempt execution with a single model.
     */
    private <I, O> O attemptExecution(AIOperationStrategy<I, O> strategy, I input, AIModelEnum model) {
        
        ChatModel chatModel = modelSelectorService.selectChatModel(model);
        
        Message message = strategy.buildMessage(input);
        
        ChatOptions chatOptions = createChatOptions(model);
        
        Prompt prompt = new Prompt(List.of(message), chatOptions);
        String response = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        log.debug("Received AI response from {} for {}: {}",
            model.getDisplayName(), strategy.getOperationName(), response);

        return strategy.parseResponse(response, input);
    }

    /**
     * Try fallback models in sequence.
     */
    private <I, O> O tryFallbackModels(
            AIOperationStrategy<I, O> strategy,
            I input,
            AIModelEnum primaryModel,
            Exception primaryException) {

        String[] fallbackModelNames = aiProperties.getFallback().getFallbackModels();

        for (String fallbackModelName : fallbackModelNames) {
            try {
                AIModelEnum fallbackModel = AIModelEnum.valueOf(fallbackModelName);
                
                if (Objects.equals(fallbackModel, primaryModel) ||
                    (strategy.requiresVision() && isFalse(fallbackModel.isSupportsVision()))) {
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
        
        throw new ServiceException(
            AI_ALL_MODELS_UNAVAILABLE,
            ErrorCode.SERVICE_AI_SERVICE_UNAVAILABLE,
            primaryException
        );
    }

    /**
     * Create ChatOptions for the specific model.
     */
    private ChatOptions createChatOptions(AIModelEnum selectedModel) {
        return switch (selectedModel.getProvider()) {
            case OPENAI -> OpenAiChatOptions.builder()
                    .model(selectedModel.getModelId())
                    .temperature(DEFAULT_TEMPERATURE)
                    .maxTokens(selectedModel.getMaxOutputTokens())
                    .build();

            case ANTHROPIC -> AnthropicChatOptions.builder()
                    .model(selectedModel.getModelId())
                    .temperature(DEFAULT_TEMPERATURE)
                    .maxTokens(selectedModel.getMaxOutputTokens())
                    .build();

            case GOOGLE -> VertexAiGeminiChatOptions.builder()
                    .model(selectedModel.getModelId())
                    .temperature(DEFAULT_TEMPERATURE)
                    .maxOutputTokens(selectedModel.getMaxOutputTokens())
                    .build();
        };
    }
}