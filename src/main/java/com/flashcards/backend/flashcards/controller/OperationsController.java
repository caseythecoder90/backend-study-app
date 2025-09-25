package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.OperationsApiDocumentation;
import com.flashcards.backend.flashcards.dto.DecryptionRequestDto;
import com.flashcards.backend.flashcards.dto.DecryptionResponseDto;
import com.flashcards.backend.flashcards.dto.EncryptionRequestDto;
import com.flashcards.backend.flashcards.dto.EncryptionResponseDto;
import com.flashcards.backend.flashcards.service.EncryptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
@Tag(name = "Operations", description = "Utility operations endpoints for administrative tasks including encryption and decryption")
public class OperationsController {

    private final EncryptionService encryptionService;

    @PostMapping("/encrypt")
    @OperationsApiDocumentation.Encrypt
    public ResponseEntity<EncryptionResponseDto> encryptText(
            @OperationsApiDocumentation.EncryptBody @Valid @RequestBody EncryptionRequestDto request) {
        log.info("POST /api/operations/encrypt - Encrypting text");

        EncryptionResponseDto response = encryptionService.encryptText(request);

        log.info("POST /api/operations/encrypt - Text encrypted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/decrypt")
    @OperationsApiDocumentation.Decrypt
    public ResponseEntity<DecryptionResponseDto> decryptText(
            @OperationsApiDocumentation.DecryptBody @Valid @RequestBody DecryptionRequestDto request) {
        log.info("POST /api/operations/decrypt - Decrypting text");

        DecryptionResponseDto response = encryptionService.decryptText(request);

        log.info("POST /api/operations/decrypt - Text decrypted successfully");
        return ResponseEntity.ok(response);
    }
}