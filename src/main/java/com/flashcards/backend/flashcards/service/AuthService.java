package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.dto.AuthResponseDto;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.LoginDto;
import com.flashcards.backend.flashcards.dto.TotpSetupDto;
import com.flashcards.backend.flashcards.dto.UserDto;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.mapper.UserMapper;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_CREDENTIALS_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOTP_CODE_REQUIRED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_USER_DISABLED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_DUPLICATE_EXISTS;
import static com.flashcards.backend.flashcards.constants.JwtConstants.JWT_TOKEN_TYPE;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final TotpService totpService;

    public AuthResponseDto register(CreateUserDto createUserDto) {
        log.debug("Registering new user: {}", createUserDto.getUsername());

        validateUserDoesNotExist(createUserDto);

        User user = createNewUser(createUserDto);
        User savedUser = userDao.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        UserDto userDto = userMapper.toDto(savedUser);

        log.debug("User registered successfully: {}", savedUser.getUsername());
        return buildAuthResponse(accessToken, userDto, savedUser.isTotpEnabled());
    }

    public AuthResponseDto login(LoginDto loginDto) {
        log.debug("Authenticating user: {}", loginDto.getUsernameOrEmail());

        User user = findUserByUsernameOrEmail(loginDto.getUsernameOrEmail());
        validateUserCredentials(user, loginDto);
        validateUserAccount(user);

        if (BooleanUtils.isTrue(user.isTotpEnabled())) {
            if (BooleanUtils.isTrue(isBlank(loginDto.getTotpCode()))) {
                throw new ServiceException(AUTH_TOTP_CODE_REQUIRED, ErrorCode.AUTH_TOTP_REQUIRED);
            }
            validateTotpCode(user, loginDto.getTotpCode());
        }

        updateLastLogin(user);
        String accessToken = jwtService.generateToken(user);
        UserDto userDto = userMapper.toDto(user);

        log.debug("User authenticated successfully: {}", user.getUsername());
        return buildAuthResponse(accessToken, userDto, user.isTotpEnabled());
    }
    private void validateUserDoesNotExist(CreateUserDto createUserDto) {
        if (BooleanUtils.isTrue(userDao.existsByUsername(createUserDto.getUsername()))) {
            throw new ServiceException(
                    SERVICE_DUPLICATE_EXISTS.formatted("User", "username", createUserDto.getUsername()),
                    ErrorCode.SERVICE_DUPLICATE_ERROR
            );
        }
        if (BooleanUtils.isTrue(userDao.existsByEmail(createUserDto.getEmail()))) {
            throw new ServiceException(
                    SERVICE_DUPLICATE_EXISTS.formatted("User", "email", createUserDto.getEmail()),
                    ErrorCode.SERVICE_DUPLICATE_ERROR
            );
        }
    }

    private User createNewUser(CreateUserDto createUserDto) {
        String encryptedPassword = passwordService.encryptPassword(createUserDto.getPassword());
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .username(createUserDto.getUsername())
                .email(createUserDto.getEmail())
                .password(encryptedPassword)
                .firstName(createUserDto.getFirstName())
                .lastName(createUserDto.getLastName())
                .roles(Set.of(Role.USER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .totpEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> userOpt = userDao.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userDao.findByEmail(usernameOrEmail);
        }

        return userOpt.orElseThrow(() -> new ServiceException(
                AUTH_CREDENTIALS_INVALID,
                ErrorCode.AUTH_INVALID_CREDENTIALS
        ));
    }

    private void validateUserCredentials(User user, LoginDto loginDto) {
        if (BooleanUtils.isFalse(passwordService.verifyPassword(loginDto.getPassword(), user.getPassword()))) {
            throw new ServiceException(AUTH_CREDENTIALS_INVALID, ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    private void validateUserAccount(User user) {
        if (BooleanUtils.isFalse(user.isEnabled())) {
            throw new ServiceException(AUTH_USER_DISABLED, ErrorCode.AUTH_USER_DISABLED);
        }
    }

    private void validateTotpCode(User user, String totpCode) {
        if (isNotBlank(user.getTotpSecret())) {
            totpService.validateTotpCode(user.getTotpSecret(), totpCode);
            log.debug("TOTP validation successful for user: {}", user.getUsername());
        } else {
            log.warn("TOTP validation attempted but no secret found for user: {}", user.getUsername());
            throw new ServiceException(AUTH_CREDENTIALS_INVALID, ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    private void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userDao.save(user);
    }

    public TotpSetupDto setupTotp(String userId) {
        log.debug("Setting up TOTP for user: {}", userId);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ServiceException(AUTH_CREDENTIALS_INVALID, ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (BooleanUtils.isTrue(user.isTotpEnabled())) {
            throw new ServiceException(
                    "TOTP is already enabled for this user",
                    ErrorCode.SERVICE_DUPLICATE_ERROR
            );
        }

        String secret = totpService.generateSecret();
        String qrCodeDataUri = totpService.generateQrCodeImageUri(secret, user.getUsername());

        user.setTotpSecret(secret);
        user.setUpdatedAt(LocalDateTime.now());
        userDao.save(user);

        log.debug("TOTP setup completed for user: {}", userId);
        return TotpSetupDto.builder()
                .secret(secret)
                .qrCodeDataUri(qrCodeDataUri)
                .instructions("Scan this QR code with your authenticator app (Google Authenticator, Authy, etc.) or enter the secret manually")
                .build();
    }

    public AuthResponseDto enableTotp(String userId, String totpCode) {
        log.debug("Enabling TOTP for user: {}", userId);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ServiceException(AUTH_CREDENTIALS_INVALID, ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (BooleanUtils.isTrue(user.isTotpEnabled())) {
            throw new ServiceException(
                    "TOTP is already enabled for this user",
                    ErrorCode.SERVICE_DUPLICATE_ERROR
            );
        }

        if (BooleanUtils.isFalse(isNotBlank(user.getTotpSecret()))) {
            throw new ServiceException(
                    "TOTP setup must be completed before enabling",
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        totpService.validateTotpCode(user.getTotpSecret(), totpCode);

        user.setTotpEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userDao.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        UserDto userDto = userMapper.toDto(savedUser);

        log.debug("TOTP enabled successfully for user: {}", userId);
        return buildAuthResponse(accessToken, userDto, true);
    }

    public AuthResponseDto disableTotp(String userId) {
        log.debug("Disabling TOTP for user: {}", userId);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ServiceException(AUTH_CREDENTIALS_INVALID, ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (BooleanUtils.isFalse(user.isTotpEnabled())) {
            throw new ServiceException(
                    "TOTP is not enabled for this user",
                    ErrorCode.SERVICE_NOT_FOUND
            );
        }

        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userDao.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        UserDto userDto = userMapper.toDto(savedUser);

        log.debug("TOTP disabled successfully for user: {}", userId);
        return buildAuthResponse(accessToken, userDto, false);
    }

    private AuthResponseDto buildAuthResponse(String accessToken, UserDto userDto, boolean totpEnabled) {
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .tokenType(JWT_TOKEN_TYPE)
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(userDto)
                .totpEnabled(totpEnabled)
                .build();
    }
}