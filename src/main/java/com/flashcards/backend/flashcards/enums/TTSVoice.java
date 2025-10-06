package com.flashcards.backend.flashcards.enums;

import lombok.Getter;

import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_ALLOY;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_DESC_ALLOY;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_DESC_ECHO;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_DESC_FABLE;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_DESC_NOVA;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_DESC_ONYX;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_DESC_SHIMMER;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_ECHO;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_FABLE;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_NOVA;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_ONYX;
import static com.flashcards.backend.flashcards.constants.AIConstants.VOICE_SHIMMER;

/**
 * OpenAI Text-to-Speech voice options.
 */
@Getter
public enum TTSVoice {
    ALLOY(VOICE_ALLOY, VOICE_DESC_ALLOY),
    ECHO(VOICE_ECHO, VOICE_DESC_ECHO),
    FABLE(VOICE_FABLE, VOICE_DESC_FABLE),
    ONYX(VOICE_ONYX, VOICE_DESC_ONYX),
    NOVA(VOICE_NOVA, VOICE_DESC_NOVA),
    SHIMMER(VOICE_SHIMMER, VOICE_DESC_SHIMMER);

    private final String voiceId;
    private final String description;

    TTSVoice(String voiceId, String description) {
        this.voiceId = voiceId;
        this.description = description;
    }
}