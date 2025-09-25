package com.flashcards.backend.flashcards.exception;

public enum ErrorCode {
    // DAO Layer Error Codes
    DAO_SAVE_ERROR("DAO_001", "Failed to save entity"),
    DAO_UPDATE_ERROR("DAO_002", "Failed to update entity"),
    DAO_DELETE_ERROR("DAO_003", "Failed to delete entity"),
    DAO_FIND_ERROR("DAO_004", "Failed to find entity"),
    DAO_DUPLICATE_ERROR("DAO_005", "Duplicate entity found"),
    DAO_CONNECTION_ERROR("DAO_006", "Database connection error"),

    // Service Layer Error Codes
    SERVICE_VALIDATION_ERROR("SVC_001", "Validation failed"),
    SERVICE_NOT_FOUND("SVC_002", "Entity not found"),
    SERVICE_DUPLICATE_ERROR("SVC_003", "Duplicate entity exists"),
    SERVICE_BUSINESS_LOGIC_ERROR("SVC_004", "Business logic error"),
    SERVICE_AUTHORIZATION_ERROR("SVC_005", "Authorization failed"),
    SERVICE_AI_GENERATION_ERROR("SVC_006", "AI generation failed"),
    SERVICE_AI_SERVICE_UNAVAILABLE("SVC_007", "AI service unavailable"),
    SERVICE_AI_RATE_LIMIT_EXCEEDED("SVC_008", "AI rate limit exceeded"),
    SERVICE_AI_INVALID_CONTENT("SVC_009", "AI invalid content"),
    SERVICE_AI_MODEL_ERROR("SVC_010", "AI model error"),
    SERVICE_AI_QUOTA_EXCEEDED("SVC_011", "AI quota exceeded"),
    SERVICE_AI_INVALID_IMAGE("SVC_012", "AI invalid image"),
    SERVICE_AI_PROCESSING_TIMEOUT("SVC_013", "AI processing timeout"),

    // Controller Layer Error Codes
    CONTROLLER_BAD_REQUEST("CTL_001", "Bad request"),
    CONTROLLER_UNAUTHORIZED("CTL_002", "Unauthorized"),
    CONTROLLER_FORBIDDEN("CTL_003", "Forbidden"),
    CONTROLLER_NOT_FOUND("CTL_004", "Resource not found"),

    // Authentication Error Codes
    AUTH_INVALID_CREDENTIALS("AUTH_001", "Invalid credentials"),
    AUTH_TOKEN_INVALID("AUTH_002", "Invalid token"),
    AUTH_TOKEN_EXPIRED("AUTH_003", "Token expired"),
    AUTH_PASSWORD_INVALID("AUTH_004", "Password validation failed"),
    AUTH_USER_DISABLED("AUTH_005", "User account disabled"),
    AUTH_TOTP_INVALID("AUTH_006", "Invalid TOTP code"),
    AUTH_TOTP_REQUIRED("AUTH_007", "TOTP verification required"),
    AUTH_RECOVERY_CODE_INVALID("AUTH_008", "Invalid recovery code"),
    AUTH_RECOVERY_CODES_EXHAUSTED("AUTH_009", "All recovery codes have been used"),
    AUTH_RECOVERY_CODES_NOT_ENABLED("AUTH_010", "Recovery codes are not enabled"),

    // Operations Error Codes
    ENCRYPTION_ERROR("OPS_001", "Encryption failed"),
    DECRYPTION_ERROR("OPS_002", "Decryption failed"),
    INVALID_INPUT("OPS_003", "Invalid input provided");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}