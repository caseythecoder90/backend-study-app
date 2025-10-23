package com.flashcards.backend.flashcards.constants;

import lombok.Getter;

@Getter
public enum Punctuation {
    COMMA(","),
    PERIOD("."),
    COLON(":"),
    SEMICOLON(";"),
    SPACE(" "),
    COMMA_SPACE(", "),
    COLON_SPACE(": "),
    PERIOD_SPACE(". "),
    EMPTY("");

    private final String value;

    Punctuation(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}