package com.flashcards.backend.flashcards.service.ai.response;

import java.util.List;

public record AIGeneratedCard(
    AICardContent front,
    AICardContent back,
    String hint,
    List<String> tags,
    String difficulty
) {}