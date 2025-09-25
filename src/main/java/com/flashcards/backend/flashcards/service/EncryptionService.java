package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dto.DecryptionRequestDto;
import com.flashcards.backend.flashcards.dto.DecryptionResponseDto;
import com.flashcards.backend.flashcards.dto.EncryptionRequestDto;
import com.flashcards.backend.flashcards.dto.EncryptionResponseDto;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.DECRYPTION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.DECRYPTION_TEXT_NULL_EMPTY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENCRYPTION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ENCRYPTION_TEXT_NULL_EMPTY;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.ENC_PREFIX;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.ENC_SUFFIX;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
@Service
public class EncryptionService {

    private final StringEncryptor stringEncryptor;

    public EncryptionService(@Qualifier("jasyptStringEncryptor") StringEncryptor stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }

    public EncryptionResponseDto encryptText(EncryptionRequestDto request) {
        log.debug("Encrypting plain text value");

        try {
            String encryptedText = stringEncryptor.encrypt(request.getPlainText());

            return EncryptionResponseDto.builder()
                    .encryptedText(encryptedText)
                    .formattedValue(formatEncryptedValue(encryptedText))
                    .build();
        } catch (Exception e) {
            log.error("Failed to encrypt text", e);
            throw new ServiceException(
                    ENCRYPTION_FAILED.formatted(e.getMessage()),
                    ErrorCode.ENCRYPTION_ERROR
            );
        }
    }

    public DecryptionResponseDto decryptText(DecryptionRequestDto request) {
        log.debug("Decrypting encrypted text value");

        try {
            String textToDecrypt = extractEncryptedText(request.getEncryptedText());
            String plainText = stringEncryptor.decrypt(textToDecrypt);

            return DecryptionResponseDto.builder()
                    .plainText(plainText)
                    .build();
        } catch (Exception e) {
            log.error("Failed to decrypt text", e);
            throw new ServiceException(
                    DECRYPTION_FAILED.formatted(e.getMessage()),
                    ErrorCode.DECRYPTION_ERROR
            );
        }
    }


    private String formatEncryptedValue(String encryptedText) {
        if (isBlank(encryptedText)) {
            return encryptedText;
        }
        return ENC_PREFIX + encryptedText + ENC_SUFFIX;
    }

    private String extractEncryptedText(String text) {
        if (isBlank(text)) {
            return text;
        }

        // Remove ENC() wrapper if present
        if (text.startsWith(ENC_PREFIX) && text.endsWith(ENC_SUFFIX)) {
            return text.substring(ENC_PREFIX.length(), text.length() - ENC_SUFFIX.length());
        }

        return text;
    }

    public boolean isEncryptionAvailable() {
        try {
            // Test encryption by encrypting and decrypting a test string
            String testString = "test";
            String encrypted = stringEncryptor.encrypt(testString);
            String decrypted = stringEncryptor.decrypt(encrypted);
            return testString.equals(decrypted);
        } catch (Exception e) {
            log.error("Failed to check encryption availability", e);
            return false;
        }
    }
}