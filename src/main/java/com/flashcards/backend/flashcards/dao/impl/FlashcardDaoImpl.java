package com.flashcards.backend.flashcards.dao.impl;

import com.flashcards.backend.flashcards.dao.FlashcardDao;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.model.Flashcard;
import com.flashcards.backend.flashcards.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_COUNT_BY_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_COUNT_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DELETE_BY_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DELETE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DUPLICATE_ENTRY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ENTITY_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ENTITY_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_ALL_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_BY_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_BY_ID_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ID_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_SAVE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_SAVE_MULTIPLE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_UPDATE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_FLASHCARD;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlashcardDaoImpl implements FlashcardDao {
    private final FlashcardRepository flashcardRepository;

    @Override
    public Optional<Flashcard> findById(String id) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(id)
                        .filter(validId -> isNotBlank(validId))
                        .flatMap(flashcardRepository::findById),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_ID_ERROR.formatted(ENTITY_FLASHCARD, id)
        );
    }

    @Override
    public List<Flashcard> findByDeckId(String deckId) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(deckId)
                        .filter(validDeckId -> isNotBlank(validDeckId))
                        .map(flashcardRepository::findByDeckId)
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_FLASHCARD, "deckId", deckId)
        );
    }

    @Override
    public List<Flashcard> findByUserId(String userId) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(userId)
                        .filter(validUserId -> isNotBlank(validUserId))
                        .map(flashcardRepository::findByUserId)
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_FLASHCARD, "userId", userId)
        );
    }

    @Override
    public List<Flashcard> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty) {
        return executeWithExceptionHandling(() -> {
            if (isBlank(deckId) || isNull(difficulty)) {
                return Collections.emptyList();
            }
            return flashcardRepository.findByDeckIdAndDifficulty(deckId, difficulty);
        }, ErrorCode.DAO_FIND_ERROR, DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_FLASHCARD, "deck and difficulty", deckId + "," + difficulty));
    }

    @Override
    public List<Flashcard> findByTagsContaining(String tag) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(tag)
                        .filter(validTag -> isNotBlank(validTag))
                        .map(flashcardRepository::findByTagsContaining)
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_FLASHCARD, "tag", tag)
        );
    }

    @Override
    public List<Flashcard> findAll() {
        return executeWithExceptionHandling(
                flashcardRepository::findAll,
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_ALL_ERROR.formatted("flashcards")
        );
    }

    @Override
    public Flashcard save(Flashcard flashcard) {
        return executeWithExceptionHandling(() -> {
            requireNonNull(flashcard, DAO_ENTITY_NULL.formatted(ENTITY_FLASHCARD));

            return Optional.of(flashcard)
                    .map(f -> {
                        f.setCreatedAt(LocalDateTime.now());
                        f.setUpdatedAt(LocalDateTime.now());
                        return f;
                    })
                    .map(flashcardRepository::save)
                    .orElseThrow(() -> new DaoException(DAO_SAVE_ERROR.formatted(ENTITY_FLASHCARD), ErrorCode.DAO_SAVE_ERROR));
        }, ErrorCode.DAO_SAVE_ERROR, DAO_SAVE_ERROR.formatted(ENTITY_FLASHCARD));
    }

    @Override
    public Flashcard update(Flashcard flashcard) {
        return executeWithExceptionHandling(() -> {
            requireNonNull(flashcard, DAO_ENTITY_NULL.formatted(ENTITY_FLASHCARD));
            requireNonNull(flashcard.getId(), DAO_ID_NULL.formatted(ENTITY_FLASHCARD));

            return findById(flashcard.getId())
                    .map(existing -> {
                        flashcard.setCreatedAt(existing.getCreatedAt());
                        flashcard.setUpdatedAt(LocalDateTime.now());
                        return flashcardRepository.save(flashcard);
                    })
                    .orElseThrow(() -> new DaoException(
                            DAO_ENTITY_NOT_FOUND.formatted(ENTITY_FLASHCARD, flashcard.getId()),
                            ErrorCode.DAO_UPDATE_ERROR
                    ));
        }, ErrorCode.DAO_UPDATE_ERROR, DAO_UPDATE_ERROR.formatted(ENTITY_FLASHCARD, flashcard.getId()));
    }

    @Override
    public List<Flashcard> saveAll(List<Flashcard> flashcards) {
        return executeWithExceptionHandling(() -> {
            if (isEmpty(flashcards)) {
                return Collections.emptyList();
            }

            LocalDateTime now = LocalDateTime.now();
            List<Flashcard> toSave = flashcards.stream()
                    .filter(flashcard -> nonNull(flashcard))
                    .map(f -> {
                        f.setCreatedAt(now);
                        f.setUpdatedAt(now);
                        return f;
                    })
                    .collect(Collectors.toList());

            return flashcardRepository.saveAll(toSave);
        }, ErrorCode.DAO_SAVE_ERROR, DAO_SAVE_MULTIPLE_ERROR.formatted(ENTITY_FLASHCARD));
    }

    @Override
    public void deleteById(String id) {
        executeWithExceptionHandling(() -> {
            Optional.ofNullable(id)
                    .filter(validId -> isNotBlank(validId))
                    .ifPresent(flashcardRepository::deleteById);
            return null;
        }, ErrorCode.DAO_DELETE_ERROR, DAO_DELETE_ERROR.formatted(ENTITY_FLASHCARD, id));
    }

    @Override
    public void deleteByDeckId(String deckId) {
        executeWithExceptionHandling(() -> {
            Optional.ofNullable(deckId)
                    .filter(validDeckId -> isNotBlank(validDeckId))
                    .ifPresent(flashcardRepository::deleteByDeckId);
            return null;
        }, ErrorCode.DAO_DELETE_ERROR, DAO_DELETE_BY_FIELD_ERROR.formatted(ENTITY_FLASHCARD, "deckId", deckId));
    }

    @Override
    public long countByDeckId(String deckId) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(deckId)
                        .filter(validDeckId -> isNotBlank(validDeckId))
                        .map(flashcardRepository::countByDeckId)
                        .orElse(0L),
                ErrorCode.DAO_FIND_ERROR,
                DAO_COUNT_BY_FIELD_ERROR.formatted(ENTITY_FLASHCARD, "deckId", deckId)
        );
    }

    @Override
    public long count() {
        return executeWithExceptionHandling(
                flashcardRepository::count,
                ErrorCode.DAO_FIND_ERROR,
                DAO_COUNT_ERROR.formatted("flashcards")
        );
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, ErrorCode errorCode, String errorMessage) {
        try {
            return operation.get();
        } catch (DuplicateKeyException e) {
            log.error("Duplicate key error: {}", e.getMessage());
            throw new DaoException(DAO_DUPLICATE_ENTRY.formatted(ENTITY_FLASHCARD), ErrorCode.DAO_DUPLICATE_ERROR, e);
        } catch (DataAccessException e) {
            log.error("{}: {}", errorMessage, e.getMessage());
            throw new DaoException(errorMessage, errorCode, e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new DaoException(errorMessage, errorCode, e);
        }
    }
}