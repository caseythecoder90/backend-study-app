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

    // Controller Layer Error Codes
    CONTROLLER_BAD_REQUEST("CTL_001", "Bad request"),
    CONTROLLER_UNAUTHORIZED("CTL_002", "Unauthorized"),
    CONTROLLER_FORBIDDEN("CTL_003", "Forbidden"),
    CONTROLLER_NOT_FOUND("CTL_004", "Resource not found");

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