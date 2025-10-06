package com.flashcards.backend.flashcards.service.ai.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Reusable component for parsing AI summary responses.
 * Used by strategies that generate summaries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryResponseParser {

    /**
     * Parse and clean a summary response.
     *
     * @param response The raw AI response
     * @return Cleaned summary text
     */
    public String parse(String response) {
        if (isBlank(response)) {
            return "";
        }

        // Summary responses are typically plain text, just trim
        String cleaned = response.trim();

        // Remove any markdown formatting if present
        cleaned = removeMarkdownFormatting(cleaned);

        log.debug("Parsed summary with {} words", countWords(cleaned));

        return cleaned;
    }

    /**
     * Count words in the summary.
     */
    public int countWords(String text) {
        if (isBlank(text)) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private String removeMarkdownFormatting(String text) {
        // Remove common markdown artifacts that might appear in summaries
        // This is a simple cleanup - can be enhanced if needed
        return text
            .replaceAll("^```.*\n", "")  // Remove opening code blocks
            .replaceAll("\n```$", "")     // Remove closing code blocks
            .trim();
    }
}