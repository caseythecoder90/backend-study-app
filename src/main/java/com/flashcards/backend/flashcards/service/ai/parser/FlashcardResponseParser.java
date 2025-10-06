package com.flashcards.backend.flashcards.service.ai.parser;

import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.model.Flashcard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.flashcards.backend.flashcards.constants.AIConstants.*;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.*;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Reusable component for parsing AI responses into flashcard DTOs.
 * Used by multiple strategies that generate flashcards.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashcardResponseParser {

    private final ObjectMapper objectMapper;
    private final JsonCleaner jsonCleaner;

    /**
     * Parse AI response into a list of flashcard DTOs.
     *
     * @param response The raw AI response
     * @param userId The user ID to associate with flashcards
     * @param deckId The deck ID to associate with flashcards
     * @param expectedCount Expected number of flashcards
     * @return List of parsed flashcard DTOs
     */
    public List<CreateFlashcardDto> parse(String response, String userId, String deckId, int expectedCount) {
        try {
            String cleanResponse = jsonCleaner.clean(response);

            // Validate JSON is not empty or incomplete
            if (isBlank(cleanResponse) || isFalse(cleanResponse.trim().endsWith("]"))) {
                log.error("AI response appears to be incomplete or empty. Response length: {}, ends with ]: {}",
                    cleanResponse.length(), cleanResponse.trim().endsWith("]"));
                throw new ServiceException(
                    AI_RESPONSE_INCOMPLETE,
                    ErrorCode.SERVICE_AI_GENERATION_ERROR
                );
            }

            List<Map<String, Object>> flashcardMaps = objectMapper.readValue(
                    cleanResponse,
                    new TypeReference<>() {}
            );

            List<CreateFlashcardDto> flashcards = flashcardMaps.stream()
                    .map(flashcardMap -> convertToCreateFlashcardDto(flashcardMap, userId, deckId))
                    .filter(Objects::nonNull)
                    .toList();

            // Validate we got the expected number of flashcards
            if (flashcards.size() < expectedCount) {
                log.warn("AI generated {} flashcards but {} were requested. Some flashcards may be invalid or incomplete.",
                    flashcards.size(), expectedCount);
            }

            if (flashcards.isEmpty()) {
                throw new ServiceException(
                    AI_NO_VALID_FLASHCARDS,
                    ErrorCode.SERVICE_AI_GENERATION_ERROR
                );
            }

            log.info("Successfully parsed {} flashcards from AI response (requested: {})",
                flashcards.size(), expectedCount);

            return flashcards;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON. Response length: {}, Error: {}",
                    response.length(), e.getMessage());
            log.debug("Full AI response that failed parsing: {}", response);

            String errorMessage = AI_RESPONSE_PARSE_FAILED;
            if (e.getMessage().contains("Unexpected end-of-input")) {
                errorMessage += AI_RESPONSE_TRUNCATED;
            } else if (e.getMessage().contains("expected close marker")) {
                errorMessage += AI_RESPONSE_MALFORMED;
            }

            throw new ServiceException(
                    errorMessage,
                    ErrorCode.SERVICE_AI_GENERATION_ERROR,
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private CreateFlashcardDto convertToCreateFlashcardDto(Map<String, Object> flashcardMap, String userId, String deckId) {
        try {
            CreateFlashcardDto.CreateFlashcardDtoBuilder builder = CreateFlashcardDto.builder()
                    .deckId(deckId)
                    .userId(userId);

            // Parse front content
            Map<String, Object> frontMap = (Map<String, Object>) flashcardMap.get(JSON_FIELD_FRONT);
            if (nonNull(frontMap)) {
                builder.front(parseCardContent(frontMap));
            }

            // Parse back content
            Map<String, Object> backMap = (Map<String, Object>) flashcardMap.get(JSON_FIELD_BACK);
            if (nonNull(backMap)) {
                builder.back(parseCardContent(backMap));
            }

            // Parse other fields
            if (flashcardMap.containsKey(JSON_FIELD_HINT)) {
                builder.hint((String) flashcardMap.get(JSON_FIELD_HINT));
            }

            if (flashcardMap.containsKey(JSON_FIELD_TAGS)) {
                List<String> tags = (List<String>) flashcardMap.get(JSON_FIELD_TAGS);
                builder.tags(tags);
            }

            if (flashcardMap.containsKey(JSON_FIELD_DIFFICULTY)) {
                String difficultyStr = (String) flashcardMap.get(JSON_FIELD_DIFFICULTY);
                builder.difficulty(parseDifficulty(difficultyStr));
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Failed to convert flashcard map to DTO: {}", flashcardMap, e);
            return null; // Skip invalid flashcards
        }
    }

    @SuppressWarnings("unchecked")
    private FlashcardDto.CardContentDto parseCardContent(Map<String, Object> contentMap) {
        FlashcardDto.CardContentDto.CardContentDtoBuilder builder = FlashcardDto.CardContentDto.builder();

        if (contentMap.containsKey(JSON_FIELD_TEXT)) {
            builder.text((String) contentMap.get(JSON_FIELD_TEXT));
        }

        if (contentMap.containsKey(JSON_FIELD_TYPE)) {
            String typeStr = (String) contentMap.get(JSON_FIELD_TYPE);
            builder.type(parseContentType(typeStr));
        }

        if (contentMap.containsKey(JSON_FIELD_CODE_BLOCKS)) {
            List<Map<String, Object>> codeBlockMaps = (List<Map<String, Object>>) contentMap.get(JSON_FIELD_CODE_BLOCKS);
            List<FlashcardDto.CodeBlockDto> codeBlocks = codeBlockMaps.stream()
                    .map(this::parseCodeBlock)
                    .filter(Objects::nonNull)
                    .toList();
            builder.codeBlocks(codeBlocks);
        }

        return builder.build();
    }

    private FlashcardDto.CodeBlockDto parseCodeBlock(Map<String, Object> codeBlockMap) {
        try {
            FlashcardDto.CodeBlockDto.CodeBlockDtoBuilder builder = FlashcardDto.CodeBlockDto.builder();

            if (codeBlockMap.containsKey(JSON_FIELD_LANGUAGE)) {
                builder.language((String) codeBlockMap.get(JSON_FIELD_LANGUAGE));
            }

            if (codeBlockMap.containsKey(JSON_FIELD_CODE)) {
                builder.code((String) codeBlockMap.get(JSON_FIELD_CODE));
            }

            if (codeBlockMap.containsKey(JSON_FIELD_FILE_NAME)) {
                builder.fileName((String) codeBlockMap.get(JSON_FIELD_FILE_NAME));
            }

            if (codeBlockMap.containsKey(JSON_FIELD_HIGHLIGHTED)) {
                builder.highlighted(isTrue((Boolean) codeBlockMap.get(JSON_FIELD_HIGHLIGHTED)));
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Failed to parse code block: {}", codeBlockMap, e);
            return null;
        }
    }

    private Flashcard.DifficultyLevel parseDifficulty(String difficultyStr) {
        if (isBlank(difficultyStr)) {
            return Flashcard.DifficultyLevel.NOT_SET;
        }

        try {
            return Flashcard.DifficultyLevel.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid difficulty level: {}, defaulting to NOT_SET", difficultyStr);
            return Flashcard.DifficultyLevel.NOT_SET;
        }
    }

    private Flashcard.ContentType parseContentType(String typeStr) {
        if (isBlank(typeStr)) {
            return Flashcard.ContentType.TEXT_ONLY;
        }

        try {
            return Flashcard.ContentType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid content type: {}, defaulting to TEXT_ONLY", typeStr);
            return Flashcard.ContentType.TEXT_ONLY;
        }
    }
}