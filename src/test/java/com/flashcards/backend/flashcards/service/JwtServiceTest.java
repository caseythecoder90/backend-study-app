package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private SecretKey secretKey;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-that-is-long-enough-for-hmac-sha-256-algorithm";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour
    private static final String TEST_ISSUER = "flashcards-test";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION_MS, TEST_ISSUER);
        secretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    }

    @Test
    @DisplayName("generateToken with valid user returns token")
    void generateToken_ValidUser_ReturnsToken() {
        // Given
        User user = createTestUser();

        // When
        String token = jwtService.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("generateToken with null user throws ServiceException")
    void generateToken_NullUser_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> jwtService.generateToken(null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    @DisplayName("generateToken with user having null ID throws ServiceException")
    void generateToken_UserWithNullId_ThrowsServiceException() {
        // Given
        User user = createTestUser();
        user.setId(null);

        // When / Then
        assertThatThrownBy(() -> jwtService.generateToken(user))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User ID cannot be null");
    }

    @Test
    @DisplayName("generateToken includes all required claims")
    void generateToken_IncludesAllClaims() {
        // Given
        User user = createTestUser();

        // When
        String token = jwtService.generateToken(user);
        Claims claims = parseToken(token);

        // Then
        assertThat(claims.getSubject()).isEqualTo(user.getId());
        assertThat(claims.get("username", String.class)).isEqualTo(user.getUsername());
        assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
        assertThat(claims.get("totpEnabled", Boolean.class)).isEqualTo(user.isTotpEnabled());
        assertThat(claims.getIssuer()).isEqualTo(TEST_ISSUER);

        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");
        assertThat(authorities).contains("ROLE_USER");
    }

    @Test
    @DisplayName("extractUserId from valid token returns user ID")
    void extractUserId_ValidToken_ReturnsUserId() {
        // Given
        User user = createTestUser();
        String token = jwtService.generateToken(user);

        // When
        String extractedUserId = jwtService.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("extractUsername from valid token returns username")
    void extractUsername_ValidToken_ReturnsUsername() {
        // Given
        User user = createTestUser();
        String token = jwtService.generateToken(user);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("extractEmail from valid token returns email")
    void extractEmail_ValidToken_ReturnsEmail() {
        // Given
        User user = createTestUser();
        String token = jwtService.generateToken(user);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("extractAuthorities from valid token returns authorities")
    void extractAuthorities_ValidToken_ReturnsAuthorities() {
        // Given
        User user = createTestUser();
        String token = jwtService.generateToken(user);

        // When
        List<String> authorities = jwtService.extractAuthorities(token);

        // Then
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).contains("ROLE_USER");
    }

    @Test
    @DisplayName("isTokenValid returns true for valid non-expired token")
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Given
        User user = createTestUser();
        String token = jwtService.generateToken(user);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false for expired token")
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Given
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, 1, TEST_ISSUER); // 1ms expiration
        User user = createTestUser();
        String token = shortLivedJwtService.generateToken(user);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for invalid token")
    void isTokenValid_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired returns true for expired token")
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        // Given
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, 1, TEST_ISSUER);
        User user = createTestUser();
        String token = shortLivedJwtService.generateToken(user);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isTrue();
    }

    // Helper Methods

    private User createTestUser() {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id("test-user-id-123")
                .username("testuser")
                .email("test@example.com")
                .password("encrypted-password")
                .firstName("Test")
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

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
