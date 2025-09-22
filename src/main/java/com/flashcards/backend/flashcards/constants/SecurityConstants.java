package com.flashcards.backend.flashcards.constants;

public class SecurityConstants {

    // Authentication Endpoints
    public static final String[] AUTH_ENDPOINTS = {
            "/api/auth/**"
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

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}