package com.flashcards.backend.flashcards.enums;

import lombok.Getter;

import static com.flashcards.backend.flashcards.constants.AIConstants.AUDIO_OUTPUT_RECITATION;
import static com.flashcards.backend.flashcards.constants.AIConstants.AUDIO_OUTPUT_SUMMARY;

/**
 * Types of audio output for text-to-speech operations.
 */
@Getter
public enum AudioOutputType {
    /**
     * Generate audio of the complete text (recitation)
     */
    RECITATION(AUDIO_OUTPUT_RECITATION, "Full text read aloud"),

    /**
     * Generate audio of a summarized version of the text
     */
    SUMMARY(AUDIO_OUTPUT_SUMMARY, "Summarized text read aloud");

    private final String value;
    private final String description;

    AudioOutputType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}