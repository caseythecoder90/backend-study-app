package com.flashcards.backend.flashcards.constants;

public class JwtConstants {

    // JWT Claims
    public static final String JWT_CLAIM_USERNAME = "username";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_AUTHORITIES = "authorities";
    public static final String JWT_CLAIM_TOTP_ENABLED = "totpEnabled";

    // Token Properties
    public static final String JWT_TOKEN_TYPE = "Bearer";
    public static final String JWT_HEADER_PREFIX = "Bearer ";
    public static final String JWT_HEADER_NAME = "Authorization";

    // Default Configuration Values
    public static final String JWT_DEFAULT_SECRET = "mySecretKey123456789012345678901234567890";
    public static final long JWT_DEFAULT_EXPIRATION_MS = 3600000L; // 1 hour
    public static final String JWT_DEFAULT_ISSUER = "flashcards-app";

    private JwtConstants() {}
}