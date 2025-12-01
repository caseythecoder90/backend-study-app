package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dao.DeckDao;
import com.flashcards.backend.flashcards.dao.FlashcardDao;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.mapper.FlashcardMapper;
import com.flashcards.backend.flashcards.model.Flashcard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_DECK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_FLASHCARD;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_FLASHCARDS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_COUNT_ALL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_COUNT_BY_DECK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_CREATE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_CREATE_MULTIPLE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_DELETE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_DELETE_BY_DECK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND_ALL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND_BY_DECK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND_BY_DECK_AND_DIFFICULTY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND_BY_TAG;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND_BY_USER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_FIND_BY_USER_AND_TAG;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_UPDATE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.OPERATION_UPDATE_STUDY_STATS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_ENTITY_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_OPERATION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_VALIDATION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_BACK_CONTENT_REQUIRED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_CREATION_DATA_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_DATA_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_DECK_ID_REQUIRED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_DIFFICULTY_LEVEL_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_FRONT_CONTENT_REQUIRED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_ID_BLANK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_TAG_BLANK;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.VALIDATION_USER_ID_REQUIRED;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardService {
    private final FlashcardDao flashcardDao;
    private final DeckDao deckDao;
    private final FlashcardMapper flashcardMapper;

    @Transactional(readOnly = true)
    public Optional<FlashcardDto> findById(String id) {
        return executeWithExceptionHandling(() -> {
            validateId(id);
            return flashcardDao.findById(id)
                    .map(flashcardMapper::toDto);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> findByDeckId(String deckId) {
        return executeWithExceptionHandling(() -> {
            validateId(deckId);
            return flashcardMapper.toDtoList(flashcardDao.findByDeckId(deckId));
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_DECK, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> findByUserId(String userId) {
        return executeWithExceptionHandling(() -> {
            validateId(userId);
            return flashcardMapper.toDtoList(flashcardDao.findByUserId(userId));
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_USER, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty) {
        return executeWithExceptionHandling(() -> {
            validateId(deckId);
            requireNonNull(difficulty, VALIDATION_DIFFICULTY_LEVEL_NULL);
            return flashcardMapper.toDtoList(flashcardDao.findByDeckIdAndDifficulty(deckId, difficulty));
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_DECK_AND_DIFFICULTY, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> findByTagsContaining(String tag) {
        return executeWithExceptionHandling(() -> {
            validateTag(tag);
            return flashcardMapper.toDtoList(flashcardDao.findByTagsContaining(tag));
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_TAG, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> findAll() {
        return executeWithExceptionHandling(() ->
                flashcardMapper.toDtoList(flashcardDao.findAll()),
                () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_ALL, ENTITY_FLASHCARDS)
        );
    }

    // Paginated query methods

    @Transactional(readOnly = true)
    public Page<FlashcardDto> findByUserId(String userId, Pageable pageable) {
        return executeWithExceptionHandling(() -> {
            validateId(userId);
            return flashcardDao.findByUserId(userId, pageable)
                    .map(flashcardMapper::toDto);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_USER, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public Page<FlashcardDto> findByDeckId(String deckId, Pageable pageable) {
        return executeWithExceptionHandling(() -> {
            validateId(deckId);
            return flashcardDao.findByDeckId(deckId, pageable)
                    .map(flashcardMapper::toDto);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_DECK, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public Page<FlashcardDto> findByDeckIdAndDifficulty(String deckId, Flashcard.DifficultyLevel difficulty, Pageable pageable) {
        return executeWithExceptionHandling(() -> {
            validateId(deckId);
            requireNonNull(difficulty, VALIDATION_DIFFICULTY_LEVEL_NULL);
            return flashcardDao.findByDeckIdAndDifficulty(deckId, difficulty, pageable)
                    .map(flashcardMapper::toDto);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_DECK_AND_DIFFICULTY, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public Page<FlashcardDto> findByUserIdAndTagsContaining(String userId, String tag, Pageable pageable) {
        return executeWithExceptionHandling(() -> {
            validateId(userId);
            validateTag(tag);
            return flashcardDao.findByUserIdAndTagsContaining(userId, tag, pageable)
                    .map(flashcardMapper::toDto);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_BY_USER_AND_TAG, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public Page<FlashcardDto> findAll(Pageable pageable) {
        return executeWithExceptionHandling(() ->
                flashcardDao.findAll(pageable)
                        .map(flashcardMapper::toDto),
                () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_FIND_ALL, ENTITY_FLASHCARDS)
        );
    }

    public FlashcardDto createFlashcard(CreateFlashcardDto createFlashcardDto) {
        return executeWithExceptionHandling(() -> {
            validateCreateFlashcardDto(createFlashcardDto);
            validateDeckExists(createFlashcardDto.getDeckId());

            Flashcard flashcard = flashcardMapper.toEntity(createFlashcardDto);
            Flashcard savedFlashcard = flashcardDao.save(flashcard);

            updateDeckCount(createFlashcardDto.getDeckId());
            return flashcardMapper.toDto(savedFlashcard);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_CREATE, ENTITY_FLASHCARD));
    }

    public List<FlashcardDto> createMultipleFlashcards(List<CreateFlashcardDto> createFlashcardDtos) {
        return executeWithExceptionHandling(() -> {
            if (isEmpty(createFlashcardDtos)) {
                return Collections.emptyList();
            }

            // Validate all DTOs first
            createFlashcardDtos.forEach(this::validateCreateFlashcardDto);

            // Validate deck exists for each unique deck ID
            createFlashcardDtos.stream()
                    .map(CreateFlashcardDto::getDeckId)
                    .distinct()
                    .forEach(this::validateDeckExists);

            List<Flashcard> flashcards = createFlashcardDtos.stream()
                    .map(flashcardMapper::toEntity)
                    .toList();

            List<Flashcard> savedFlashcards = flashcardDao.saveAll(flashcards);

            // Update deck counts for all affected decks
            createFlashcardDtos.stream()
                    .map(CreateFlashcardDto::getDeckId)
                    .distinct()
                    .forEach(this::updateDeckCount);

            return flashcardMapper.toDtoList(savedFlashcards);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_CREATE_MULTIPLE, ENTITY_FLASHCARD));
    }

    public FlashcardDto updateFlashcard(String id, FlashcardDto flashcardDto) {
        return executeWithExceptionHandling(() -> {
            validateId(id);
            validateFlashcardDto(flashcardDto);

            Flashcard existingFlashcard = flashcardDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_FLASHCARD, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            flashcardMapper.updateEntity(existingFlashcard, flashcardDto);
            Flashcard updatedFlashcard = flashcardDao.update(existingFlashcard);
            return flashcardMapper.toDto(updatedFlashcard);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_UPDATE, ENTITY_FLASHCARD));
    }

    public void deleteFlashcard(String id) {
        executeWithExceptionHandling(() -> {
            validateId(id);

            Flashcard flashcard = flashcardDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_FLASHCARD, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            flashcardDao.deleteById(id);
            updateDeckCount(flashcard.getDeckId());
            return null;
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_DELETE, ENTITY_FLASHCARD));
    }

    public void deleteFlashcardsByDeckId(String deckId) {
        executeWithExceptionHandling(() -> {
            validateId(deckId);
            flashcardDao.deleteByDeckId(deckId);
            updateDeckCount(deckId);
            return null;
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_DELETE_BY_DECK, ENTITY_FLASHCARD));
    }

    public void updateStudyStats(String id, boolean correct) {
        executeWithExceptionHandling(() -> {
            validateId(id);

            Flashcard flashcard = flashcardDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_FLASHCARD, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            flashcard.setTimesStudied(flashcard.getTimesStudied() + 1);

            if (correct) {
                flashcard.setTimesCorrect(flashcard.getTimesCorrect() + 1);
            } else {
                flashcard.setTimesIncorrect(flashcard.getTimesIncorrect() + 1);
            }

            flashcard.setLastStudiedAt(LocalDateTime.now());
            flashcardDao.update(flashcard);
            return null;
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_UPDATE_STUDY_STATS, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public long countByDeckId(String deckId) {
        return executeWithExceptionHandling(() -> {
            validateId(deckId);
            return flashcardDao.countByDeckId(deckId);
        }, () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_COUNT_BY_DECK, ENTITY_FLASHCARD));
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return executeWithExceptionHandling(flashcardDao::count,
                () -> SERVICE_OPERATION_FAILED.formatted(OPERATION_COUNT_ALL, ENTITY_FLASHCARDS));
    }

    private void validateId(String id) {
        if (isBlank(id)) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_FLASHCARD, VALIDATION_ID_BLANK),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }
    }

    private void validateTag(String tag) {
        if (isBlank(tag)) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_FLASHCARD, VALIDATION_TAG_BLANK),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }
    }

    private void validateCreateFlashcardDto(CreateFlashcardDto createFlashcardDto) {
        requireNonNull(createFlashcardDto, VALIDATION_CREATION_DATA_NULL.formatted(ENTITY_FLASHCARD));

        if (isBlank(createFlashcardDto.getDeckId())) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_FLASHCARD, VALIDATION_DECK_ID_REQUIRED),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        if (isBlank(createFlashcardDto.getUserId())) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_FLASHCARD, VALIDATION_USER_ID_REQUIRED),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        if (isNull(createFlashcardDto.getFront()) ||
            isBlank(createFlashcardDto.getFront().getText())) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_FLASHCARD, VALIDATION_FRONT_CONTENT_REQUIRED),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        if (isNull(createFlashcardDto.getBack()) ||
            isBlank(createFlashcardDto.getBack().getText())) {
            throw new ServiceException(
                    SERVICE_VALIDATION_FAILED.formatted(ENTITY_FLASHCARD, VALIDATION_BACK_CONTENT_REQUIRED),
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }
    }

    private void validateFlashcardDto(FlashcardDto flashcardDto) {
        requireNonNull(flashcardDto, VALIDATION_DATA_NULL.formatted(ENTITY_FLASHCARD));
    }

    private void validateDeckExists(String deckId) {
        if (deckDao.findById(deckId).isEmpty()) {
            throw new ServiceException(
                    SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_DECK, deckId),
                    ErrorCode.SERVICE_NOT_FOUND
            );
        }
    }

    private void updateDeckCount(String deckId) {
        try {
            long count = flashcardDao.countByDeckId(deckId);
            deckDao.findById(deckId).ifPresent(deck -> {
                deck.setFlashcardCount((int) count);
                deck.setUpdatedAt(LocalDateTime.now());
                deckDao.update(deck);
            });
        } catch (Exception e) {
            log.warn("Failed to update deck count for deck {}: {}", deckId, e.getMessage());
            // Don't fail the main operation if deck count update fails
        }
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, Supplier<String> errorMessageSupplier) {
        try {
            return operation.get();
        } catch (DaoException e) {
            log.error("DAO error in FlashcardService: {}", e.getMessage());
            throw new ServiceException(errorMessageSupplier.get(), ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR, e);
        } catch (ServiceException e) {
            log.error("Service error in FlashcardService: {}", e.getMessage());
            throw e; // Re-throw service exceptions as-is
        } catch (Exception e) {
            log.error("Unexpected error in FlashcardService: {}", e.getMessage());
            throw new ServiceException(errorMessageSupplier.get(), ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR, e);
        }
    }
}