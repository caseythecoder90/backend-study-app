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
    public static final String AUTH_RECOVERY_CODE_INVALID = "Invalid or already used recovery code";
    public static final String AUTH_RECOVERY_CODE_REQUIRED = "Recovery code is required";
    public static final String AUTH_RECOVERY_CODES_EXHAUSTED = "All recovery codes have been used. Please contact support";
    public static final String AUTH_RECOVERY_CODES_NOT_ENABLED = "Recovery codes are not enabled for this account";
    public static final String AUTH_RECOVERY_CODES_GENERATION_FAILED = "Failed to generate recovery codes: %s";
    public static final String AUTH_OAUTH_PROVIDER_NOT_SUPPORTED = "OAuth provider '%s' is not supported";
    public static final String AUTH_OAUTH_EMAIL_NOT_FOUND = "Email not found from OAuth2 provider";
    public static final String AUTH_OAUTH_PROCESSING_FAILED = "OAuth2 authentication processing failed: %s";

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

    // Encryption Error Messages
    public static final String ENCRYPTION_FAILED = "Failed to encrypt text: %s";
    public static final String DECRYPTION_FAILED = "Failed to decrypt text: %s";
    public static final String ENCRYPTION_TEXT_NULL_EMPTY = "Text to encrypt cannot be null or empty";
    public static final String DECRYPTION_TEXT_NULL_EMPTY = "Text to decrypt cannot be null or empty";
    public static final String ENCRYPTION_SERVICE_UNAVAILABLE = "Encryption service is not available";

    // AI Service Error Messages
    public static final String AI_REQUEST_NULL = "AI generation request cannot be null";
    public static final String AI_TEXT_CONTENT_REQUIRED = "Text content is required for AI generation";
    public static final String AI_TEXT_LENGTH_EXCEEDED = "Text content exceeds maximum allowed length of %s";
    public static final String AI_FLASHCARD_COUNT_EXCEEDED = "Flashcard count exceeds maximum allowed of %s";
    public static final String AI_RESPONSE_PARSE_FAILED = "Failed to parse AI-generated flashcard response";
    public static final String AI_FLASHCARD_MAP_CONVERSION_FAILED = "Failed to convert flashcard map to DTO";
    public static final String AI_CODE_BLOCK_PARSE_FAILED = "Failed to parse code block";
    public static final String AI_DIFFICULTY_LEVEL_INVALID = "Invalid difficulty level: %s, defaulting to NOT_SET";
    public static final String AI_CONTENT_TYPE_INVALID = "Invalid content type: %s, defaulting to TEXT_ONLY";
    public static final String AI_SERVICE_UNEXPECTED_ERROR = "Unexpected error in AIService";
    public static final String AI_PROVIDER_UNKNOWN = "Unknown AI provider: %s";
    public static final String AI_MODEL_UNAVAILABLE_FALLBACK_DISABLED = "AI model %s is unavailable and fallback is disabled";
    public static final String AI_ALL_MODELS_UNAVAILABLE = "All AI models (primary and fallbacks) are unavailable";
    public static final String AI_RESPONSE_INCOMPLETE = "AI response is incomplete or malformed. The JSON appears to be truncated.";
    public static final String AI_NO_VALID_FLASHCARDS = "No valid flashcards were generated from the AI response";
    public static final String AI_RESPONSE_TRUNCATED = " - the response appears to be incomplete or truncated";
    public static final String AI_RESPONSE_MALFORMED = " - the JSON structure is malformed or incomplete";
    public static final String AI_PRIMARY_MODEL_FAILED = "Primary model %s failed: %s";
    public static final String AI_FALLBACK_DISABLED = "Fallback disabled, throwing original exception";
    public static final String AI_FALLBACK_MODEL_INVALID = "Invalid fallback model name in configuration: %s";
    public static final String AI_FALLBACK_MODEL_FAILED = "Fallback model %s failed: %s";
    public static final String AI_ATTEMPTING_FALLBACK = "Attempting fallback with model: %s";
    public static final String AI_FALLBACK_SUCCESS = "Successfully generated flashcards using fallback model: %s";
    public static final String AI_FLASHCARDS_INCOMPLETE = "AI generated %s flashcards but %s were requested. Some flashcards may be invalid or incomplete.";
    public static final String AI_FLASHCARDS_PARSED = "Successfully parsed %s flashcards from AI response (requested: %s)";

    // AI Error Detection Keywords
    public static final String AI_ERROR_RATE_LIMIT = "rate limit";
    public static final String AI_ERROR_TOO_MANY_REQUESTS = "too many requests";
    public static final String AI_ERROR_QUOTA = "quota";
    public static final String AI_ERROR_BILLING = "billing";
    public static final String AI_ERROR_TIMEOUT = "timeout";
    public static final String AI_ERROR_TIMED_OUT = "timed out";
    public static final String AI_ERROR_UNAVAILABLE = "unavailable";
    public static final String AI_ERROR_SERVICE = "service";
    public static final String AI_ERROR_MODEL = "model";
    public static final String AI_ERROR_INVALID = "invalid";

    // Configuration Error Messages
    public static final String CONFIG_API_KEY_MISSING = "%s API key is required but not configured";
    public static final String CONFIG_PROJECT_ID_MISSING = "%s project ID is required but not configured";
    public static final String CONFIG_PROVIDER_INVALID = "Invalid AI provider configuration for %s";
    public static final String CONFIG_PROVIDER_UNAVAILABLE = "AI provider %s is not available";

    // Data Initialization Error Messages
    public static final String INIT_ADMIN_CREATION_FAILED = "Failed to initialize admin user";
    public static final String INIT_ADMIN_UPGRADE_FAILED = "Failed to upgrade user %s to admin role";

    private ErrorMessages() {
        // Private constructor to prevent instantiation
    }
}