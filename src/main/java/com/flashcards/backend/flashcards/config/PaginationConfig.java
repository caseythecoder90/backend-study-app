package com.flashcards.backend.flashcards.config;

import org.springframework.data.domain.Sort;

/**
 * Constants class for pagination settings across the application.
 * Defines default values and limits for paginated API responses.
 */
public class PaginationConfig {

    /**
     * Default number of items per page when not specified
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Maximum allowed page size to prevent abuse
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Default field to sort by
     */
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    /**
     * Default sort direction
     */
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private PaginationConfig() {
        // Private constructor to prevent instantiation
    }
}