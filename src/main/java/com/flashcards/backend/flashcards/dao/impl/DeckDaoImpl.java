package com.flashcards.backend.flashcards.dao.impl;

import com.flashcards.backend.flashcards.dao.DeckDao;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.model.Deck;
import com.flashcards.backend.flashcards.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_COUNT_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DELETE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DUPLICATE_ENTRY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ENTITY_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ENTITY_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_ALL_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_BY_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_BY_ID_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ID_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_SAVE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_UPDATE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_DECK;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeckDaoImpl implements DeckDao {
    private final DeckRepository deckRepository;

    @Override
    public Optional<Deck> findById(String id) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(id)
                        .filter(validId -> isNotBlank(validId))
                        .flatMap(deckRepository::findById),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_ID_ERROR.formatted(ENTITY_DECK, id)
        );
    }

    @Override
    public List<Deck> findByUserId(String userId) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(userId)
                        .filter(validUserId -> isNotBlank(validUserId))
                        .map(deckRepository::findByUserId)
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_DECK, "userId", userId)
        );
    }

    @Override
    public List<Deck> findByIsPublicTrue() {
        return executeWithExceptionHandling(
                deckRepository::findByIsPublicTrue,
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_DECK, "isPublic", "true")
        );
    }

    @Override
    public List<Deck> findByCategory(String category) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(category)
                        .filter(validCategory -> isNotBlank(validCategory))
                        .map(deckRepository::findByCategory)
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_DECK, "category", category)
        );
    }

    @Override
    public List<Deck> findByTagsContaining(String tag) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(tag)
                        .filter(validTag -> isNotBlank(validTag))
                        .map(deckRepository::findByTagsContaining)
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_DECK, "tag", tag)
        );
    }

    @Override
    public List<Deck> findByUserIdAndIsPublic(String userId, boolean isPublic) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(userId)
                        .filter(validUserId -> isNotBlank(validUserId))
                        .map(id -> deckRepository.findByUserIdAndIsPublic(id, isPublic))
                        .orElse(Collections.emptyList()),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_DECK, "userId and isPublic", userId + "," + isPublic)
        );
    }

    @Override
    public List<Deck> findAll() {
        return executeWithExceptionHandling(
                deckRepository::findAll,
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_ALL_ERROR.formatted("decks")
        );
    }

    @Override
    public Deck save(Deck deck) {
        return executeWithExceptionHandling(() -> {
            requireNonNull(deck, DAO_ENTITY_NULL.formatted(ENTITY_DECK));

            return Optional.of(deck)
                    .map(d -> {
                        d.setCreatedAt(LocalDateTime.now());
                        d.setUpdatedAt(LocalDateTime.now());
                        return d;
                    })
                    .map(deckRepository::save)
                    .orElseThrow(() -> new DaoException(DAO_SAVE_ERROR.formatted(ENTITY_DECK), ErrorCode.DAO_SAVE_ERROR));
        }, ErrorCode.DAO_SAVE_ERROR, DAO_SAVE_ERROR.formatted(ENTITY_DECK));
    }

    @Override
    public Deck update(Deck deck) {
        return executeWithExceptionHandling(() -> {
            requireNonNull(deck, DAO_ENTITY_NULL.formatted(ENTITY_DECK));
            requireNonNull(deck.getId(), DAO_ID_NULL.formatted(ENTITY_DECK));

            return findById(deck.getId())
                    .map(existing -> {
                        deck.setCreatedAt(existing.getCreatedAt());
                        deck.setUpdatedAt(LocalDateTime.now());
                        return deckRepository.save(deck);
                    })
                    .orElseThrow(() -> new DaoException(
                            DAO_ENTITY_NOT_FOUND.formatted(ENTITY_DECK, deck.getId()),
                            ErrorCode.DAO_UPDATE_ERROR
                    ));
        }, ErrorCode.DAO_UPDATE_ERROR, DAO_UPDATE_ERROR.formatted(ENTITY_DECK, deck.getId()));
    }

    @Override
    public void deleteById(String id) {
        executeWithExceptionHandling(() -> {
            Optional.ofNullable(id)
                    .filter(validId -> isNotBlank(validId))
                    .ifPresent(deckRepository::deleteById);
            return null;
        }, ErrorCode.DAO_DELETE_ERROR, DAO_DELETE_ERROR.formatted(ENTITY_DECK, id));
    }

    @Override
    public long count() {
        return executeWithExceptionHandling(
                deckRepository::count,
                ErrorCode.DAO_FIND_ERROR,
                DAO_COUNT_ERROR.formatted("decks")
        );
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, ErrorCode errorCode, String errorMessage) {
        try {
            return operation.get();
        } catch (DuplicateKeyException e) {
            log.error("Duplicate key error: {}", e.getMessage());
            throw new DaoException(DAO_DUPLICATE_ENTRY.formatted(ENTITY_DECK), ErrorCode.DAO_DUPLICATE_ERROR, e);
        } catch (DataAccessException e) {
            log.error("{}: {}", errorMessage, e.getMessage());
            throw new DaoException(errorMessage, errorCode, e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new DaoException(errorMessage, errorCode, e);
        }
    }
}