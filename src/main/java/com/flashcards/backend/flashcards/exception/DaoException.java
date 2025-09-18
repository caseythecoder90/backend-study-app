package com.flashcards.backend.flashcards.exception;

public class DaoException extends RuntimeException {
    private final ErrorCode errorCode;

    public DaoException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public DaoException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DaoException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}