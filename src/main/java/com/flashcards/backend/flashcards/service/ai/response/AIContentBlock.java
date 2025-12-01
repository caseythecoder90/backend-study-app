package com.flashcards.backend.flashcards.service.ai.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AITextBlock.class, name = "TEXT"),
    @JsonSubTypes.Type(value = AICodeBlock.class, name = "CODE")
})
public sealed interface AIContentBlock permits AITextBlock, AICodeBlock {}

record AITextBlock(String content) implements AIContentBlock {}

record AICodeBlock(
    String language,
    String code,
    String fileName,
    Boolean highlighted,
    List<Integer> highlightedLines
) implements AIContentBlock {}