package com.flashcards.backend.flashcards.constants;

public class AuthConstants {

    // BCrypt Password Prefixes
    public static final String BCRYPT_PREFIX_2A = "$2a$";
    public static final String BCRYPT_PREFIX_2B = "$2b$";
    public static final String BCRYPT_PREFIX_2Y = "$2y$";

    // BCrypt Strength
    public static final int BCRYPT_STRENGTH = 12;

    // TOTP Configuration
    public static final int TOTP_CODE_LENGTH = 6;
    public static final int TOTP_TIME_STEP_SECONDS = 30;
    public static final int TOTP_WINDOW = 1;

    // Password Validation
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;

    // Username Validation
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;

    // QR Code Generation
    public static final String QR_CODE_DATA_URI_PREFIX = "data:image/png;base64,";

    // Recovery Codes Configuration
    public static final int RECOVERY_CODE_COUNT = 10;
    public static final int RECOVERY_CODE_LENGTH = 8;
    public static final int RECOVERY_CODE_SEGMENT_LENGTH = 4;
    public static final String RECOVERY_CODE_DELIMITER = "-";
    public static final String RECOVERY_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int RECOVERY_CODE_MIN_WARNING_THRESHOLD = 3;

    // Recovery Codes Messages
    public static final String RECOVERY_CODE_WARNING = "Save these recovery codes in a secure place. Each code can only be used once. You won't be able to see these codes again!";
    public static final String RECOVERY_CODE_LOW_WARNING = "You have only %d recovery codes remaining. Consider regenerating your codes.";
    public static final String RECOVERY_CODE_INSTRUCTIONS = "Use one of these codes to sign in if you lose access to your authenticator device.";

    // Service Error Messages
    public static final String TOTP_ALREADY_ENABLED = "TOTP is already enabled for this user";
    public static final String TOTP_NOT_ENABLED = "TOTP is not enabled for this user";
    public static final String TOTP_SETUP_REQUIRED = "TOTP setup must be completed before enabling";

    // OAuth Provider Names
    public static final String OAUTH_PROVIDER_GOOGLE = "google";
    public static final String OAUTH_PROVIDER_GITHUB = "github";

    // OAuth User Attributes - Google
    public static final String GOOGLE_ATTR_SUB = "sub";
    public static final String GOOGLE_ATTR_NAME = "name";
    public static final String GOOGLE_ATTR_EMAIL = "email";
    public static final String GOOGLE_ATTR_PICTURE = "picture";
    public static final String GOOGLE_ATTR_GIVEN_NAME = "given_name";
    public static final String GOOGLE_ATTR_FAMILY_NAME = "family_name";

    // OAuth User Attributes - GitHub
    public static final String GITHUB_ATTR_ID = "id";
    public static final String GITHUB_ATTR_NAME = "name";
    public static final String GITHUB_ATTR_EMAIL = "email";
    public static final String GITHUB_ATTR_AVATAR_URL = "avatar_url";

    // OAuth Endpoints
    public static final String OAUTH_AUTHORIZATION_BASE_PATH = "/oauth2/authorization";

    // OAuth URL Parameters
    public static final String OAUTH_PARAM_TOKEN = "token";
    public static final String OAUTH_PARAM_TYPE = "type";
    public static final String OAUTH_PARAM_ERROR = "error";
    public static final String OAUTH_TYPE_VALUE = "oauth";
    public static final String OAUTH_ERROR_AUTHENTICATION_FAILED = "authentication_failed";

    // OAuth Username Generation
    public static final String OAUTH_USERNAME_FALLBACK_PREFIX = "user";

    private AuthConstants() {}
}