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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_INVALID_PARAMETER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_INVALID_REQUEST;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_MISSING_PARAMETER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.CONTROLLER_RESOURCE_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_DATA_DEPENDENCY_CONSTRAINT;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_DATA_INTEGRITY_VIOLATION;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_DUPLICATE_KEY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_INTERNAL_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_INVALID_REQUEST_BODY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_MEDIA_TYPE_NOT_SUPPORTED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_METHOD_NOT_ALLOWED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_PARAMETER_TYPE_EXPECTED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_PARAMETER_TYPE_GOT;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_SUPPORTED_METHODS_NONE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_UNEXPECTED_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.HANDLER_VALIDATION_FAILED;
import static com.flashcards.backend.flashcards.constants.Punctuation.COLON_SPACE;
import static com.flashcards.backend.flashcards.constants.Punctuation.COMMA_SPACE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
                HANDLER_INTERNAL_ERROR,
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
                .collect(Collectors.joining(COMMA_SPACE.getValue()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.CONTROLLER_BAD_REQUEST)
                .message(HANDLER_VALIDATION_FAILED.formatted(COLON_SPACE.getValue(), message))
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        log.warn("Validation failed{} {}", COLON_SPACE.getValue(), message);
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

        String typeDetails = HANDLER_PARAMETER_TYPE_EXPECTED.formatted(COLON_SPACE.getValue(), expectedType) +
                COMMA_SPACE.getValue() +
                HANDLER_PARAMETER_TYPE_GOT.formatted(COLON_SPACE.getValue(), ex.getValue());

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                CONTROLLER_INVALID_PARAMETER.formatted(parameterName, typeDetails),
                request.getRequestURI()
        );

        log.warn("Type mismatch for parameter{}{} expected{} {}{} got{} {}",
                COLON_SPACE.getValue(), parameterName,
                COLON_SPACE.getValue(), expectedType,
                COMMA_SPACE.getValue(),
                COLON_SPACE.getValue(), ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = Optional.ofNullable(ex.getMessage())
                .filter(StringUtils::isNotBlank)
                .map(msg -> msg.split(COLON_SPACE.getValue())[0]) // Take only the first part of the message
                .orElse(HANDLER_INVALID_REQUEST_BODY);

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
                        .collect(Collectors.joining(COMMA_SPACE.getValue())))
                .orElse(HANDLER_SUPPORTED_METHODS_NONE);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                HANDLER_METHOD_NOT_ALLOWED.formatted(ex.getMethod(), COLON_SPACE.getValue(), supportedMethods),
                request.getRequestURI()
        );

        log.warn("Method not allowed{} {} for path {}", COLON_SPACE.getValue(), ex.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(COMMA_SPACE.getValue()));

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorCode.CONTROLLER_BAD_REQUEST,
                HANDLER_MEDIA_TYPE_NOT_SUPPORTED.formatted(COLON_SPACE.getValue(), supportedTypes),
                request.getRequestURI()
        );

        log.warn("Unsupported media type{} {}", COLON_SPACE.getValue(), ex.getContentType());
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
                HANDLER_DUPLICATE_KEY,
                request.getRequestURI()
        );

        log.warn("Duplicate key violation{} {}", COLON_SPACE.getValue(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = HANDLER_DATA_INTEGRITY_VIOLATION;

        if (nonNull(ex.getMessage())) {
            if (ex.getMessage().contains("duplicate")) {
                message = HANDLER_DUPLICATE_KEY;
            } else if (ex.getMessage().contains("foreign key")) {
                message = HANDLER_DATA_DEPENDENCY_CONSTRAINT;
            }
        }

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR,
                message,
                request.getRequestURI()
        );

        log.error("Data integrity violation{} {}", COLON_SPACE.getValue(), ex.getMessage());
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

        log.warn("Illegal argument{} {}", COLON_SPACE.getValue(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR,
                HANDLER_UNEXPECTED_ERROR,
                request.getRequestURI()
        );

        log.error("Unexpected error occurred{} {}", COLON_SPACE.getValue(), ex.getMessage(), ex);
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
        if (isNull(errorCode)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (errorCode) {
            // 400 Bad Request - Validation and input errors
            case SERVICE_VALIDATION_ERROR, CONTROLLER_BAD_REQUEST, INVALID_INPUT -> HttpStatus.BAD_REQUEST;

            // 401 Unauthorized - Authentication failures
            case SERVICE_AUTHORIZATION_ERROR, CONTROLLER_UNAUTHORIZED,
                 AUTH_INVALID_CREDENTIALS, AUTH_TOKEN_INVALID, AUTH_TOKEN_EXPIRED,
                 AUTH_PASSWORD_INVALID, AUTH_TOTP_INVALID, AUTH_TOTP_REQUIRED,
                 AUTH_RECOVERY_CODE_INVALID, AUTH_RECOVERY_CODES_EXHAUSTED,
                 AUTH_RECOVERY_CODES_NOT_ENABLED -> HttpStatus.UNAUTHORIZED;

            // 403 Forbidden - Authenticated but not allowed
            case CONTROLLER_FORBIDDEN, AUTH_USER_DISABLED -> HttpStatus.FORBIDDEN;

            // 404 Not Found - Resource not found
            case SERVICE_NOT_FOUND, CONTROLLER_NOT_FOUND -> HttpStatus.NOT_FOUND;

            // 409 Conflict - Duplicate resources
            case SERVICE_DUPLICATE_ERROR -> HttpStatus.CONFLICT;

            // 429 Too Many Requests - Rate limiting
            case SERVICE_AI_RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;

            // 500 Internal Server Error - Server errors, DAO errors, unexpected errors
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}