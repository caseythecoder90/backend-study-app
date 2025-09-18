package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.dto.ErrorResponse;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_INVALID_PARAMETER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_INVALID_REQUEST;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_MISSING_PARAMETER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_RESOURCE_NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(
            ServiceException ex,
            HttpServletRequest request) {

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());

        ErrorResponse errorResponse = buildErrorResponse(
                status,
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        log.error("Service exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DaoException.class)
    public ResponseEntity<ErrorResponse> handleDaoException(
            DaoException ex,
            HttpServletRequest request) {

        // DAO exceptions should be caught by service layer, but if they leak through...
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getErrorCode(),
                "An internal error occurred. Please try again later.",
                request.getRequestURI()
        );

        log.error("DAO exception leaked to controller layer: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildValidationError)
                .collect(Collectors.toList());

        String message = validationErrors.stream()
                .map(ErrorResponse.ValidationError::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.CONTROLLER_BAD_REQUEST)
                .message("Validation failed: " + message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                CONTROLLER_MISSING_PARAMETER.formatted(ex.getParameterName()),
                request.getRequestURI()
        );

        log.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String parameterName = ex.getName();
        String expectedType = Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("unknown");

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                CONTROLLER_INVALID_PARAMETER.formatted(parameterName,
                    "Expected type: " + expectedType + ", got: " + ex.getValue()),
                request.getRequestURI()
        );

        log.warn("Type mismatch for parameter {}: expected {}, got {}",
                parameterName, expectedType, ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = Optional.ofNullable(ex.getMessage())
                .filter(StringUtils::isNotBlank)
                .map(msg -> msg.split(":")[0]) // Take only the first part of the message
                .orElse("Invalid request body");

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                CONTROLLER_INVALID_REQUEST.formatted(message),
                request.getRequestURI()
        );

        log.warn("Invalid request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        String supportedMethods = Optional.ofNullable(ex.getSupportedHttpMethods())
                .map(methods -> methods.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")))
                .orElse("none");

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                "Method " + ex.getMethod() + " not allowed. Supported methods: " + supportedMethods,
                request.getRequestURI()
        );

        log.warn("Method not allowed: {} for path {}", ex.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                "Media type not supported. Supported types: " + supportedTypes,
                request.getRequestURI()
        );

        log.warn("Unsupported media type: {}", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.CONTROLLER_NOT_FOUND,
                CONTROLLER_RESOURCE_NOT_FOUND.formatted(ex.getRequestURL()),
                request.getRequestURI()
        );

        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(
            DuplicateKeyException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.SERVICE_DUPLICATE_ERROR,
                "A resource with the same key already exists",
                request.getRequestURI()
        );

        log.warn("Duplicate key violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = "Data integrity violation";

        if (Objects.nonNull(ex.getMessage())) {
            if (ex.getMessage().contains("duplicate")) {
                message = "A resource with the same key already exists";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Cannot perform operation due to related data dependencies";
            }
        }

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR,
                message,
                request.getRequestURI()
        );

        log.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR,
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, ErrorCode code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(path)
                .build();
    }

    private ErrorResponse.ValidationError buildValidationError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        if (Objects.isNull(errorCode)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (errorCode) {
            case SERVICE_VALIDATION_ERROR, CONTROLLER_BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case SERVICE_NOT_FOUND, CONTROLLER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case SERVICE_DUPLICATE_ERROR -> HttpStatus.CONFLICT;
            case SERVICE_AUTHORIZATION_ERROR, CONTROLLER_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case CONTROLLER_FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}