package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.UserDto;
import com.flashcards.backend.flashcards.exception.DaoException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.mapper.UserMapper;
import com.flashcards.backend.flashcards.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_USER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_DUPLICATE_EXISTS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_ENTITY_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_OPERATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserDao userDao;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Optional<UserDto> findById(String id) {
        return executeWithExceptionHandling(() ->
            userDao.findById(id).map(userMapper::toDto),
            () -> SERVICE_OPERATION_FAILED.formatted("find", ENTITY_USER)
        );
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findByUsername(String username) {
        return executeWithExceptionHandling(() ->
            userDao.findByUsername(username).map(userMapper::toDto),
            () -> SERVICE_OPERATION_FAILED.formatted("find", ENTITY_USER)
        );
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        return executeWithExceptionHandling(() ->
            userDao.findByEmail(email).map(userMapper::toDto),
            () -> SERVICE_OPERATION_FAILED.formatted("find", ENTITY_USER)
        );
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return executeWithExceptionHandling(() ->
                userMapper.toDtoList(userDao.findAll()),
                () -> SERVICE_OPERATION_FAILED.formatted("find all", "users")
        );
    }

    public UserDto createUser(CreateUserDto createUserDto) {
        return executeWithExceptionHandling(() -> {
            // Business logic validation - check uniqueness
            checkUserDoesNotExist(createUserDto);

            User user = userMapper.toEntity(createUserDto);
            User savedUser = userDao.save(user);
            return userMapper.toDto(savedUser);
        }, () -> SERVICE_OPERATION_FAILED.formatted("create", ENTITY_USER));
    }

    public UserDto updateUser(String id, UserDto userDto) {
        return executeWithExceptionHandling(() -> {
            User existingUser = userDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_USER, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            userMapper.updateEntity(existingUser, userDto);
            User updatedUser = userDao.update(existingUser);
            return userMapper.toDto(updatedUser);
        }, () -> SERVICE_OPERATION_FAILED.formatted("update", ENTITY_USER));
    }

    public void deleteUser(String id) {
        executeWithExceptionHandling(() -> {
            User existingUser = userDao.findById(id)
                    .orElseThrow(() -> new ServiceException(
                            SERVICE_ENTITY_NOT_FOUND.formatted(ENTITY_USER, id),
                            ErrorCode.SERVICE_NOT_FOUND
                    ));

            userDao.deleteById(id);
            return null;
        }, () -> SERVICE_OPERATION_FAILED.formatted("delete", ENTITY_USER));
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return executeWithExceptionHandling(() ->
            userDao.existsByUsername(username),
            () -> SERVICE_OPERATION_FAILED.formatted("check existence for", ENTITY_USER)
        );
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return executeWithExceptionHandling(() ->
            userDao.existsByEmail(email),
            () -> SERVICE_OPERATION_FAILED.formatted("check existence for", ENTITY_USER)
        );
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return executeWithExceptionHandling(userDao::count,
                () -> SERVICE_OPERATION_FAILED.formatted("count", "users"));
    }

    // Business logic validation - checks that can't be done with annotations

    private void checkUserDoesNotExist(CreateUserDto createUserDto) {
        if (userDao.existsByUsername(createUserDto.getUsername())) {
            throw new ServiceException(
                    SERVICE_DUPLICATE_EXISTS.formatted(ENTITY_USER, "username", createUserDto.getUsername()),
                    ErrorCode.SERVICE_DUPLICATE_ERROR
            );
        }

        if (userDao.existsByEmail(createUserDto.getEmail())) {
            throw new ServiceException(
                    SERVICE_DUPLICATE_EXISTS.formatted(ENTITY_USER, "email", createUserDto.getEmail()),
                    ErrorCode.SERVICE_DUPLICATE_ERROR
            );
        }
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation, Supplier<String> errorMessageSupplier) {
        try {
            return operation.get();
        } catch (DaoException e) {
            log.error("DAO error in UserService: {}", e.getMessage());
            throw new ServiceException(errorMessageSupplier.get(), ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR, e);
        } catch (ServiceException e) {
            log.error("Service error in UserService: {}", e.getMessage());
            throw e; // Re-throw service exceptions as-is
        } catch (Exception e) {
            log.error("Unexpected error in UserService: {}", e.getMessage());
            throw new ServiceException(errorMessageSupplier.get(), ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR, e);
        }
    }
}