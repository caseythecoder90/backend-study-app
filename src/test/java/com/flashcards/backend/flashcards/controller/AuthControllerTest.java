package com.flashcards.backend.flashcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashcards.backend.flashcards.dto.AuthResponseDto;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.LoginDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodeLoginDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodesDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodeStatusDto;
import com.flashcards.backend.flashcards.dto.TotpSetupDto;
import com.flashcards.backend.flashcards.dto.TotpVerificationDto;
import com.flashcards.backend.flashcards.dto.UserDto;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.service.AuthService;
import com.flashcards.backend.flashcards.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static com.flashcards.backend.flashcards.constants.AuthConstants.FIELD_EMAIL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.FIELD_USERNAME;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_CREDENTIALS_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENTITY_USER;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.SERVICE_DUPLICATE_EXISTS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private static final String TEST_JWT_TOKEN = "test.jwt.token";
    private static final String TEST_USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        // Manually create MockMvc without Spring context
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationArgumentResolver())
                .build();

        // Create ObjectMapper for JSON serialization
        objectMapper = new ObjectMapper();
    }

    // Custom argument resolver to handle Authentication parameters in standalone MockMvc
    private static class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return Authentication.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return webRequest.getUserPrincipal();
        }
    }

    // Helper method to create authenticated principal for tests
    private UsernamePasswordAuthenticationToken createAuthenticatedPrincipal(String userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    // Helper method to create UserDto
    private UserDto createUserDto(String id, String username, String email) {
        return UserDto.builder()
                .id(id)
                .username(username)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    // Helper method to create AuthResponseDto
    private AuthResponseDto createAuthResponse(String username, String email, boolean totpEnabled) {
        return AuthResponseDto.builder()
                .accessToken(TEST_JWT_TOKEN)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(createUserDto(TEST_USER_ID, username, email))
                .totpEnabled(totpEnabled)
                .build();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("POST /api/auth/register with valid user returns 201 Created")
        void register_ValidUser_Returns201Created() throws Exception {
            // Given
            CreateUserDto createUserDto = CreateUserDto.builder()
                    .username("newuser")
                    .email("newuser@example.com")
                    .password("SecurePass123!")
                    .firstName("New")
                    .lastName("User")
                    .build();

            AuthResponseDto authResponse = createAuthResponse("newuser", "newuser@example.com", false);
            when(authService.register(any(CreateUserDto.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value(TEST_JWT_TOKEN))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.user.id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.user.username").value("newuser"));
        }

        @Test
        @DisplayName("POST /api/auth/register with duplicate username returns 400 Bad Request")
        void register_DuplicateUsername_Returns400BadRequest() throws Exception {
            // Given
            CreateUserDto createUserDto = CreateUserDto.builder()
                    .username("existinguser")
                    .email("newuser@example.com")
                    .password("SecurePass123!")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            when(authService.register(any(CreateUserDto.class)))
                    .thenThrow(new ServiceException(
                            SERVICE_DUPLICATE_EXISTS.formatted(ENTITY_USER, FIELD_USERNAME, "existinguser"),
                            ErrorCode.SERVICE_VALIDATION_ERROR
                    ));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/register with duplicate email returns 400 Bad Request")
        void register_DuplicateEmail_Returns400BadRequest() throws Exception {
            // Given
            CreateUserDto createUserDto = CreateUserDto.builder()
                    .username("newuser")
                    .email("existing@example.com")
                    .password("SecurePass123!")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            when(authService.register(any(CreateUserDto.class)))
                    .thenThrow(new ServiceException(
                            SERVICE_DUPLICATE_EXISTS.formatted(ENTITY_USER, FIELD_EMAIL, "existing@example.com"),
                            ErrorCode.SERVICE_VALIDATION_ERROR
                    ));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/register with invalid data returns 400 Bad Request")
        void register_InvalidData_Returns400BadRequest() throws Exception {
            // Given - empty username and invalid email
            CreateUserDto createUserDto = CreateUserDto.builder()
                    .username("")
                    .email("invalid-email")
                    .password("short")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("POST /api/auth/login with valid credentials returns 200 OK")
        void login_ValidCredentials_Returns200OK() throws Exception {
            // Given
            LoginDto loginDto = LoginDto.builder()
                    .usernameOrEmail("testuser")
                    .password("SecurePass123!")
                    .build();

            AuthResponseDto authResponse = createAuthResponse("testuser", "testuser@example.com", false);
            when(authService.login(any(LoginDto.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(TEST_JWT_TOKEN))
                    .andExpect(jsonPath("$.user.username").value("testuser"));
        }

        @Test
        @DisplayName("POST /api/auth/login with invalid credentials returns 400 Bad Request")
        void login_InvalidCredentials_Returns400BadRequest() throws Exception {
            // Given
            LoginDto loginDto = LoginDto.builder()
                    .usernameOrEmail("testuser")
                    .password("WrongPassword")
                    .build();

            when(authService.login(any(LoginDto.class)))
                    .thenThrow(new ServiceException(AUTH_CREDENTIALS_INVALID, ErrorCode.AUTH_INVALID_CREDENTIALS));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/auth/login with TOTP enabled returns 200 with totpEnabled flag")
        void login_WithTotpEnabled_ReturnsTotpEnabledFlag() throws Exception {
            // Given
            LoginDto loginDto = LoginDto.builder()
                    .usernameOrEmail("totpuser")
                    .password("SecurePass123!")
                    .build();

            AuthResponseDto authResponse = AuthResponseDto.builder()
                    .user(createUserDto(TEST_USER_ID, "totpuser", "totpuser@example.com"))
                    .totpEnabled(true)
                    .build();

            when(authService.login(any(LoginDto.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totpEnabled").value(true));
        }
    }

    @Nested
    @DisplayName("TOTP Tests")
    class TotpTests {

        @Test
        @DisplayName("POST /api/auth/totp/setup returns QR code and recovery codes")
        void setupTotp_ReturnsQrCodeAndRecoveryCodes() throws Exception {
            // Given
            List<String> recoveryCodes = List.of("ABCD-1234", "EFGH-5678", "IJKL-9012");
            TotpSetupDto totpSetupDto = TotpSetupDto.builder()
                    .qrCodeDataUri("data:image/png;base64,...")
                    .secret("JBSWY3DPEHPK3PXP")
                    .recoveryCodes(recoveryCodes)
                    .build();

            when(authService.setupTotp(TEST_USER_ID)).thenReturn(totpSetupDto);

            // When & Then
            mockMvc.perform(post("/api/auth/totp/setup")
                            .principal(createAuthenticatedPrincipal(TEST_USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.qrCodeDataUri").value("data:image/png;base64,..."))
                    .andExpect(jsonPath("$.secret").value("JBSWY3DPEHPK3PXP"))
                    .andExpect(jsonPath("$.recoveryCodes", hasSize(3)));
        }

        @Test
        @DisplayName("POST /api/auth/totp/enable with valid code returns JWT token")
        void enableTotp_ValidCode_ReturnsJwtToken() throws Exception {
            // Given
            TotpVerificationDto totpVerificationDto = TotpVerificationDto.builder()
                    .totpCode("123456")
                    .build();

            AuthResponseDto authResponse = createAuthResponse("testuser", "testuser@example.com", true);
            when(authService.enableTotp(eq(TEST_USER_ID), eq("123456"))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/totp/enable")
                            .principal(createAuthenticatedPrincipal(TEST_USER_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(totpVerificationDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(TEST_JWT_TOKEN))
                    .andExpect(jsonPath("$.totpEnabled").value(true));
        }

        @Test
        @DisplayName("POST /api/auth/totp/disable returns success response")
        void disableTotp_ReturnsSuccessResponse() throws Exception {
            // Given
            AuthResponseDto authResponse = createAuthResponse("testuser", "testuser@example.com", false);
            when(authService.disableTotp(TEST_USER_ID)).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/totp/disable")
                            .principal(createAuthenticatedPrincipal(TEST_USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(TEST_JWT_TOKEN))
                    .andExpect(jsonPath("$.totpEnabled").value(false));
        }
    }

    @Nested
    @DisplayName("Recovery Code Tests")
    class RecoveryCodeTests {

        @Test
        @DisplayName("POST /api/auth/login/recovery with valid code returns JWT token")
        void loginWithRecoveryCode_ValidCode_ReturnsJwtToken() throws Exception {
            // Given
            RecoveryCodeLoginDto recoveryCodeLoginDto = RecoveryCodeLoginDto.builder()
                    .usernameOrEmail("testuser")
                    .password("SecurePass123!")
                    .recoveryCode("ABCD-1234")
                    .build();

            AuthResponseDto authResponse = createAuthResponse("testuser", "testuser@example.com", true);
            when(authService.loginWithRecoveryCode(any(RecoveryCodeLoginDto.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login/recovery")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(recoveryCodeLoginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(TEST_JWT_TOKEN));
        }

        @Test
        @DisplayName("POST /api/auth/recovery-codes/regenerate returns new recovery codes")
        void regenerateRecoveryCodes_ReturnsNewCodes() throws Exception {
            // Given
            List<String> newRecoveryCodes = List.of(
                    "NEW1-1111", "NEW2-2222", "NEW3-3333", "NEW4-4444", "NEW5-5555",
                    "NEW6-6666", "NEW7-7777", "NEW8-8888", "NEW9-9999", "NEW0-0000"
            );
            RecoveryCodesDto recoveryCodesDto = RecoveryCodesDto.builder()
                    .codes(newRecoveryCodes)
                    .remainingCodes(10)
                    .build();

            when(authService.regenerateRecoveryCodes(TEST_USER_ID)).thenReturn(recoveryCodesDto);

            // When & Then
            mockMvc.perform(post("/api/auth/recovery-codes/regenerate")
                            .principal(createAuthenticatedPrincipal(TEST_USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codes", hasSize(10)))
                    .andExpect(jsonPath("$.remainingCodes").value(10));
        }

        @Test
        @DisplayName("GET /api/auth/recovery-codes/status returns status with warning")
        void getRecoveryCodeStatus_ReturnsStatusWithWarning() throws Exception {
            // Given
            RecoveryCodeStatusDto statusDto = RecoveryCodeStatusDto.builder()
                    .remainingCodes(2)
                    .usedCodes(8)
                    .warning("You have only 2 recovery codes remaining. Consider regenerating them.")
                    .build();

            when(authService.getRecoveryCodeStatus(TEST_USER_ID)).thenReturn(statusDto);

            // When & Then
            mockMvc.perform(get("/api/auth/recovery-codes/status")
                            .principal(createAuthenticatedPrincipal(TEST_USER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.remainingCodes").value(2))
                    .andExpect(jsonPath("$.usedCodes").value(8))
                    .andExpect(jsonPath("$.warning").exists());
        }
    }

    @Nested
    @DisplayName("OAuth2 Tests")
    class OAuth2Tests {

        @Test
        @DisplayName("GET /api/auth/oauth2/providers returns available providers")
        void getOAuth2Providers_ReturnsAvailableProviders() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/auth/oauth2/providers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.providers", hasSize(2)))
                    .andExpect(jsonPath("$.providers", containsInAnyOrder("google", "github")))
                    .andExpect(jsonPath("$.authorizationBaseUrl").exists());
        }
    }
}
