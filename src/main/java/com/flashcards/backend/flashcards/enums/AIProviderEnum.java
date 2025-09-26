package com.flashcards.backend.flashcards.enums;

import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_PROVIDER_UNKNOWN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@RequiredArgsConstructor
public enum AIProviderEnum {
    OPENAI("openai", "OpenAI", true),
    ANTHROPIC("anthropic", "Anthropic Claude", true),
    GOOGLE("google", "Google Gemini", true);

    private final String code;
    private final String displayName;
    private final boolean available;

    public static AIProviderEnum fromCode(String code) {
        if (isBlank(code)) {
            throw new ServiceException(
                AI_PROVIDER_UNKNOWN.formatted("null or empty"),
                ErrorCode.SERVICE_AI_PROVIDER_UNKNOWN
            );
        }

        for (AIProviderEnum provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }

        throw new ServiceException(
            AI_PROVIDER_UNKNOWN.formatted(code),
            ErrorCode.SERVICE_AI_PROVIDER_UNKNOWN
        );
    }
}