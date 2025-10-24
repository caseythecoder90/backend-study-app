package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dao.UserDao;
import com.flashcards.backend.flashcards.dto.*;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.mapper.UserMapper;
import com.flashcards.backend.flashcards.model.Role;
import com.flashcards.backend.flashcards.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.flashcards.backend.flashcards.constants.AuthConstants.TOTP_SETUP_REQUIRED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_CREDENTIALS_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_RECOVERY_CODE_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TotpService totpService;

    @Mock
    private RecoveryCodeService recoveryCodeService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDto testUserDto;
    private CreateUserDto createUserDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testUser = User.builder()
                .id("user-id-123")
                .username("testuser")
                .email("test@example.com")
                .password("encrypted-password")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .totpEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testUserDto = UserDto.builder()
                .id("user-id-123")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        createUserDto = CreateUserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        loginDto = LoginDto.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register with valid user returns AuthResponse")
        void register_ValidUser_ReturnsAuthResponse() {
            // Given
            when(userDao.existsByUsername(anyString())).thenReturn(false);
            when(userDao.existsByEmail(anyString())).thenReturn(false);
            when(passwordService.encryptPassword(anyString())).thenReturn("encrypted-password");
            when(userDao.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);

            // When
            AuthResponseDto response = authService.register(createUserDto);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("test-jwt-token");
            assertThat(response.getUser()).isEqualTo(testUserDto);
            assertThat(response.isTotpEnabled()).isFalse();

            verify(userDao).existsByUsername("testuser");
            verify(userDao).existsByEmail("test@example.com");
            verify(userDao).save(any(User.class));
            verify(jwtService).generateToken(any(User.class));
        }

        @Test
        @DisplayName("register with duplicate username throws ServiceException")
        void register_DuplicateUsername_ThrowsServiceException() {
            // Given
            when(userDao.existsByUsername("testuser")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.register(createUserDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("already exists")
                    .hasMessageContaining("username");

            verify(userDao).existsByUsername("testuser");
            verify(userDao, never()).save(any(User.class));
        }

        @Test
        @DisplayName("register with duplicate email throws ServiceException")
        void register_DuplicateEmail_ThrowsServiceException() {
            // Given
            when(userDao.existsByUsername(anyString())).thenReturn(false);
            when(userDao.existsByEmail("test@example.com")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.register(createUserDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("already exists")
                    .hasMessageContaining("email");

            verify(userDao).existsByEmail("test@example.com");
            verify(userDao, never()).save(any(User.class));
        }

        @Test
        @DisplayName("register normalizes email to lowercase")
        void register_NormalizesEmail() {
            // Given
            createUserDto.setEmail("Test@Example.COM");
            when(userDao.existsByUsername(anyString())).thenReturn(false);
            when(userDao.existsByEmail(anyString())).thenReturn(false);
            when(passwordService.encryptPassword(anyString())).thenReturn("encrypted-password");
            when(userDao.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);

            // When
            authService.register(createUserDto);

            // Then
            assertThat(createUserDto.getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("login with valid credentials returns AuthResponse")
        void login_ValidCredentials_ReturnsAuthResponse() {
            // Given
            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword("password123", "encrypted-password")).thenReturn(true);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);
            when(userDao.save(any(User.class))).thenReturn(testUser);

            // When
            AuthResponseDto response = authService.login(loginDto);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("test-jwt-token");
            assertThat(response.getUser()).isEqualTo(testUserDto);

            verify(userDao).findByUsername("testuser");
            verify(passwordService).verifyPassword("password123", "encrypted-password");
            verify(userDao).save(any(User.class)); // Updates lastLoginAt
        }

        @Test
        @DisplayName("login with invalid password throws ServiceException")
        void login_InvalidPassword_ThrowsServiceException() {
            // Given
            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword("wrong-password", "encrypted-password")).thenReturn(false);

            loginDto.setPassword("wrong-password");

            // When / Then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining(AUTH_CREDENTIALS_INVALID);

            verify(passwordService).verifyPassword("wrong-password", "encrypted-password");
            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("login with non-existent user throws ServiceException")
        void login_UserNotFound_ThrowsServiceException() {
            // Given
            when(userDao.findByUsername("nonexistent")).thenReturn(Optional.empty());
            when(userDao.findByEmail("nonexistent")).thenReturn(Optional.empty());

            loginDto.setUsernameOrEmail("nonexistent");

            // When / Then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining(AUTH_CREDENTIALS_INVALID);

            verify(userDao).findByUsername("nonexistent");
            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("login with disabled user throws ServiceException")
        void login_DisabledUser_ThrowsServiceException() {
            // Given
            testUser.setEnabled(false);
            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("disabled");

            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("login with TOTP enabled requires TOTP code")
        void login_WithTotpEnabled_RequiresTotpCode() {
            // Given
            testUser.setTotpEnabled(true);
            testUser.setTotpSecret("totp-secret");
            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("TOTP");

            verify(totpService, never()).validateTotpCode(anyString(), anyString());
        }

        @Test
        @DisplayName("login with TOTP enabled and valid code succeeds")
        void login_WithTotpEnabledAndValidCode_Succeeds() {
            // Given
            testUser.setTotpEnabled(true);
            testUser.setTotpSecret("totp-secret");
            loginDto.setTotpCode("123456");

            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);
            when(userDao.save(any(User.class))).thenReturn(testUser);
            doNothing().when(totpService).validateTotpCode("totp-secret", "123456");

            // When
            AuthResponseDto response = authService.login(loginDto);

            // Then
            assertThat(response).isNotNull();
            verify(totpService).validateTotpCode("totp-secret", "123456");
        }

        @Test
        @DisplayName("login with email finds user by email")
        void login_WithEmail_FindsUserByEmail() {
            // Given
            loginDto.setUsernameOrEmail("test@example.com");
            when(userDao.findByUsername("test@example.com")).thenReturn(Optional.empty());
            when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);
            when(userDao.save(any(User.class))).thenReturn(testUser);

            // When
            AuthResponseDto response = authService.login(loginDto);

            // Then
            assertThat(response).isNotNull();
            verify(userDao).findByUsername("test@example.com");
            verify(userDao).findByEmail("test@example.com");
        }
    }

    @Nested
    @DisplayName("TOTP Tests")
    class TotpTests {

        @Test
        @DisplayName("setupTotp returns TotpSetupDto with QR code and recovery codes")
        void setupTotp_ValidUser_ReturnsTotpSetupDto() {
            // Given
            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));
            when(totpService.generateSecret()).thenReturn("totp-secret");
            when(totpService.generateQrCodeImageUri("totp-secret", "testuser")).thenReturn("data:image/png;base64,...");
            when(recoveryCodeService.generateRecoveryCodes()).thenReturn(List.of("CODE1", "CODE2"));
            when(recoveryCodeService.hashRecoveryCodes(any())).thenReturn(List.of("hash1", "hash2"));
            when(recoveryCodeService.formatCodesForDisplay(any())).thenReturn(List.of("CODE-1", "CODE-2"));
            when(userDao.save(any(User.class))).thenReturn(testUser);

            // When
            TotpSetupDto result = authService.setupTotp("user-id-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSecret()).isEqualTo("totp-secret");
            assertThat(result.getQrCodeDataUri()).contains("data:image/png");
            assertThat(result.getRecoveryCodes()).hasSize(2);

            verify(userDao).save(any(User.class));
        }

        @Test
        @DisplayName("setupTotp with TOTP already enabled throws ServiceException")
        void setupTotp_TotpAlreadyEnabled_ThrowsServiceException() {
            // Given
            testUser.setTotpEnabled(true);
            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));

            // When / Then
            assertThatThrownBy(() -> authService.setupTotp("user-id-123"))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("already enabled");

            verify(totpService, never()).generateSecret();
        }

        @Test
        @DisplayName("enableTotp with valid code enables TOTP")
        void enableTotp_ValidCode_EnablesTotp() {
            // Given
            testUser.setTotpSecret("totp-secret");
            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));
            doNothing().when(totpService).validateTotpCode("totp-secret", "123456");
            when(userDao.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);

            // When
            AuthResponseDto response = authService.enableTotp("user-id-123", "123456");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.isTotpEnabled()).isTrue();

            verify(totpService).validateTotpCode("totp-secret", "123456");
            verify(userDao).save(argThat(user -> user.isTotpEnabled()));
        }

        @Test
        @DisplayName("enableTotp without setup throws ServiceException")
        void enableTotp_TotpNotSetup_ThrowsServiceException() {
            // Given
            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));

            // When / Then
            assertThatThrownBy(() -> authService.enableTotp("user-id-123", "123456"))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining(TOTP_SETUP_REQUIRED);

            verify(totpService, never()).validateTotpCode(anyString(), anyString());
        }

        @Test
        @DisplayName("disableTotp disables TOTP and clears recovery codes")
        void disableTotp_ValidUser_DisablesTotp() {
            // Given
            testUser.setTotpEnabled(true);
            testUser.setTotpSecret("totp-secret");
            testUser.setRecoveryCodeHashes(new HashSet<>(Set.of("hash1", "hash2")));

            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));
            when(userDao.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);

            // When
            AuthResponseDto response = authService.disableTotp("user-id-123");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.isTotpEnabled()).isFalse();

            verify(userDao).save(argThat(user ->
                    !user.isTotpEnabled() &&
                            user.getTotpSecret() == null &&
                            user.getRecoveryCodeHashes() == null
            ));
        }
    }

    @Nested
    @DisplayName("Recovery Code Tests")
    class RecoveryCodeTests {

        @Test
        @DisplayName("loginWithRecoveryCode with valid code returns AuthResponse")
        void loginWithRecoveryCode_ValidCode_ReturnsAuthResponse() {
            // Given
            testUser.setTotpEnabled(true);
            Set<String> hashedCodes = new HashSet<>(Set.of("hash1", "hash2", "hash3"));
            testUser.setRecoveryCodeHashes(hashedCodes);
            testUser.setRecoveryCodesUsedCount(0);

            RecoveryCodeLoginDto recoveryLoginDto = RecoveryCodeLoginDto.builder()
                    .usernameOrEmail("testuser")
                    .password("password123")
                    .recoveryCode("CODE1")
                    .build();

            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword("password123", "encrypted-password")).thenReturn(true);
            when(recoveryCodeService.getRemainingCodesCount(any())).thenReturn(3);
            when(recoveryCodeService.validateRecoveryCode("CODE1", hashedCodes)).thenReturn(true);
            when(recoveryCodeService.removeUsedCode(eq("CODE1"), any())).thenReturn(Set.of("hash2", "hash3"));
            when(userDao.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
            when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
            when(jwtService.getExpirationMs()).thenReturn(3600000L);

            // When
            AuthResponseDto response = authService.loginWithRecoveryCode(recoveryLoginDto);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("test-jwt-token");

            verify(recoveryCodeService).validateRecoveryCode("CODE1", hashedCodes);
            verify(recoveryCodeService).removeUsedCode(eq("CODE1"), any());
            verify(userDao).save(any(User.class)); // Single save in updateLastLogin includes all changes
        }

        @Test
        @DisplayName("loginWithRecoveryCode with invalid code throws ServiceException")
        void loginWithRecoveryCode_InvalidCode_ThrowsServiceException() {
            // Given
            testUser.setTotpEnabled(true);
            testUser.setRecoveryCodeHashes(new HashSet<>(Set.of("hash1")));

            RecoveryCodeLoginDto recoveryLoginDto = RecoveryCodeLoginDto.builder()
                    .usernameOrEmail("testuser")
                    .password("password123")
                    .recoveryCode("INVALID")
                    .build();

            when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
            when(recoveryCodeService.getRemainingCodesCount(any())).thenReturn(1);
            when(recoveryCodeService.validateRecoveryCode(anyString(), any())).thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> authService.loginWithRecoveryCode(recoveryLoginDto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining(AUTH_RECOVERY_CODE_INVALID);

            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("regenerateRecoveryCodes returns new codes")
        void regenerateRecoveryCodes_ValidUser_ReturnsNewCodes() {
            // Given
            testUser.setTotpEnabled(true);
            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));
            when(recoveryCodeService.generateRecoveryCodes()).thenReturn(List.of("NEW1", "NEW2"));
            when(recoveryCodeService.hashRecoveryCodes(any())).thenReturn(List.of("newhash1", "newhash2"));
            when(recoveryCodeService.formatCodesForDisplay(any())).thenReturn(List.of("NEW-1", "NEW-2"));
            when(userDao.save(any(User.class))).thenReturn(testUser);

            // When
            RecoveryCodesDto result = authService.regenerateRecoveryCodes("user-id-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCodes()).hasSize(2);
            assertThat(result.getRemainingCodes()).isEqualTo(2);
            assertThat(result.getUsedCodes()).isZero();

            verify(userDao).save(any(User.class));
        }

        @Test
        @DisplayName("getRecoveryCodeStatus returns status without codes")
        void getRecoveryCodeStatus_ValidUser_ReturnsStatus() {
            // Given
            testUser.setTotpEnabled(true);
            testUser.setRecoveryCodeHashes(new HashSet<>(Set.of("hash1", "hash2", "hash3", "hash4")));
            testUser.setRecoveryCodesUsedCount(0);

            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));
            when(recoveryCodeService.getRemainingCodesCount(any())).thenReturn(4);

            // When
            RecoveryCodeStatusDto result = authService.getRecoveryCodeStatus("user-id-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRemainingCodes()).isEqualTo(4);
            assertThat(result.getUsedCodes()).isZero();
            assertThat(result.getWarning()).isNull(); // No warning when > threshold
        }

        @Test
        @DisplayName("getRecoveryCodeStatus with low codes returns warning")
        void getRecoveryCodeStatus_WithLowCodes_ReturnsWarning() {
            // Given
            testUser.setTotpEnabled(true);
            testUser.setRecoveryCodeHashes(new HashSet<>(Set.of("hash1", "hash2")));
            testUser.setRecoveryCodesUsedCount(8);

            when(userDao.findById("user-id-123")).thenReturn(Optional.of(testUser));
            when(recoveryCodeService.getRemainingCodesCount(any())).thenReturn(2);

            // When
            RecoveryCodeStatusDto result = authService.getRecoveryCodeStatus("user-id-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRemainingCodes()).isEqualTo(2);
            assertThat(result.getWarning()).isNotNull();
            assertThat(result.getWarning()).contains("2");
        }
    }
}
