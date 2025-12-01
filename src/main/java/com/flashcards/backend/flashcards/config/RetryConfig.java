package com.flashcards.backend.flashcards.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.RetryConfiguration;

/**
 * Configuration for Spring Retry functionality.
 * <p>
 * Enables retry capabilities across the application for handling transient failures,
 * particularly in database and external service operations.
 * <p>
 * Retry behavior is configured via application properties:
 * <ul>
 *   <li>retry.max-attempts: Maximum number of retry attempts (default: 3)</li>
 *   <li>retry.backoff-delay: Initial backoff delay in milliseconds (default: 100ms)</li>
 *   <li>retry.backoff-multiplier: Exponential backoff multiplier (default: 2.0)</li>
 * </ul>
 *
 * @see com.flashcards.backend.flashcards.annotation.TransientRetryableTemplate
 */
@Slf4j
@Configuration
@EnableRetry
public class RetryConfig {

    @Value("${retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${retry.backoff-delay:100}")
    private long backoffDelay;

    @Value("${retry.backoff-multiplier:2.0}")
    private double backoffMultiplier;

    public RetryConfig() {
        log.info("Spring Retry enabled with configurable properties");
    }
}