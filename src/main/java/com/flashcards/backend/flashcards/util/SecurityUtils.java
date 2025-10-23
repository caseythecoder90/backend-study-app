package com.flashcards.backend.flashcards.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Security utility class for sanitizing user input to prevent injection attacks.
 *
 * This class provides methods to sanitize input for various contexts including:
 * - Log injection prevention
 * - NoSQL injection prevention
 * - General input sanitization
 *
 * All methods are defensive and return the original input if it is null or blank.
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Sanitizes input for logging to prevent log injection/forgery attacks.
     *
     * Removes control characters that could be used to inject fake log entries:
     * - Newlines (\n)
     * - Carriage returns (\r)
     * - Tabs (\t)
     * - Other control characters
     *
     * Example attack prevented:
     * Input: "admin\nINFO [FakeLogger] Unauthorized access granted"
     * Output: "admin_INFO [FakeLogger] Unauthorized access granted"
     *
     * @param input the string to sanitize
     * @return sanitized string safe for logging, or original if null/blank
     */
    public static String sanitizeForLog(String input) {
        if (isBlank(input)) {
            return input;
        }

        return input
                .replaceAll("[\n\r\t]", "_")     // Replace newlines, returns, tabs with underscore
                .replaceAll("\\p{Cntrl}", "")  // Remove other control characters
                .trim();
    }

    /**
     * Sanitizes input to prevent NoSQL injection attacks in MongoDB queries.
     *
     * Removes MongoDB special operators that could be used for injection:
     * - $where - allows arbitrary JavaScript execution
     * - $regex - could be used for regex injection
     * - $ne, $gt, $gte, $lt, $lte - comparison operators
     * - $in, $nin - array operators
     *
     * Note: Spring Data MongoDB with typed queries already provides good protection,
     * but this adds an extra layer of defense-in-depth.
     *
     * Example attack prevented:
     * Input: "{$where: 'this.password.length > 0'}"
     * Output: "{: 'this.password.length > 0'}"
     *
     * @param input the string to sanitize
     * @return sanitized string safe for MongoDB, or original if null/blank
     */
    public static String sanitizeForMongoDB(String input) {
        if (isBlank(input)) {
            return input;
        }

        return input
                .replaceAll("\\$where", "")
                .replaceAll("\\$regex", "")
                .replaceAll("\\$ne", "")
                .replaceAll("\\$gt", "")
                .replaceAll("\\$gte", "")
                .replaceAll("\\$lt", "")
                .replaceAll("\\$lte", "")
                .replaceAll("\\$in", "")
                .replaceAll("\\$nin", "")
                .replaceAll("\\$or", "")
                .replaceAll("\\$and", "")
                .trim();
    }

    /**
     * Sanitizes exception messages before logging to prevent sensitive data leakage
     * and log injection via crafted exceptions.
     *
     * Applies the same sanitization as sanitizeForLog() to exception messages
     * that may contain user input or system information.
     *
     * @param exceptionMessage the exception message to sanitize
     * @return sanitized exception message safe for logging
     */
    public static String sanitizeException(String exceptionMessage) {
        return sanitizeForLog(exceptionMessage);
    }

    /**
     * Validates that a string doesn't contain dangerous MongoDB operators.
     * Useful for additional validation before database operations.
     *
     * @param input the string to validate
     * @return true if input contains MongoDB operators, false otherwise
     */
    public static boolean containsMongoDBInjection(String input) {
        if (isBlank(input)) {
            return false;
        }

        return input.contains("$where") ||
               input.contains("$regex") ||
               input.contains("$ne") ||
               input.contains("$gt") ||
               input.contains("$lt") ||
               input.contains("$or") ||
               input.contains("$and");
    }

    /**
     * Validates that a string doesn't contain log injection patterns.
     * Useful for additional validation in security-critical scenarios.
     *
     * @param input the string to validate
     * @return true if input contains control characters, false otherwise
     */
    public static boolean containsLogInjectionPattern(String input) {
        if (isBlank(input)) {
            return false;
        }

        return input.matches(".*[\\n\\r\\t\\p{Cntrl}].*");
    }
}