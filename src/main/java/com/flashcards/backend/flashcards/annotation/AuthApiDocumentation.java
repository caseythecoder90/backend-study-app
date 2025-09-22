package com.flashcards.backend.flashcards.annotation;

import com.flashcards.backend.flashcards.dto.AuthResponseDto;
import com.flashcards.backend.flashcards.dto.CreateUserDto;
import com.flashcards.backend.flashcards.dto.LoginDto;
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
}