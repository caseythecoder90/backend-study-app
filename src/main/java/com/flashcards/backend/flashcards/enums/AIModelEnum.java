package com.flashcards.backend.flashcards.enums;

import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_35_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_3_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_3_OPUS;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_3_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GEMINI_15_FLASH;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GEMINI_15_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GEMINI_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GEMINI_PRO_VISION;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_35_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_OPUS_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_CLAUDE_SONNET_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GPT_35_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GPT_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GPT_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GPT_4O;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GPT_4O_MINI;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_GPT_4_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.CONTEXT_TOKENS_O1_PREVIEW;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_35_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_3_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_3_OPUS;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_3_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GEMINI_15_FLASH;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GEMINI_15_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GEMINI_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GEMINI_PRO_VISION;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GPT_35_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_35_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_OPUS_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_CLAUDE_SONNET_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GPT_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GPT_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GPT_4O;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GPT_4O_MINI;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_GPT_4_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.DISPLAY_O1_PREVIEW;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_35_HAIKU_20241022;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_35_SONNET_20241022;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_3_HAIKU_20240307;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_3_OPUS_20240229;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_3_SONNET_20240229;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_OPUS_4_1_20250805;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_CLAUDE_SONNET_4_20250514;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GEMINI_15_FLASH;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GEMINI_15_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GEMINI_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GPT_35_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GPT_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GPT_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GPT_4O;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GPT_4O_MINI;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_GPT_4_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.MODEL_O1_PREVIEW;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_35_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_3_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_3_OPUS;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_3_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GEMINI_15_FLASH;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GEMINI_15_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GEMINI_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GEMINI_PRO_VISION;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_35_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_OPUS_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_CLAUDE_SONNET_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GPT_35_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GPT_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GPT_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GPT_4O;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GPT_4O_MINI;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_GPT_4_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.OUTPUT_TOKENS_O1_PREVIEW;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_35_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_3_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_3_OPUS;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_3_SONNET;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GEMINI_15_FLASH;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GEMINI_15_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GEMINI_PRO;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GEMINI_PRO_VISION;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_35_HAIKU;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_OPUS_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_CLAUDE_SONNET_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GPT_35_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GPT_4;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GPT_4_1;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GPT_4O;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GPT_4O_MINI;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_GPT_4_TURBO;
import static com.flashcards.backend.flashcards.constants.AIConstants.SUPPORTS_VISION_O1_PREVIEW;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_PROVIDER_UNKNOWN;
import static com.flashcards.backend.flashcards.exception.ErrorCode.SERVICE_AI_PROVIDER_UNKNOWN;



@Getter
public enum AIModelEnum {
    // OpenAI Models
    GPT_4O(MODEL_GPT_4O, AIProviderEnum.OPENAI, DISPLAY_GPT_4O, SUPPORTS_VISION_GPT_4O, CONTEXT_TOKENS_GPT_4O, OUTPUT_TOKENS_GPT_4O),
    GPT_4O_MINI(MODEL_GPT_4O_MINI, AIProviderEnum.OPENAI, DISPLAY_GPT_4O_MINI, SUPPORTS_VISION_GPT_4O_MINI, CONTEXT_TOKENS_GPT_4O_MINI, OUTPUT_TOKENS_GPT_4O_MINI),
    GPT_4_1(MODEL_GPT_4_1, AIProviderEnum.OPENAI, DISPLAY_GPT_4_1, SUPPORTS_VISION_GPT_4_1, CONTEXT_TOKENS_GPT_4_1, OUTPUT_TOKENS_GPT_4_1),
    GPT_4_TURBO(MODEL_GPT_4_TURBO, AIProviderEnum.OPENAI, DISPLAY_GPT_4_TURBO, SUPPORTS_VISION_GPT_4_TURBO, CONTEXT_TOKENS_GPT_4_TURBO, OUTPUT_TOKENS_GPT_4_TURBO),
    GPT_4(MODEL_GPT_4, AIProviderEnum.OPENAI, DISPLAY_GPT_4, SUPPORTS_VISION_GPT_4, CONTEXT_TOKENS_GPT_4, OUTPUT_TOKENS_GPT_4),
    O1_PREVIEW(MODEL_O1_PREVIEW, AIProviderEnum.OPENAI, DISPLAY_O1_PREVIEW, SUPPORTS_VISION_O1_PREVIEW, CONTEXT_TOKENS_O1_PREVIEW, OUTPUT_TOKENS_O1_PREVIEW),
    GPT_3_5_TURBO(MODEL_GPT_35_TURBO, AIProviderEnum.OPENAI, DISPLAY_GPT_35_TURBO, SUPPORTS_VISION_GPT_35_TURBO, CONTEXT_TOKENS_GPT_35_TURBO, OUTPUT_TOKENS_GPT_35_TURBO),

    // Anthropic Claude Models
    CLAUDE_SONNET_4(MODEL_CLAUDE_SONNET_4_20250514, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_SONNET_4, SUPPORTS_VISION_CLAUDE_SONNET_4, CONTEXT_TOKENS_CLAUDE_SONNET_4, OUTPUT_TOKENS_CLAUDE_SONNET_4),
    CLAUDE_OPUS_4_1(MODEL_CLAUDE_OPUS_4_1_20250805, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_OPUS_4_1, SUPPORTS_VISION_CLAUDE_OPUS_4_1, CONTEXT_TOKENS_CLAUDE_OPUS_4_1, OUTPUT_TOKENS_CLAUDE_OPUS_4_1),
    CLAUDE_3_5_SONNET(MODEL_CLAUDE_35_SONNET_20241022, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_35_SONNET, SUPPORTS_VISION_CLAUDE_35_SONNET, CONTEXT_TOKENS_CLAUDE_35_SONNET, OUTPUT_TOKENS_CLAUDE_35_SONNET),
    CLAUDE_3_5_HAIKU(MODEL_CLAUDE_35_HAIKU_20241022, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_35_HAIKU, SUPPORTS_VISION_CLAUDE_35_HAIKU, CONTEXT_TOKENS_CLAUDE_35_HAIKU, OUTPUT_TOKENS_CLAUDE_35_HAIKU),
    CLAUDE_3_OPUS(MODEL_CLAUDE_3_OPUS_20240229, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_3_OPUS, SUPPORTS_VISION_CLAUDE_3_OPUS, CONTEXT_TOKENS_CLAUDE_3_OPUS, OUTPUT_TOKENS_CLAUDE_3_OPUS),
    CLAUDE_3_SONNET(MODEL_CLAUDE_3_SONNET_20240229, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_3_SONNET, SUPPORTS_VISION_CLAUDE_3_SONNET, CONTEXT_TOKENS_CLAUDE_3_SONNET, OUTPUT_TOKENS_CLAUDE_3_SONNET),
    CLAUDE_3_HAIKU(MODEL_CLAUDE_3_HAIKU_20240307, AIProviderEnum.ANTHROPIC, DISPLAY_CLAUDE_3_HAIKU, SUPPORTS_VISION_CLAUDE_3_HAIKU, CONTEXT_TOKENS_CLAUDE_3_HAIKU, OUTPUT_TOKENS_CLAUDE_3_HAIKU),

    // Google Vertex AI Gemini Models
    GEMINI_PRO(MODEL_GEMINI_PRO, AIProviderEnum.GOOGLE, DISPLAY_GEMINI_PRO, SUPPORTS_VISION_GEMINI_PRO, CONTEXT_TOKENS_GEMINI_PRO, OUTPUT_TOKENS_GEMINI_PRO),
    GEMINI_PRO_VISION("gemini-pro-vision", AIProviderEnum.GOOGLE, DISPLAY_GEMINI_PRO_VISION, SUPPORTS_VISION_GEMINI_PRO_VISION, CONTEXT_TOKENS_GEMINI_PRO_VISION, OUTPUT_TOKENS_GEMINI_PRO_VISION),
    GEMINI_1_5_PRO(MODEL_GEMINI_15_PRO, AIProviderEnum.GOOGLE, DISPLAY_GEMINI_15_PRO, SUPPORTS_VISION_GEMINI_15_PRO, CONTEXT_TOKENS_GEMINI_15_PRO, OUTPUT_TOKENS_GEMINI_15_PRO),
    GEMINI_1_5_FLASH(MODEL_GEMINI_15_FLASH, AIProviderEnum.GOOGLE, DISPLAY_GEMINI_15_FLASH, SUPPORTS_VISION_GEMINI_15_FLASH, CONTEXT_TOKENS_GEMINI_15_FLASH, OUTPUT_TOKENS_GEMINI_15_FLASH);

    private final String modelId;
    private final AIProviderEnum provider;
    private final String displayName;
    private final boolean supportsVision;
    private final int maxContextTokens;
    private final int maxOutputTokens;

    AIModelEnum(String modelId, AIProviderEnum provider, String displayName,
                boolean supportsVision, int maxContextTokens, int maxOutputTokens) {
        this.modelId = modelId;
        this.provider = provider;
        this.displayName = displayName;
        this.supportsVision = supportsVision;
        this.maxContextTokens = maxContextTokens;
        this.maxOutputTokens = maxOutputTokens;
    }

    /**
     * Find AIModelEnum by model ID string
     */
    public static AIModelEnum fromModelId(String modelId) {
        if (StringUtils.isBlank(modelId)) {
            throw new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted("Model ID cannot be null or blank"),
                SERVICE_AI_PROVIDER_UNKNOWN
            );
        }

        return Arrays.stream(values())
            .filter(model -> model.getModelId().equalsIgnoreCase(modelId.trim()))
            .findFirst()
            .orElseThrow(() -> new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted(modelId),
                SERVICE_AI_PROVIDER_UNKNOWN
            ));
    }

    /**
     * Find AIModelEnum by enum name
     */
    public static AIModelEnum fromName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted("Model name cannot be null or blank"),
                SERVICE_AI_PROVIDER_UNKNOWN
            );
        }

        try {
            return valueOf(name.toUpperCase().replace("-", "_").replace(".", "_"));
        } catch (IllegalArgumentException e) {
            throw new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted(name),
                SERVICE_AI_PROVIDER_UNKNOWN,
                e
            );
        }
    }

    /**
     * Get all models for a specific provider
     */
    public static AIModelEnum[] getModelsForProvider(AIProviderEnum provider) {
        if (Objects.isNull(provider)) {
            return new AIModelEnum[0];
        }

        return Arrays.stream(values())
            .filter(model -> Objects.equals(model.getProvider(), provider))
            .toArray(AIModelEnum[]::new);
    }

    /**
     * Get default model for a provider
     */
    public static AIModelEnum getDefaultForProvider(AIProviderEnum provider) {
        return switch (provider) {
            case OPENAI -> GPT_4O_MINI;
            case ANTHROPIC -> CLAUDE_SONNET_4;
            case GOOGLE -> GEMINI_1_5_FLASH;
        };
    }

    @Override
    public String toString() {
        return displayName + " (" + modelId + ")";
    }
}