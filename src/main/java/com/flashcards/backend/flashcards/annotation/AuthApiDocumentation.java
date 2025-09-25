package com.flashcards.backend.flashcards.annotation;

import com.flashcards.backend.flashcards.dto.AuthResponseDto;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.LoginDto;
import com.flashcards.backend.flashcards.dto.OAuth2ProviderDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodeLoginDto;
import com.flashcards.backend.flashcards.dto.RecoveryCodesDto;
import com.flashcards.backend.flashcards.dto.TotpSetupDto;
import com.flashcards.backend.flashcards.dto.TotpVerificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AuthApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Register new user", description = "Create a new user account with email and password. Returns JWT token upon successful registration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid registration data - validation failed"),
            @ApiResponse(responseCode = "409", description = "User already exists with provided username or email"),
            @ApiResponse(responseCode = "500", description = "Internal server error during registration")
    })
    public @interface Register {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "User login", description = "Authenticate user with username/email and password. Supports optional TOTP for 2FA enabled accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials - username/email or password incorrect"),
            @ApiResponse(responseCode = "423", description = "Account locked, disabled, or requires TOTP verification"),
            @ApiResponse(responseCode = "400", description = "Invalid request format or missing required fields"),
            @ApiResponse(responseCode = "500", description = "Internal server error during authentication")
    })
    public @interface Login {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "User registration details including username, email, password, and personal information",
               required = true, schema = @Schema(implementation = CreateUserDto.class))
    @RequestBody(description = "Complete user registration information",
                 content = @Content(schema = @Schema(implementation = CreateUserDto.class)))
    public @interface RegisterBody {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "User login credentials with optional TOTP code for 2FA",
               required = true, schema = @Schema(implementation = LoginDto.class))
    @RequestBody(description = "User authentication credentials",
                 content = @Content(schema = @Schema(implementation = LoginDto.class)))
    public @interface LoginBody {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Setup TOTP 2FA", description = "Generate TOTP secret and QR code for setting up two-factor authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TOTP setup information generated successfully",
                    content = @Content(schema = @Schema(implementation = TotpSetupDto.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "409", description = "TOTP already enabled for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error during TOTP setup")
    })
    public @interface SetupTotp {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Enable TOTP 2FA", description = "Verify TOTP code and enable two-factor authentication for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TOTP enabled successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid TOTP code or request format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error during TOTP verification")
    })
    public @interface EnableTotp {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Disable TOTP 2FA", description = "Disable two-factor authentication for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TOTP disabled successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "TOTP not enabled for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error during TOTP disable")
    })
    public @interface DisableTotp {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "TOTP verification code from authenticator app",
               required = true, schema = @Schema(implementation = TotpVerificationDto.class))
    @RequestBody(description = "TOTP code for verification",
                 content = @Content(schema = @Schema(implementation = TotpVerificationDto.class)))
    public @interface TotpVerificationBody {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Login with recovery code", description = "Authenticate using a recovery code when TOTP device is unavailable. Each code can only be used once.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful with recovery code",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request format or recovery code format"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or recovery code"),
            @ApiResponse(responseCode = "403", description = "Recovery codes not enabled or all codes exhausted"),
            @ApiResponse(responseCode = "500", description = "Internal server error during recovery login")
    })
    public @interface LoginWithRecoveryCode {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(description = "Recovery code login credentials including username, password, and recovery code",
               required = true, schema = @Schema(implementation = RecoveryCodeLoginDto.class))
    @RequestBody(description = "Recovery code authentication credentials",
                 content = @Content(schema = @Schema(implementation = RecoveryCodeLoginDto.class)))
    public @interface RecoveryCodeLoginBody {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Regenerate recovery codes", description = "Generate new recovery codes, invalidating all previous codes. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recovery codes regenerated successfully",
                    content = @Content(schema = @Schema(implementation = RecoveryCodesDto.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "TOTP not enabled for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error during code generation")
    })
    public @interface RegenerateRecoveryCodes {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get recovery code status", description = "Check remaining recovery codes count and usage statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recovery code status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RecoveryCodesDto.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "TOTP not enabled for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error during status check")
    })
    public @interface GetRecoveryCodeStatus {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Get OAuth2 providers", description = "Retrieve list of available OAuth2 authentication providers and authorization URLs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 providers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OAuth2ProviderDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public @interface GetOAuth2Providers {
    }
}