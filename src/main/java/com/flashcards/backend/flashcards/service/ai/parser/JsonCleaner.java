package com.flashcards.backend.flashcards.service.ai.parser;

import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Utility component for cleaning JSON responses from AI models.
 * Removes markdown code blocks and extracts pure JSON.
 */
@Component
public class JsonCleaner {

    /**
     * Clean a JSON response by removing markdown code blocks and extra text.
     *
     * @param response The raw AI response
     * @return Cleaned JSON string
     */
    public String clean(String response) {
        if (isBlank(response)) {
            return "";
        }

        String cleaned = response.trim();

        // Remove markdown code block markers if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        // Find the JSON array start and end
        int jsonStart = cleaned.indexOf('[');
        int jsonEnd = cleaned.lastIndexOf(']') + 1;

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd);
        }

        return cleaned.trim();
    }
}