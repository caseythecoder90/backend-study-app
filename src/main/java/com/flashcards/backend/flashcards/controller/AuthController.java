package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.AuthApiDocumentation;
import com.flashcards.backend.flashcards.dto.AuthResponseDto;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.LoginDto;
import com.flashcards.backend.flashcards.dto.OAuth2ProviderDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodeLoginDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodesDto;
import com.flashcards.backend.flashcards.dto.TotpSetupDto;
import com.flashcards.backend.flashcards.dto.TotpVerificationDto;
import com.flashcards.backend.flashcards.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_AUTHORIZATION_BASE_PATH;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PROVIDER_GITHUB;
import static com.flashcards.backend.flashcards.constants.AuthConstants.OAUTH_PROVIDER_GOOGLE;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints with JWT token support")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @AuthApiDocumentation.Register
    public ResponseEntity<AuthResponseDto> register(
            @AuthApiDocumentation.RegisterBody @Valid @RequestBody CreateUserDto createUserDto) {
        log.info("POST /api/auth/register - Registering new user: {}", createUserDto.getUsername());

        AuthResponseDto response = authService.register(createUserDto);

        log.info("POST /api/auth/register - User registered successfully: {}", createUserDto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @AuthApiDocumentation.Login
    public ResponseEntity<AuthResponseDto> login(
            @AuthApiDocumentation.LoginBody @Valid @RequestBody LoginDto loginDto) {
        log.info("POST /api/auth/login - Login attempt for: {}", loginDto.getUsernameOrEmail());

        AuthResponseDto response = authService.login(loginDto);

        log.info("POST /api/auth/login - Login successful for: {}", loginDto.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/totp/setup")
    @AuthApiDocumentation.SetupTotp
    public ResponseEntity<TotpSetupDto> setupTotp(Authentication authentication) {
        String userId = authentication.getName();
        log.info("POST /api/auth/totp/setup - Setting up TOTP for user: {}", userId);

        TotpSetupDto response = authService.setupTotp(userId);

        log.info("POST /api/auth/totp/setup - TOTP setup completed for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/totp/enable")
    @AuthApiDocumentation.EnableTotp
    public ResponseEntity<AuthResponseDto> enableTotp(
            @AuthApiDocumentation.TotpVerificationBody @Valid @RequestBody TotpVerificationDto totpVerificationDto,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("POST /api/auth/totp/enable - Enabling TOTP for user: {}", userId);

        AuthResponseDto response = authService.enableTotp(userId, totpVerificationDto.getTotpCode());

        log.info("POST /api/auth/totp/enable - TOTP enabled successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/totp/disable")
    @AuthApiDocumentation.DisableTotp
    public ResponseEntity<AuthResponseDto> disableTotp(Authentication authentication) {
        String userId = authentication.getName();
        log.info("POST /api/auth/totp/disable - Disabling TOTP for user: {}", userId);

        AuthResponseDto response = authService.disableTotp(userId);

        log.info("POST /api/auth/totp/disable - TOTP disabled successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/recovery")
    @AuthApiDocumentation.LoginWithRecoveryCode
    public ResponseEntity<AuthResponseDto> loginWithRecoveryCode(
            @AuthApiDocumentation.RecoveryCodeLoginBody @Valid @RequestBody RecoveryCodeLoginDto recoveryCodeLoginDto) {
        log.info("POST /api/auth/login/recovery - Recovery code login attempt for: {}", recoveryCodeLoginDto.getUsernameOrEmail());

        AuthResponseDto response = authService.loginWithRecoveryCode(recoveryCodeLoginDto);

        log.info("POST /api/auth/login/recovery - Recovery code login successful for: {}", recoveryCodeLoginDto.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recovery-codes/regenerate")
    @AuthApiDocumentation.RegenerateRecoveryCodes
    public ResponseEntity<RecoveryCodesDto> regenerateRecoveryCodes(Authentication authentication) {
        String userId = authentication.getName();
        log.info("POST /api/auth/recovery-codes/regenerate - Regenerating recovery codes for user: {}", userId);

        RecoveryCodesDto response = authService.regenerateRecoveryCodes(userId);

        log.info("POST /api/auth/recovery-codes/regenerate - Recovery codes regenerated for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recovery-codes/status")
    @AuthApiDocumentation.GetRecoveryCodeStatus
    public ResponseEntity<RecoveryCodesDto> getRecoveryCodeStatus(Authentication authentication) {
        String userId = authentication.getName();
        log.info("GET /api/auth/recovery-codes/status - Getting recovery code status for user: {}", userId);

        RecoveryCodesDto response = authService.getRecoveryCodeStatus(userId);

        log.info("GET /api/auth/recovery-codes/status - Retrieved recovery code status for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/providers")
    @AuthApiDocumentation.GetOAuth2Providers
    public ResponseEntity<OAuth2ProviderDto> getOAuth2Providers(HttpServletRequest request) {
        log.info("GET /api/auth/oauth2/providers - Getting available OAuth2 providers");

        List<String> providers = List.of(OAUTH_PROVIDER_GOOGLE, OAUTH_PROVIDER_GITHUB);
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                        (request.getServerPort() != 80 && request.getServerPort() != 443 ?
                         ":" + request.getServerPort() : "") +
                        request.getContextPath() + OAUTH_AUTHORIZATION_BASE_PATH;

        OAuth2ProviderDto response = new OAuth2ProviderDto(providers, baseUrl);

        log.info("GET /api/auth/oauth2/providers - Retrieved {} OAuth2 providers", providers.size());
        return ResponseEntity.ok(response);
    }
}