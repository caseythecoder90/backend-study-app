package com.flashcards.backend.flashcards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "decks")
public class Deck {
    @Id
    private String id;

    private String title;
    private String description;
    private String category;
    private String userId;
    private boolean isPublic;

    private List<String> tags;
    private int flashcardCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastStudiedAt;
}