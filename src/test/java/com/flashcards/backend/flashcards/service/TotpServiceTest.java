package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.exception.ServiceException;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TotpService Tests")
class TotpServiceTest {

    @Mock
    private SecretGenerator secretGenerator;

    @Mock
    private CodeVerifier codeVerifier;

    @Mock
    private QrGenerator qrGenerator;

    @Mock
    private QrDataFactory qrDataFactory;

    @InjectMocks
    private TotpService totpService;

    private static final String TEST_SECRET = "JBSWY3DPEHPK3PXP";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOTP_CODE = "123456";
    private static final String TEST_APP_NAME = "Flashcards";

    @BeforeEach
    void setUp() {
        // Set appName using reflection since it's a @Value injected field
        ReflectionTestUtils.setField(totpService, "appName", TEST_APP_NAME);
    }

    @Test
    @DisplayName("generateSecret returns non-null secret")
    void generateSecret_ReturnsNonNullSecret() {
        // Given
        when(secretGenerator.generate()).thenReturn(TEST_SECRET);

        // When
        String result = totpService.generateSecret();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(TEST_SECRET);
        verify(secretGenerator).generate();
    }

    @Test
    @DisplayName("generateSecret returns non-empty secret")
    void generateSecret_ReturnsNonEmptySecret() {
        // Given
        when(secretGenerator.generate()).thenReturn(TEST_SECRET);

        // When
        String result = totpService.generateSecret();

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("generateQrCodeImageUri with valid params returns data URI")
    void generateQrCodeImageUri_ValidParams_ReturnsDataUri() throws Exception {
        // Given
        QrData.Builder mockBuilder = mock(QrData.Builder.class);
        QrData mockQrData = mock(QrData.class);
        byte[] mockQrCodeImage = new byte[]{1, 2, 3, 4, 5};

        when(qrDataFactory.newBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.label(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.secret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.issuer(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockQrData);
        when(qrGenerator.generate(mockQrData)).thenReturn(mockQrCodeImage);

        // When
        String result = totpService.generateQrCodeImageUri(TEST_SECRET, TEST_USERNAME);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).startsWith("data:image/png;base64,");
        verify(qrDataFactory).newBuilder();
        verify(mockBuilder).label(TEST_USERNAME);
        verify(mockBuilder).secret(TEST_SECRET);
        verify(mockBuilder).issuer(TEST_APP_NAME);
        verify(qrGenerator).generate(mockQrData);
    }

    @Test
    @DisplayName("generateQrCodeImageUri with null secret throws ServiceException")
    void generateQrCodeImageUri_NullSecret_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> totpService.generateQrCodeImageUri(null, TEST_USERNAME))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Secret and username cannot be null");
    }

    @Test
    @DisplayName("generateQrCodeImageUri with blank secret throws ServiceException")
    void generateQrCodeImageUri_BlankSecret_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> totpService.generateQrCodeImageUri("", TEST_USERNAME))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Secret and username cannot be null");
    }

    @Test
    @DisplayName("generateQrCodeImageUri with null username throws ServiceException")
    void generateQrCodeImageUri_NullUsername_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> totpService.generateQrCodeImageUri(TEST_SECRET, null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Secret and username cannot be null");
    }

    @Test
    @DisplayName("generateQrCodeImageUri with blank username throws ServiceException")
    void generateQrCodeImageUri_BlankUsername_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> totpService.generateQrCodeImageUri(TEST_SECRET, ""))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Secret and username cannot be null");
    }

    @Test
    @DisplayName("generateQrCodeImageUri handles QR generation failure")
    void generateQrCodeImageUri_QrGenerationFails_ThrowsServiceException() throws Exception {
        // Given
        QrData.Builder mockBuilder = mock(QrData.Builder.class);
        QrData mockQrData = mock(QrData.class);

        when(qrDataFactory.newBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.label(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.secret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.issuer(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockQrData);
        when(qrGenerator.generate(any())).thenThrow(new RuntimeException("QR generation failed"));

        // When / Then
        assertThatThrownBy(() -> totpService.generateQrCodeImageUri(TEST_SECRET, TEST_USERNAME))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to generate QR code");
    }

    @Test
    @DisplayName("verifyCode with valid code returns true")
    void verifyCode_ValidCode_ReturnsTrue() {
        // Given
        when(codeVerifier.isValidCode(TEST_SECRET, TEST_TOTP_CODE)).thenReturn(true);

        // When
        boolean result = totpService.verifyCode(TEST_SECRET, TEST_TOTP_CODE);

        // Then
        assertThat(result).isTrue();
        verify(codeVerifier).isValidCode(TEST_SECRET, TEST_TOTP_CODE);
    }

    @Test
    @DisplayName("verifyCode with invalid code returns false")
    void verifyCode_InvalidCode_ReturnsFalse() {
        // Given
        String invalidCode = "000000";
        when(codeVerifier.isValidCode(TEST_SECRET, invalidCode)).thenReturn(false);

        // When
        boolean result = totpService.verifyCode(TEST_SECRET, invalidCode);

        // Then
        assertThat(result).isFalse();
        verify(codeVerifier).isValidCode(TEST_SECRET, invalidCode);
    }

    @Test
    @DisplayName("verifyCode with null secret returns false")
    void verifyCode_NullSecret_ReturnsFalse() {
        // When
        boolean result = totpService.verifyCode(null, TEST_TOTP_CODE);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyCode with blank secret returns false")
    void verifyCode_BlankSecret_ReturnsFalse() {
        // When
        boolean result = totpService.verifyCode("", TEST_TOTP_CODE);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyCode with null code returns false")
    void verifyCode_NullCode_ReturnsFalse() {
        // When
        boolean result = totpService.verifyCode(TEST_SECRET, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyCode with blank code returns false")
    void verifyCode_BlankCode_ReturnsFalse() {
        // When
        boolean result = totpService.verifyCode(TEST_SECRET, "");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateTotpCode with valid code does not throw")
    void validateTotpCode_ValidCode_DoesNotThrow() {
        // Given
        when(codeVerifier.isValidCode(TEST_SECRET, TEST_TOTP_CODE)).thenReturn(true);

        // When / Then - should not throw
        totpService.validateTotpCode(TEST_SECRET, TEST_TOTP_CODE);
        verify(codeVerifier).isValidCode(TEST_SECRET, TEST_TOTP_CODE);
    }

    @Test
    @DisplayName("validateTotpCode with invalid code throws ServiceException")
    void validateTotpCode_InvalidCode_ThrowsServiceException() {
        // Given
        String invalidCode = "000000";
        when(codeVerifier.isValidCode(TEST_SECRET, invalidCode)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> totpService.validateTotpCode(TEST_SECRET, invalidCode))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Invalid TOTP code");
        verify(codeVerifier).isValidCode(TEST_SECRET, invalidCode);
    }

    @Test
    @DisplayName("validateTotpCode with null secret throws ServiceException")
    void validateTotpCode_NullSecret_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> totpService.validateTotpCode(null, TEST_TOTP_CODE))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Invalid TOTP code");
    }

    @Test
    @DisplayName("validateTotpCode with null code throws ServiceException")
    void validateTotpCode_NullCode_ThrowsServiceException() {
        // When / Then
        assertThatThrownBy(() -> totpService.validateTotpCode(TEST_SECRET, null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Invalid TOTP code");
    }
}
