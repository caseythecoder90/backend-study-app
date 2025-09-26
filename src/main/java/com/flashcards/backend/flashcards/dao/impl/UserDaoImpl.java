package com.flashcards.backend.flashcards.dao.impl;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import com.flashcards.backend.flashcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_COUNT_BY_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_COUNT_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DELETE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_DUPLICATE_ENTRY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ENTITY_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ENTITY_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_EXISTS_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_ALL_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_BY_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_FIND_BY_ID_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_ID_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_SAVE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DAO_UPDATE_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_USER;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(String id) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(id)
                        .filter(str -> isNotBlank(str))
                        .flatMap(userRepository::findById),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_ID_ERROR.formatted(ENTITY_USER, id)
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(username)
                        .filter(str -> isNotBlank(str))
                        .flatMap(userRepository::findByUsername),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_USER, "username", username)
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(email)
                        .filter(str -> isNotBlank(str))
                        .flatMap(userRepository::findByEmail),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_USER, "email", email)
        );
    }

    @Override
    public List<User> findAll() {
        return executeWithExceptionHandling(
                userRepository::findAll,
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_ALL_ERROR.formatted("users")
        );
    }

    @Override
    public User save(User user) {
        return executeWithExceptionHandling(() -> {
            requireNonNull(user, DAO_ENTITY_NULL.formatted(ENTITY_USER));

            return Optional.of(user)
                    .map(u -> {
                        u.setCreatedAt(LocalDateTime.now());
                        u.setUpdatedAt(LocalDateTime.now());
                        return u;
                    })
                    .map(userRepository::save)
                    .orElseThrow(() -> new DaoException(DAO_SAVE_ERROR.formatted(ENTITY_USER), ErrorCode.DAO_SAVE_ERROR));
        }, ErrorCode.DAO_SAVE_ERROR, DAO_SAVE_ERROR.formatted(ENTITY_USER));
    }

    @Override
    public User update(User user) {
        return executeWithExceptionHandling(() -> {
            requireNonNull(user, DAO_ENTITY_NULL.formatted(ENTITY_USER));
            requireNonNull(user.getId(), DAO_ID_NULL.formatted(ENTITY_USER));

            return findById(user.getId())
                    .map(existing -> {
                        user.setCreatedAt(existing.getCreatedAt());
                        user.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(user);
                    })
                    .orElseThrow(() -> new DaoException(
                            DAO_ENTITY_NOT_FOUND.formatted(ENTITY_USER, user.getId()),
                            ErrorCode.DAO_UPDATE_ERROR
                    ));
        }, ErrorCode.DAO_UPDATE_ERROR, DAO_UPDATE_ERROR.formatted(ENTITY_USER, user.getId()));
    }

    @Override
    public void deleteById(String id) {
        executeWithExceptionHandling(() -> {
            Optional.ofNullable(id)
                    .filter(str -> isNotBlank(str))
                    .ifPresent(userRepository::deleteById);
            return null;
        }, ErrorCode.DAO_DELETE_ERROR, DAO_DELETE_ERROR.formatted(ENTITY_USER, id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(username)
                        .filter(str -> isNotBlank(str))
                        .map(userRepository::existsByUsername)
                        .orElse(false),
                ErrorCode.DAO_FIND_ERROR,
                DAO_EXISTS_ERROR.formatted(ENTITY_USER, "username", username)
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(email)
                        .filter(str -> isNotBlank(str))
                        .map(userRepository::existsByEmail)
                        .orElse(false),
                ErrorCode.DAO_FIND_ERROR,
                DAO_EXISTS_ERROR.formatted(ENTITY_USER, "email", email)
        );
    }

    @Override
    public Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId) {
        return executeWithExceptionHandling(() ->
                Optional.ofNullable(oauthProvider)
                        .filter(str -> isNotBlank(str))
                        .flatMap(provider -> Optional.ofNullable(oauthId)
                                .filter(str -> isNotBlank(str))
                                .flatMap(id -> userRepository.findByOauthProviderAndOauthId(provider, id))),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_USER, "oauthProvider and oauthId", oauthProvider + ":" + oauthId)
        );
    }

    @Override
    public List<User> findByRole(Role role) {
        return executeWithExceptionHandling(
                () -> userRepository.findByRolesContaining(role),
                ErrorCode.DAO_FIND_ERROR,
                DAO_FIND_BY_FIELD_ERROR.formatted(ENTITY_USER, "role", role)
        );
    }

    @Override
    public long count() {
        return executeWithExceptionHandling(
                userRepository::count,
                ErrorCode.DAO_FIND_ERROR,
                DAO_COUNT_ERROR.formatted("users")
        );
    }

    @Override
    public long countByRole(Role role) {
        return executeWithExceptionHandling(
                () -> userRepository.countByRolesContaining(role),
                ErrorCode.DAO_FIND_ERROR,
                DAO_COUNT_BY_FIELD_ERROR.formatted(ENTITY_USER, "role", role)
        );
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, ErrorCode errorCode, String errorMessage) {
        try {
            return operation.get();
        } catch (DuplicateKeyException e) {
            log.error("Duplicate key error: {}", e.getMessage());
            throw new DaoException(DAO_DUPLICATE_ENTRY.formatted(ENTITY_USER), ErrorCode.DAO_DUPLICATE_ERROR, e);
        } catch (DataAccessException e) {
            log.error("{}: {}", errorMessage, e.getMessage());
            throw new DaoException(errorMessage, errorCode, e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new DaoException(errorMessage, errorCode, e);
        }
    }
}