package com.flashcards.backend.flashcards.constants;

public class SecurityConstants {

    // Public Authentication Endpoints (no token required)
    public static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/login/recovery",
            "/api/auth/oauth2/providers"
    };

    // Protected Authentication Endpoints (token required)
    public static final String[] PROTECTED_AUTH_ENDPOINTS = {
            "/api/auth/totp/**",
            "/api/auth/recovery-codes/**"
    };

    // Admin Only Endpoints (ADMIN role required)
    public static final String[] ADMIN_ONLY_ENDPOINTS = {
            "/api/users/**",           // All user management endpoints
            "/api/admin/**",           // Dedicated admin endpoints
            "/api/decks/admin/**",     // Admin deck operations
            "/api/flashcards/admin/**" // Admin flashcard operations
    };

    // Swagger/OpenAPI Endpoints
    public static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**"
    };

    // Public Endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
            "/actuator/health",
            "/error",
            "/favicon.ico"
    };

    // Security Headers
    public static final String SECURITY_HEADER_AUTHORIZATION = "Authorization";
    public static final String SECURITY_HEADER_BEARER_PREFIX = "Bearer ";
    public static final int SECURITY_HEADER_BEARER_PREFIX_LENGTH = 7;

    // HTTP Methods
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";

    // CORS Settings
    public static final long CORS_MAX_AGE_SECONDS = 3600L;

    // Swagger Security
    public static final String SWAGGER_SECURITY_SCHEME_NAME = "bearerAuth";
    public static final String SWAGGER_SECURITY_SCHEME_BEARER_FORMAT = "JWT";
    public static final String SWAGGER_SECURITY_SCHEME_DESCRIPTION = "JWT Authorization header using the Bearer scheme";

    // Encryption
    public static final String ENC_PREFIX = "ENC(";
    public static final String ENC_SUFFIX = ")";

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}