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

    private AuthConstants() {}
}