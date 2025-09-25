package com.flashcards.backend.flashcards.oauth;

import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;

import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PROVIDER_GITHUB;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PROVIDER_GOOGLE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_OAUTH_PROVIDER_NOT_SUPPORTED;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case OAUTH_PROVIDER_GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case OAUTH_PROVIDER_GITHUB -> new GitHubOAuth2UserInfo(attributes);
            default -> throw new ServiceException(
                    AUTH_OAUTH_PROVIDER_NOT_SUPPORTED.formatted(registrationId),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        };
    }
}