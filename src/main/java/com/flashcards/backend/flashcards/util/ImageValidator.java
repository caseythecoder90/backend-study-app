package com.flashcards.backend.flashcards.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_IMAGE_REQUEST_INVALID_MIME;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_IMAGE_SIZE_ERROR;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Validator for image files uploaded via MultipartFile.
 * Validates MIME type and file size according to @ValidImage annotation constraints.
 */
public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private String[] allowedMimeTypes;
    private long maxSizeBytes;

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        this.allowedMimeTypes = constraintAnnotation.allowedMimeTypes();
        this.maxSizeBytes = constraintAnnotation.maxSizeBytes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (isNull(file) || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (isBlank(contentType) || isFalse(isAllowedMimeType(contentType))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    AI_IMAGE_REQUEST_INVALID_MIME.formatted(String.join(", ", allowedMimeTypes))
            ).addConstraintViolation();
            return false;
        }

        if (file.getSize() > maxSizeBytes) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    AI_IMAGE_SIZE_ERROR.formatted((maxSizeBytes / 1024 / 1024))
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isAllowedMimeType(String contentType) {
        return Arrays.stream(allowedMimeTypes)
                .anyMatch(allowed -> allowed.equalsIgnoreCase(contentType));
    }
}