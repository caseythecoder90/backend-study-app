package com.flashcards.backend.flashcards.annotation;

import com.mongodb.MongoException;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom retry annotation for MongoDB transient failures.
 * <p>
 * This annotation combines Spring Retry's @Retryable with sensible defaults
 * for MongoDB and GridFS operations. It will automatically retry on:
 * <ul>
 *   <li>MongoException - MongoDB driver exceptions</li>
 *   <li>DataAccessException - Spring Data access layer exceptions</li>
 * </ul>
 * <p>
 * Retry configuration is externalized in application.yml:
 * <pre>
 * retry:
 *   max-attempts: 3           # Maximum number of retry attempts
 *   backoff-delay: 100        # Initial backoff delay in milliseconds
 *   backoff-multiplier: 2.0   # Backoff multiplier for exponential backoff
 * </pre>
 * <p>
 * Recovery Method Pattern:
 * <pre>
 * {@code @TransientRetryableTemplate}
 * public String uploadImage(MultipartFile file, String userId) {
 *     // No try-catch needed - exceptions bubble up to retry mechanism
 *     return gridFsTemplate.store(...);
 * }
 *
 * // Recovery method catches all exceptions after all retries exhausted
 * {@code @Recover}
 * public String recoverUploadImage(Exception e, MultipartFile file, String userId) {
 *     log.error("Failed to upload image after retries: {}", e.getMessage());
 *     throw new ServiceException(ErrorCode.IMAGE_UPLOAD_FAILED, ...);
 * }
 * </pre>
 *
 * @see org.springframework.retry.annotation.Retryable
 * @see org.springframework.retry.annotation.Recover
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Retryable(
    retryFor = {MongoException.class, DataAccessException.class},
    maxAttemptsExpression = "${retry.max-attempts:3}",
    backoff = @Backoff(
        delayExpression = "${retry.backoff-delay:100}",
        multiplierExpression = "${retry.backoff-multiplier:2.0}"
    )
)
public @interface TransientRetryableTemplate {
}