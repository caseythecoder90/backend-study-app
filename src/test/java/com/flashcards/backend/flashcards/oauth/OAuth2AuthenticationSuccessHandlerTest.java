package com.flashcards.backend.flashcards.oauth;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.enums.Role;
import com.flashcards.backend.flashcards.model.User;
import com.flashcards.backend.flashcards.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_AVATAR_URL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_EMAIL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_ID;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_EMAIL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_FAMILY_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_GIVEN_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_PICTURE;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_SUB;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PROVIDER_GITHUB;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PROVIDER_GOOGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OAuth2AuthenticationSuccessHandler Tests")
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private UserDao userDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler handler;

    private static final String TEST_JWT_TOKEN = "test-jwt-token-123";
    private static final String SUCCESS_REDIRECT_URL = "http://localhost:3000/auth/success";
    private static final String FAILURE_REDIRECT_URL = "http://localhost:3000/auth/error";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "successRedirectUrl", SUCCESS_REDIRECT_URL);
        ReflectionTestUtils.setField(handler, "failureRedirectUrl", FAILURE_REDIRECT_URL);
        handler.setRedirectStrategy(redirectStrategy);

        when(jwtService.generateToken(any(User.class))).thenReturn(TEST_JWT_TOKEN);
    }

    @Nested
    @DisplayName("New User Creation Tests")
    class NewUserCreationTests {

        @Test
        @DisplayName("onAuthenticationSuccess with new Google user creates user and redirects")
        void onAuthenticationSuccess_NewGoogleUser_CreatesUserAndRedirects() throws Exception {
            // Given
            Map<String, Object> attributes = createGoogleAttributes(
                    "google-id-123",
                    "John Doe",
                    "john.doe@gmail.com",
                    "John",
                    "Doe",
                    "https://example.com/photo.jpg"
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GOOGLE, attributes);

            when(userDao.findByOauthProviderAndOauthId(OAUTH_PROVIDER_GOOGLE, "google-id-123"))
                    .thenReturn(Optional.empty());
            when(userDao.findByEmail("john.doe@gmail.com")).thenReturn(Optional.empty());
            when(userDao.existsByUsername("johndoe")).thenReturn(false);
            when(userDao.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("new-user-id");
                return user;
            });

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains(SUCCESS_REDIRECT_URL));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("token=" + TEST_JWT_TOKEN));
        }

        @Test
        @DisplayName("onAuthenticationSuccess with new GitHub user creates user and redirects")
        void onAuthenticationSuccess_NewGitHubUser_CreatesUserAndRedirects() throws Exception {
            // Given
            Map<String, Object> attributes = createGitHubAttributes(
                    12345,
                    "Jane Smith",
                    "jane.smith@example.com",
                    "https://avatars.githubusercontent.com/u/12345"
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GITHUB, attributes);

            when(userDao.findByOauthProviderAndOauthId(OAUTH_PROVIDER_GITHUB, "12345"))
                    .thenReturn(Optional.empty());
            when(userDao.findByEmail("jane.smith@example.com")).thenReturn(Optional.empty());
            when(userDao.existsByUsername("janesmith")).thenReturn(false);
            when(userDao.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("new-github-user-id");
                return user;
            });

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains(SUCCESS_REDIRECT_URL));
        }

        @Test
        @DisplayName("generateUniqueUsername handles collisions by appending counter")
        void generateUniqueUsername_HandlesCollisions() throws Exception {
            // Given
            Map<String, Object> attributes = createGoogleAttributes(
                    "google-id-789",
                    "Test User",
                    "test.user@example.com",
                    "Test",
                    "User",
                    null
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GOOGLE, attributes);

            when(userDao.findByOauthProviderAndOauthId(anyString(), anyString())).thenReturn(Optional.empty());
            when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userDao.existsByUsername("testuser")).thenReturn(true);
            when(userDao.existsByUsername("testuser1")).thenReturn(true);
            when(userDao.existsByUsername("testuser2")).thenReturn(false);
            when(userDao.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("user-with-collision-id");
                assertThat(user.getUsername()).isEqualTo("testuser2");
                return user;
            });

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
        }

        @Test
        @DisplayName("generateUniqueUsername uses email prefix when name is missing")
        void generateUniqueUsername_UsesEmailPrefix() throws Exception {
            // Given
            Map<String, Object> attributes = createGoogleAttributes(
                    "google-id-999",
                    null,  // No name
                    "emailuser@example.com",
                    null,
                    null,
                    null
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GOOGLE, attributes);

            when(userDao.findByOauthProviderAndOauthId(anyString(), anyString())).thenReturn(Optional.empty());
            when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userDao.existsByUsername("emailuser")).thenReturn(false);
            when(userDao.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("email-prefix-user-id");
                assertThat(user.getUsername()).isEqualTo("emailuser");
                return user;
            });

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Existing User Tests")
    class ExistingUserTests {

        @Test
        @DisplayName("onAuthenticationSuccess with existing OAuth user updates user")
        void onAuthenticationSuccess_ExistingOAuthUser_UpdatesUser() throws Exception {
            // Given
            Map<String, Object> attributes = createGoogleAttributes(
                    "google-id-existing",
                    "Updated Name",
                    "existing@example.com",
                    "Updated",
                    "Name",
                    "https://example.com/new-photo.jpg"
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GOOGLE, attributes);

            User existingUser = createExistingUser("existing-user-id", "existinguser", "existing@example.com");
            existingUser.setOauthProvider(OAUTH_PROVIDER_GOOGLE);
            existingUser.setOauthId("google-id-existing");

            when(userDao.findByOauthProviderAndOauthId(OAUTH_PROVIDER_GOOGLE, "google-id-existing"))
                    .thenReturn(Optional.of(existingUser));
            when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains(SUCCESS_REDIRECT_URL));
        }

        @Test
        @DisplayName("onAuthenticationSuccess with existing email user links OAuth account")
        void onAuthenticationSuccess_ExistingEmailUser_LinksOAuthAccount() throws Exception {
            // Given
            Map<String, Object> attributes = createGoogleAttributes(
                    "google-id-new",
                    "Existing User",
                    "existing@example.com",
                    "Existing",
                    "User",
                    null
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GOOGLE, attributes);

            User existingEmailUser = createExistingUser("email-user-id", "emailuser", "existing@example.com");

            when(userDao.findByOauthProviderAndOauthId(OAUTH_PROVIDER_GOOGLE, "google-id-new"))
                    .thenReturn(Optional.empty());
            when(userDao.findByEmail("existing@example.com")).thenReturn(Optional.of(existingEmailUser));
            when(userDao.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getOauthProvider()).isEqualTo(OAUTH_PROVIDER_GOOGLE);
                assertThat(user.getOauthId()).isEqualTo("google-id-new");
                return user;
            });

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains(SUCCESS_REDIRECT_URL));
        }
    }

    @Nested
    @DisplayName("Provider-Specific Tests")
    class ProviderSpecificTests {

        @Test
        @DisplayName("GitHub provider extracts firstName and lastName from full name")
        void gitHubProvider_ExtractsFirstAndLastName() throws Exception {
            // Given
            Map<String, Object> attributes = createGitHubAttributes(
                    54321,
                    "Alice Marie Johnson",  // Full name with middle name
                    "alice.johnson@example.com",
                    "https://avatars.githubusercontent.com/u/54321"
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GITHUB, attributes);

            when(userDao.findByOauthProviderAndOauthId(anyString(), anyString())).thenReturn(Optional.empty());
            when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userDao.existsByUsername(anyString())).thenReturn(false);
            when(userDao.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("github-name-test-id");
                // GitHub extracts first word as firstName, last word as lastName
                assertThat(user.getFirstName()).isEqualTo("Alice");
                assertThat(user.getLastName()).isEqualTo("Johnson");
                return user;
            });

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(userDao).save(any(User.class));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains(SUCCESS_REDIRECT_URL));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("onAuthenticationSuccess with missing email redirects to error")
        void onAuthenticationSuccess_MissingEmail_RedirectsToError() throws Exception {
            // Given
            Map<String, Object> attributes = createGoogleAttributes(
                    "google-id-no-email",
                    "No Email User",
                    null,  // Missing email
                    "No",
                    "Email",
                    null
            );
            OAuth2AuthenticationToken authToken = createOAuth2Token(OAUTH_PROVIDER_GOOGLE, attributes);

            // When
            handler.onAuthenticationSuccess(request, response, authToken);

            // Then
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains(FAILURE_REDIRECT_URL));
            verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("error="));
        }
    }

    // Helper Methods

    private Map<String, Object> createGoogleAttributes(String id, String name, String email,
                                                        String givenName, String familyName, String picture) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(GOOGLE_ATTR_SUB, id);
        attributes.put(GOOGLE_ATTR_NAME, name);
        attributes.put(GOOGLE_ATTR_EMAIL, email);
        attributes.put(GOOGLE_ATTR_GIVEN_NAME, givenName);
        attributes.put(GOOGLE_ATTR_FAMILY_NAME, familyName);
        attributes.put(GOOGLE_ATTR_PICTURE, picture);
        return attributes;
    }

    private Map<String, Object> createGitHubAttributes(Integer id, String name, String email, String avatarUrl) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(GITHUB_ATTR_ID, id);
        attributes.put(GITHUB_ATTR_NAME, name);
        attributes.put(GITHUB_ATTR_EMAIL, email);
        attributes.put(GITHUB_ATTR_AVATAR_URL, avatarUrl);
        return attributes;
    }

    private OAuth2AuthenticationToken createOAuth2Token(String registrationId, Map<String, Object> attributes) {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        return new OAuth2AuthenticationToken(oAuth2User, null, registrationId);
    }

    private User createExistingUser(String id, String username, String email) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .firstName("Existing")
                .lastName("User")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .totpEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
