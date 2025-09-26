package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOKEN_EXTRACTION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOKEN_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_USER_ID_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_USER_NULL;
import static com.flashcards.backend.flashcards.constants.JwtConstants.JWT_CLAIM_AUTHORITIES;
import static com.flashcards.backend.flashcards.constants.JwtConstants.JWT_CLAIM_EMAIL;
import static com.flashcards.backend.flashcards.constants.JwtConstants.JWT_CLAIM_TOTP_ENABLED;
import static com.flashcards.backend.flashcards.constants.JwtConstants.JWT_CLAIM_USERNAME;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final String jwtIssuer;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpirationMs,
            @Value("${jwt.issuer}") String jwtIssuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtIssuer = jwtIssuer;
    }

    public String generateToken(User user) {
        if (nonNull(user) && isNotBlank(user.getId())) {
            Instant now = Instant.now();
            Instant expiration = now.plus(jwtExpirationMs, ChronoUnit.MILLIS);

            Set<String> authorities = user.getRoles().stream()
                    .map(Role::getAuthority)
                    .collect(Collectors.toSet());

            String token = Jwts.builder()
                    .subject(user.getId())
                    .claim(JWT_CLAIM_USERNAME, user.getUsername())
                    .claim(JWT_CLAIM_EMAIL, user.getEmail())
                    .claim(JWT_CLAIM_AUTHORITIES, authorities)
                    .claim(JWT_CLAIM_TOTP_ENABLED, user.isTotpEnabled())
                    .issuer(jwtIssuer)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(secretKey)
                    .compact();

            log.debug("JWT token generated successfully for user: {}", user.getUsername());
            return token;
        }
        throw new ServiceException(
                nonNull(user) ? AUTH_USER_ID_NULL : AUTH_USER_NULL,
                ErrorCode.AUTH_TOKEN_INVALID
        );
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return extractClaims(token).get(JWT_CLAIM_USERNAME, String.class);
    }

    public String extractEmail(String token) {
        return extractClaims(token).get(JWT_CLAIM_EMAIL, String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Claims claims = extractClaims(token);
        return claims.get(JWT_CLAIM_AUTHORITIES, List.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean isValid = isFalse(isTokenExpired(claims));
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return isTokenExpired(extractClaims(token));
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error( "Failed to extract claims from token: {}", e.getMessage());
            throw new ServiceException(
                    AUTH_TOKEN_EXTRACTION_FAILED.formatted(e.getMessage()),
                    ErrorCode.AUTH_TOKEN_INVALID,
                    e
            );
        }
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}