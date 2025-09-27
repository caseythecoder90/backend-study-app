package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.enums.AIProviderEnum;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


import static com.flashcards.backend.flashcards.constants.AIConstants.CHARS_PER_TOKEN_ESTIMATE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_PROVIDER_UNKNOWN;
import static com.flashcards.backend.flashcards.exception.ErrorCode.SERVICE_AI_PROVIDER_UNKNOWN;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Slf4j
public class ModelSelectorService {

    private final ChatModel openAiChatModel;
    private final ChatModel anthropicChatModel;
    private final ChatModel vertexAiGeminiChatModel;

    public ModelSelectorService(
            @Qualifier("openAiChatModel") @Autowired(required = false) ChatModel openAiChatModel,
            @Qualifier("anthropicChatModel") @Autowired(required = false) ChatModel anthropicChatModel,
            @Qualifier("vertexAiGeminiChat") @Autowired(required = false) ChatModel vertexAiGeminiChatModel) {
        this.openAiChatModel = openAiChatModel;
        this.anthropicChatModel = anthropicChatModel;
        this.vertexAiGeminiChatModel = vertexAiGeminiChatModel;

        log.info("ModelSelectorService initialized with providers:");
        log.info("  - OpenAI: {} ({})",
            nonNull(openAiChatModel) ? "AVAILABLE" : "NOT_AVAILABLE",
            nonNull(openAiChatModel) ? openAiChatModel.getClass().getSimpleName() : "null");
        log.info("  - Anthropic: {} ({})",
            nonNull(anthropicChatModel) ? "AVAILABLE" : "NOT_AVAILABLE",
            nonNull(anthropicChatModel) ? anthropicChatModel.getClass().getSimpleName() : "null");
        log.info("  - Vertex AI: {} ({})",
            nonNull(vertexAiGeminiChatModel) ? "AVAILABLE" : "NOT_AVAILABLE",
            nonNull(vertexAiGeminiChatModel) ? vertexAiGeminiChatModel.getClass().getSimpleName() : "null");
    }

    /**
     * Selects the appropriate ChatModel based on the AI model.
     * If model is null, defaults to GPT-4O-MINI.
     *
     * @param model The AI model enum
     * @return The ChatModel instance for the specified model's provider
     * @throws ServiceException if the model/provider is not supported or available
     */
    public ChatModel selectChatModel(AIModelEnum model) {

        AIModelEnum selectedModel = nonNull(model) ? model : AIModelEnum.GPT_4O_MINI;

        validateModelForRequest(selectedModel);

        AIProviderEnum provider = selectedModel.getProvider();

        try {
            ChatModel chatModel = switch (provider) {
                case OPENAI -> {
                    log.debug("Selected OpenAI ChatModel for model: {}", selectedModel.getDisplayName());
                    yield openAiChatModel;
                }
                case ANTHROPIC -> {
                    log.debug("Selected Anthropic ChatModel for model: {}", selectedModel.getDisplayName());
                    yield anthropicChatModel;
                }
                case GOOGLE -> {
                    log.debug("Selected Vertex AI Gemini ChatModel for model: {}", selectedModel.getDisplayName());
                    yield vertexAiGeminiChatModel;
                }
            };

            if (isNull(chatModel)) {
                throw new ServiceException(
                    AI_PROVIDER_UNKNOWN.formatted("Provider " + provider.name() + " is not configured"),
                    SERVICE_AI_PROVIDER_UNKNOWN
                );
            }

            return chatModel;

        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            }
            log.error("Failed to select ChatModel for model: {} ({})", selectedModel.getDisplayName(), provider, e);
            throw new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted(selectedModel.getDisplayName()),
                SERVICE_AI_PROVIDER_UNKNOWN,
                e
            );
        }
    }

    /**
     * Checks if a model is available and configured.
     *
     * @param model The AI model enum
     * @return true if the model is available, false otherwise
     */
    public boolean isModelAvailable(AIModelEnum model) {
        if (isNull(model)) {
            return false;
        }

        try {
            AIProviderEnum provider = model.getProvider();
            ChatModel chatModel = switch (provider) {
                case OPENAI -> openAiChatModel;
                case ANTHROPIC -> anthropicChatModel;
                case GOOGLE -> vertexAiGeminiChatModel;
            };
            return isTrue(nonNull(chatModel));
        } catch (Exception e) {
            log.warn("Error checking model availability for {}: {}", model.getDisplayName(), e.getMessage());
            return false;
        }
    }

    /**
     * Validates if a model is suitable for a given request.
     *
     * @param model The AI model enum
     * @throws ServiceException if the model is not available or suitable
     */
    public void validateModelForRequest(AIModelEnum model) {
        if (isFalse(isModelAvailable(model))) {
            throw new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted("Model " + model.getDisplayName() + " is not available"),
                SERVICE_AI_PROVIDER_UNKNOWN
            );
        }
    }

    /**
     * Validates model against text content length.
     *
     * @param model The AI model enum
     * @param text  The text content
     * @throws ServiceException if text is too long for the model
     */
    public void validateModelForText(AIModelEnum model, String text) {
        validateModelForRequest(model);

        if (isNotBlank(text)) {
            int estimatedTokens = text.length() / CHARS_PER_TOKEN_ESTIMATE;
            if (estimatedTokens > model.getMaxContextTokens()) {
                throw new ServiceException(
                    "Text too long for model " + model.getDisplayName() +
                        ". Estimated " + estimatedTokens + " tokens, max " + model.getMaxContextTokens(),
                    SERVICE_AI_PROVIDER_UNKNOWN
                );
            }
        }
    }
}