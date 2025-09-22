package com.flashcards.backend.flashcards.constants;

public class ErrorMessages {

    // DAO Layer Error Messages
    public static final String DAO_FIND_BY_ID_ERROR = "Failed to find %s by id: %s";
    public static final String DAO_FIND_BY_FIELD_ERROR = "Failed to find %s by %s: %s";
    public static final String DAO_FIND_ALL_ERROR = "Failed to find all %s";
    public static final String DAO_SAVE_ERROR = "Failed to save %s";
    public static final String DAO_UPDATE_ERROR = "Failed to update %s with id: %s";
    public static final String DAO_DELETE_ERROR = "Failed to delete %s with id: %s";
    public static final String DAO_DELETE_BY_FIELD_ERROR = "Failed to delete %s by %s: %s";
    public static final String DAO_COUNT_ERROR = "Failed to count %s";
    public static final String DAO_COUNT_BY_FIELD_ERROR = "Failed to count %s by %s: %s";
    public static final String DAO_EXISTS_ERROR = "Failed to check %s existence by %s: %s";
    public static final String DAO_ENTITY_NOT_FOUND = "%s not found with id: %s";
    public static final String DAO_ENTITY_NULL = "%s cannot be null";
    public static final String DAO_ID_NULL = "%s ID cannot be null";
    public static final String DAO_DUPLICATE_ENTRY = "Duplicate %s entry found";
    public static final String DAO_SAVE_MULTIPLE_ERROR = "Failed to save multiple %s";

    // Service Layer Error Messages
    public static final String SERVICE_ENTITY_NOT_FOUND = "%s with id %s not found";
    public static final String SERVICE_VALIDATION_FAILED = "Validation failed for %s: %s";
    public static final String SERVICE_DUPLICATE_EXISTS = "%s already exists with %s: %s";
    public static final String SERVICE_BUSINESS_RULE_VIOLATION = "Business rule violation: %s";
    public static final String SERVICE_AUTHORIZATION_FAILED = "User %s is not authorized to %s";
    public static final String SERVICE_AI_GENERATION_FAILED = "Failed to generate flashcards from text: %s";
    public static final String SERVICE_OPERATION_FAILED = "Failed to %s %s";

    // Controller Layer Error Messages
    public static final String CONTROLLER_INVALID_REQUEST = "Invalid request: %s";
    public static final String CONTROLLER_MISSING_PARAMETER = "Missing required parameter: %s";
    public static final String CONTROLLER_INVALID_PARAMETER = "Invalid parameter %s: %s";
    public static final String CONTROLLER_RESOURCE_NOT_FOUND = "Resource %s not found";
    public static final String CONTROLLER_UNAUTHORIZED_ACCESS = "Unauthorized access to %s";
    public static final String CONTROLLER_FORBIDDEN_ACTION = "Action %s is forbidden for current user";

    // Authentication Error Messages
    public static final String AUTH_PASSWORD_NULL_EMPTY = "Password cannot be null or empty";
    public static final String AUTH_USER_NULL = "User cannot be null";
    public static final String AUTH_USER_ID_NULL = "User ID cannot be null";
    public static final String AUTH_TOKEN_INVALID = "Invalid JWT token: %s";
    public static final String AUTH_TOKEN_EXPIRED = "JWT token has expired";
    public static final String AUTH_TOKEN_EXTRACTION_FAILED = "Failed to extract claims from token: %s";
    public static final String AUTH_PASSWORD_VERIFICATION_FAILED = "Password verification failed: null or empty password provided";
    public static final String AUTH_CREDENTIALS_INVALID = "Invalid username or password";
    public static final String AUTH_USER_DISABLED = "User account is disabled";
    public static final String AUTH_TOTP_CODE_INVALID = "Invalid TOTP code";
    public static final String AUTH_TOTP_CODE_REQUIRED = "TOTP code is required for this account";
    public static final String AUTH_TOTP_SECRET_USERNAME_NULL = "Secret and username cannot be null or empty";
    public static final String AUTH_TOTP_QR_GENERATION_FAILED = "Failed to generate QR code: %s";
    public static final String AUTH_TOTP_VERIFICATION_FAILED = "TOTP verification failed: secret or code is null/empty";

    // Error Response Field Names
    public static final String ERROR_FIELD_TIMESTAMP = "timestamp";
    public static final String ERROR_FIELD_STATUS = "status";
    public static final String ERROR_FIELD_ERROR = "error";
    public static final String ERROR_FIELD_MESSAGE = "message";
    public static final String ERROR_FIELD_PATH = "path";

    // Error Type Values
    public static final String ERROR_TYPE_UNAUTHORIZED = "Unauthorized";
    public static final String ERROR_TYPE_FORBIDDEN = "Forbidden";
    public static final String ERROR_TYPE_BAD_REQUEST = "Bad Request";
    public static final String ERROR_TYPE_NOT_FOUND = "Not Found";
    public static final String ERROR_TYPE_INTERNAL_SERVER_ERROR = "Internal Server Error";

    // Common Entity Names
    public static final String ENTITY_USER = "User";
    public static final String ENTITY_DECK = "Deck";
    public static final String ENTITY_FLASHCARD = "Flashcard";
    public static final String ENTITY_STUDY_SESSION = "Study Session";

    private ErrorMessages() {
        // Private constructor to prevent instantiation
    }
}