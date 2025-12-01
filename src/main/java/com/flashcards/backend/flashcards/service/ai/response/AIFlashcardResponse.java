package com.flashcards.backend.flashcards.service.ai.response;

import java.util.List;

public record AIFlashcardResponse(List<AIGeneratedCard> flashcards) {}