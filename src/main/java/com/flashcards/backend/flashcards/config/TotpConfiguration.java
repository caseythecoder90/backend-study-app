package com.flashcards.backend.flashcards.config;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for TOTP (Time-based One-Time Password) components.
 * This configuration provides Spring beans for all TOTP-related dependencies,
 * enabling proper dependency injection and testability.
 */
@Configuration
public class TotpConfiguration {

    /**
     * Provides a SecretGenerator bean for generating TOTP secrets.
     *
     * @return DefaultSecretGenerator instance
     */
    @Bean
    public SecretGenerator secretGenerator() {
        return new DefaultSecretGenerator();
    }

    /**
     * Provides a CodeGenerator bean for generating TOTP codes.
     *
     * @return DefaultCodeGenerator instance
     */
    @Bean
    public CodeGenerator codeGenerator() {
        return new DefaultCodeGenerator();
    }

    /**
     * Provides a TimeProvider bean for system time access.
     *
     * @return SystemTimeProvider instance
     */
    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }

    /**
     * Provides a CodeVerifier bean for verifying TOTP codes.
     * This bean depends on CodeGenerator and TimeProvider.
     *
     * @param codeGenerator the code generator
     * @param timeProvider the time provider
     * @return DefaultCodeVerifier instance
     */
    @Bean
    public CodeVerifier codeVerifier(CodeGenerator codeGenerator, TimeProvider timeProvider) {
        return new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    /**
     * Provides a QrGenerator bean for generating QR codes.
     *
     * @return ZxingPngQrGenerator instance
     */
    @Bean
    public QrGenerator qrGenerator() {
        return new ZxingPngQrGenerator();
    }

    /**
     * Provides a QrDataFactory bean for creating QR code data.
     * Configuration values are externalized to application properties.
     *
     * @param codeLength the length of TOTP codes (default: 6)
     * @param timeStep the time step in seconds for TOTP validity (default: 30)
     * @return QrDataFactory instance configured with SHA1 algorithm
     */
    @Bean
    public QrDataFactory qrDataFactory(
            @Value("${totp.code-length:6}") int codeLength,
            @Value("${totp.time-step:30}") int timeStep) {
        return new QrDataFactory(HashingAlgorithm.SHA1, codeLength, timeStep);
    }
}
