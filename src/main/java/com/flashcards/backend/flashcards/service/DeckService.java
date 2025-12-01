package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dao.DeckDao;
import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.dto.CreateDeckDto;
import com.flashcards.backend.flashcards.dto.DeckDto;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.mapper.DeckMapper;
import com.flashcards.backend.flashcards.model.Deck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_DECK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_DECKS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_USER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_DECK_OWNERSHIP_DENIED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_ENTITY_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_OPERATION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_VALIDATION_FAILED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeckService {
    private final DeckDao deckDao;
    private final UserDao userDao;
    private final DeckMapper deckMapper;

    @Transactional(readOnly = true)
    public Optional<DeckDto> findById(String id) {
        return executeWithExceptionHandling(() ->
            deckDao.findById(id).map(deckMapper::toDto),
            () -> SERVICE_OPERATION_FAILED.formatted("find", ENTITY_DECK)
        );
    }

    @Transactional(readOnly = true)
    public List<DeckDto> findByUserId(String userId) {
        return executeWithExceptionHandling(() ->
            deckMapper.toDtoList(deckDao.findByUserId(userId)),
            () -> SERVICE_OPERATION_FAILED.formatted("find by user", ENTITY_DECKS)
        );
    }

    @Transactional(readOnly = true)
    public List<DeckDto> findPublicDecks() {
        return executeWithExceptionHandling(() ->
            deckMapper.toDtoList(deckDao.findByIsPublicTrue()),
            () -> SERVICE_OPERATION_FAILED.formatted("find public", ENTITY_DECKS)
        );
    }

    @Transactional(readOnly = true)
    public List<DeckDto> findByCategory(String category) {
        return executeWithExceptionHandling(() ->
            deckMapper.toDtoList(deckDao.findByCategory(category)),
            () -> SERVICE_OPERATION_FAILED.formatted("find by category", ENTITY_DECKS)
        );
    }

    @Transactional(readOnly = true)
    public List<DeckDto> findByTag(String tag) {
        return executeWithExceptionHandling(() ->
            deckMapper.toDtoList(deckDao.findByTagsContaining(tag)),
            () -> SERVICE_OPERATION_FAILED.formatted("find by tag", ENTITY_DECKS)
        );
    }

    @Transactional(readOnly = true)
    public List<DeckDto> findByUserIdAndVisibility(String userId, boolean isPublic) {
        return executeWithExceptionHandling(() ->
            deckMapper.toDtoList(deckDao.findByUserIdAndIsPublic(userId, isPublic)),
            () -> SERVICE_OPERATION_FAILED.formatted("find by user and visibility", ENTITY_DECKS)
        );
    }

    @Transactional(readOnly = true)
    public List<DeckDto> findAll() {
        return executeWithExceptionHandling(() ->
            deckMapper.toDtoList(deckDao.findAll()),
            () -> SERVICE_OPERATION_FAILED.formatted("find all", ENTITY_DECKS)
        );
    }

    public DeckDto createDeck(CreateDeckDto createDeckDto) {
        return executeWithExceptionHandling(() -> {
            validateUserExists(createDeckDto.getUserId());

            Deck deck = deckMapper.toEntity(createDeckDto);
            Deck savedDeck = deckDao.save(deck);
            return deckMapper.toDto(savedDeck);
        }, () -> SERVICE_OPERATION_FAILED.formatted("create", ENTITY_DECK));
    }

    public DeckDto updateDeck(String id, DeckDto deckDto) {
        return executeWithExceptionHandling(() -> {
            Deck existingDeck = deckDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_DECK, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            deckMapper.updateEntity(existingDeck, deckDto);
            Deck updatedDeck = deckDao.update(existingDeck);
            return deckMapper.toDto(updatedDeck);
        }, () -> SERVICE_OPERATION_FAILED.formatted("update", ENTITY_DECK));
    }

    public void deleteDeck(String id) {
        executeWithExceptionHandling(() -> {
            Deck existingDeck = deckDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_DECK, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            deckDao.deleteById(id);
            return null;
        }, () -> SERVICE_OPERATION_FAILED.formatted("delete", ENTITY_DECK));
    }

    @Transactional(readOnly = true)
    public long countDecks() {
        return executeWithExceptionHandling(deckDao::count,
                () -> SERVICE_OPERATION_FAILED.formatted("count", ENTITY_DECKS));
    }

    /**
     * Validates that a deck belongs to a specific user.
     * Used for authorization checks before allowing access to deck flashcards.
     *
     * @param deckId the deck ID to validate
     * @param userId the user ID that should own the deck
     * @throws ServiceException if deck not found or doesn't belong to user
     */
    public void validateDeckBelongsToUser(String deckId, String userId) {
        Deck deck = deckDao.findById(deckId)
                .orElseThrow(() -> new ServiceException(
                        SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_DECK, deckId),
                        ErrorCode.SERVICE_NOT_FOUND
                ));

        if (isFalse(deck.getUserId().equals(userId))) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_DECK, SERVICE_DECK_OWNERSHIP_DENIED),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }
    }

    private void validateUserExists(String userId) {
        boolean userExists = userDao.findById(userId).isPresent();
        if (isFalse(userExists)) {
            throw new ServiceException(
                    SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_USER, userId),
                    ErrorCode.SERVICE_NOT_FOUND
            );
        }
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, Supplier<String> errorMessageSupplier) {
        try {
            return operation.get();
        } catch (DaoException e) {
            log.error("DAO error in DeckService: {}", e.getMessage());
            throw new ServiceException(errorMessageSupplier.get(), ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR, e);
        } catch (ServiceException e) {
            log.error("Service error in DeckService: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in DeckService: {}", e.getMessage());
            throw new ServiceException(errorMessageSupplier.get(), ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR, e);
        }
    }
}