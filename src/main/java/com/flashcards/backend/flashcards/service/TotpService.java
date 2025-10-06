package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

import static com.flashcards.backend.flashcards.constants.AuthConstants.QR_CODE_DATA_URI_PREFIX;
import static com.flashcards.backend.flashcards.constants.AuthConstants.TOTP_CODE_LENGTH;
import static com.flashcards.backend.flashcards.constants.AuthConstants.TOTP_TIME_STEP_SECONDS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOTP_CODE_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOTP_QR_GENERATION_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOTP_SECRET_USERNAME_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_TOTP_VERIFICATION_FAILED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
public class TotpService {
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;
    private final QrDataFactory qrDataFactory;
    private final String appName;

    public TotpService(@Value("${app.name:Flashcards}") String appName) {
        this.appName = appName;
        this.secretGenerator = new DefaultSecretGenerator();

        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        TimeProvider timeProvider = new SystemTimeProvider();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        this.qrGenerator = new ZxingPngQrGenerator();
        this.qrDataFactory = new QrDataFactory(HashingAlgorithm.SHA1, TOTP_CODE_LENGTH, TOTP_TIME_STEP_SECONDS);
    }

    public String generateSecret() {
        String secret = secretGenerator.generate();
        log.debug("Generated new TOTP secret");
        return secret;
    }

    public void doSomething() {
        int num = new Random().nextInt(0, 5000);
        switch(num) {
            case 1 -> System.out.println();                    // Returns String (ignored)
            case 2 -> System.out.println();                     // Returns int (ignored);
            case 3 -> System.out.println("3");  // Returns void
            default -> doSomething();           // Any return type
        };
    }

    public String generateQrCodeImageUri(String secret, String username) {
        if (isNotBlank(secret) && isNotBlank(username)) {
            QrData data = qrDataFactory.newBuilder()
                    .label(username)
                    .secret(secret)
                    .issuer(appName)
                    .build();

            try {
                byte[] qrCodeImage = qrGenerator.generate(data);
                String base64Image = java.util.Base64.getEncoder().encodeToString(qrCodeImage);
                String dataUri = QR_CODE_DATA_URI_PREFIX + base64Image;

                log.debug("Generated QR code for user: {}", username);
                return dataUri;
            } catch (Exception e) {
                log.error("Failed to generate QR code for user: {} - {}", username, e.getMessage());
                throw new ServiceException(
                        AUTH_TOTP_QR_GENERATION_FAILED.formatted(e.getMessage()),
                        ErrorCode.SERVICE_BUSINESS_LOGIC_ERROR,
                        e
                );
            }
        }
        throw new ServiceException(AUTH_TOTP_SECRET_USERNAME_NULL, ErrorCode.SERVICE_VALIDATION_ERROR);
    }

    public boolean verifyCode(String secret, String code) {
        if (isNotBlank(secret) && isNotBlank(code)) {
            boolean isValid = codeVerifier.isValidCode(secret, code);
            log.debug("TOTP code verification result: {}", isValid);
            return isValid;
        }
        log.warn(AUTH_TOTP_VERIFICATION_FAILED);
        return false;
    }

    public void validateTotpCode(String secret, String code) {
        if (isFalse(verifyCode(secret, code))) {
            throw new ServiceException(AUTH_TOTP_CODE_INVALID, ErrorCode.AUTH_TOTP_INVALID);
        }
    }
}