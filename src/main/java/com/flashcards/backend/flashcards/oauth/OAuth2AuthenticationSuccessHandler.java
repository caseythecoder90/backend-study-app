package com.flashcards.backend.flashcards.oauth;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import com.flashcards.backend.flashcards.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_ERROR_AUTHENTICATION_FAILED;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PARAM_ERROR;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PARAM_TOKEN;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PARAM_TYPE;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_TYPE_VALUE;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_USERNAME_FALLBACK_PREFIX;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_OAUTH_EMAIL_NOT_FOUND;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserDao userDao;
    private final JwtService jwtService;

    @Value("${oauth.success-redirect-url}")
    private String successRedirectUrl;

    @Value("${oauth.failure-redirect-url}")
    private String failureRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("OAuth2 authentication successful");

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            try {
                String targetUrl = processOAuth2Authentication(oauthToken);
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } catch (Exception e) {
                log.error("OAuth2 authentication processing failed: {}", e.getMessage(), e);
                String errorUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                        .queryParam(OAUTH_PARAM_ERROR, OAUTH_ERROR_AUTHENTICATION_FAILED)
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
            }
        } else {
            log.error("Authentication is not OAuth2AuthenticationToken");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private String processOAuth2Authentication(OAuth2AuthenticationToken oauthToken) {
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.debug("Processing OAuth2 authentication for provider: {}", registrationId);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        if (isBlank(userInfo.getEmail())) {
            throw new ServiceException(AUTH_OAUTH_EMAIL_NOT_FOUND, ErrorCode.SERVICE_VALIDATION_ERROR);
        }

        User user = findOrCreateUser(userInfo, registrationId);
        String jwtToken = jwtService.generateToken(user);

        log.debug("OAuth2 authentication successful for user: {}", user.getUsername());

        return UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam(OAUTH_PARAM_TOKEN, jwtToken)
                .queryParam(OAUTH_PARAM_TYPE, OAUTH_TYPE_VALUE)
                .build().toUriString();
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo, String provider) {
        Optional<User> existingOAuthUser = userDao.findByOauthProviderAndOauthId(provider, userInfo.getId());
        if (existingOAuthUser.isPresent()) {
            User user = existingOAuthUser.get();
            updateUserFromOAuth(user, userInfo);
            return userDao.save(user);
        }

        Optional<User> existingEmailUser = userDao.findByEmail(userInfo.getEmail());
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();
            user.setOauthProvider(provider);
            user.setOauthId(userInfo.getId());
            updateUserFromOAuth(user, userInfo);
            return userDao.save(user);
        }

        return createNewOAuthUser(userInfo, provider);
    }

    private User createNewOAuthUser(OAuth2UserInfo userInfo, String provider) {
        String username = generateUniqueUsername(userInfo);
        LocalDateTime now = LocalDateTime.now();

        User newUser = User.builder()
                .username(username)
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .oauthProvider(provider)
                .oauthId(userInfo.getId())
                .profileImageUrl(userInfo.getImageUrl())
                .roles(Set.of(Role.USER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .totpEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .build();

        log.debug("Creating new OAuth user: {}", username);
        return userDao.save(newUser);
    }

    private void updateUserFromOAuth(User user, OAuth2UserInfo userInfo) {
        if (isNotBlank(userInfo.getFirstName())) {
            user.setFirstName(userInfo.getFirstName());
        }
        if (isNotBlank(userInfo.getLastName())) {
            user.setLastName(userInfo.getLastName());
        }
        if (isNotBlank(userInfo.getImageUrl())) {
            user.setProfileImageUrl(userInfo.getImageUrl());
        }
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    private String generateUniqueUsername(OAuth2UserInfo userInfo) {
        String baseUsername = extractBaseUsername(userInfo);
        String username = baseUsername;
        int counter = 1;

        while (isTrue(userDao.existsByUsername(username))) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private String extractBaseUsername(OAuth2UserInfo userInfo) {
        if (isNotBlank(userInfo.getName())) {
            return userInfo.getName().replaceAll("\\s+", "").toLowerCase();
        }
        if (isNotBlank(userInfo.getEmail())) {
            return userInfo.getEmail().split("@")[0].toLowerCase();
        }
        return OAUTH_USERNAME_FALLBACK_PREFIX + System.currentTimeMillis();
    }
}