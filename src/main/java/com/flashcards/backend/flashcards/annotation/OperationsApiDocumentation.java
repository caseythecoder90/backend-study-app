package com.flashcards.backend.flashcards.annotation;

import com.flashcards.backend.flashcards.dto.DecryptionRequestDto;
import com.flashcards.backend.flashcards.dto.DecryptionResponseDto;
import com.flashcards.backend.flashcards.dto.EncryptionRequestDto;
import com.flashcards.backend.flashcards.dto.EncryptionResponseDto;
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

public class OperationsApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "Encrypt plain text value",
            description = "Encrypts a plain text value using Jasypt encryption. Returns the encrypted text formatted for use in configuration files."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Text encrypted successfully",
                    content = @Content(schema = @Schema(implementation = EncryptionResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - plain text is null, empty, or exceeds maximum length"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - encryption service unavailable or encryption failed"
            )
    })
    public @interface Encrypt {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "Decrypt encrypted text value",
            description = "Decrypts an encrypted text value that was previously encrypted using Jasypt. Accepts values with or without the ENC() wrapper."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Text decrypted successfully",
                    content = @Content(schema = @Schema(implementation = DecryptionResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - encrypted text is null, empty, or invalid format"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - decryption service unavailable or decryption failed"
            )
    })
    public @interface Decrypt {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(
            description = "Plain text value to be encrypted",
            required = true,
            schema = @Schema(implementation = EncryptionRequestDto.class)
    )
    @RequestBody(
            description = "Request containing the plain text value to encrypt",
            required = true,
            content = @Content(schema = @Schema(implementation = EncryptionRequestDto.class))
    )
    public @interface EncryptBody {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(
            description = "Encrypted text value to be decrypted",
            required = true,
            schema = @Schema(implementation = DecryptionRequestDto.class)
    )
    @RequestBody(
            description = "Request containing the encrypted text value to decrypt",
            required = true,
            content = @Content(schema = @Schema(implementation = DecryptionRequestDto.class))
    )
    public @interface DecryptBody {
    }
}