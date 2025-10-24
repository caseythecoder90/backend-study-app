package com.flashcards.backend.flashcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecoveryCodeService Tests")
class RecoveryCodeServiceTest {

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private RecoveryCodeService recoveryCodeService;

    private static final int EXPECTED_CODE_COUNT = 10;
    private static final int EXPECTED_CODE_LENGTH = 8;

    @BeforeEach
    void setUp() {
        // Mock password hashing
        when(passwordService.encryptPassword(anyString()))
                .thenAnswer(invocation -> "hashed_" + invocation.getArgument(0));

        // Mock password verification
        when(passwordService.verifyPassword(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String rawCode = invocation.getArgument(0);
                    String hashedCode = invocation.getArgument(1);
                    return hashedCode.equals("hashed_" + rawCode);
                });
    }

    @Test
    @DisplayName("generateRecoveryCodes returns correct count of codes")
    void generateRecoveryCodes_ReturnsCorrectCount() {
        // When
        List<String> codes = recoveryCodeService.generateRecoveryCodes();

        // Then
        assertThat(codes).hasSize(EXPECTED_CODE_COUNT);
    }

    @Test
    @DisplayName("generateRecoveryCodes returns codes with correct length")
    void generateRecoveryCodes_CodesHaveCorrectLength() {
        // When
        List<String> codes = recoveryCodeService.generateRecoveryCodes();

        // Then
        codes.forEach(code -> assertThat(code).hasSize(EXPECTED_CODE_LENGTH));
    }

    @Test
    @DisplayName("generateRecoveryCodes returns unique codes")
    void generateRecoveryCodes_CodesAreUnique() {
        // When
        List<String> codes = recoveryCodeService.generateRecoveryCodes();

        // Then
        Set<String> uniqueCodes = new HashSet<>(codes);
        assertThat(uniqueCodes).hasSize(codes.size());
    }

    @Test
    @DisplayName("hashRecoveryCodes with valid codes returns hashed codes")
    void hashRecoveryCodes_ValidCodes_ReturnsHashedCodes() {
        // Given
        List<String> codes = List.of("ABCD1234", "EFGH5678", "IJKL9012");

        // When
        List<String> hashedCodes = recoveryCodeService.hashRecoveryCodes(codes);

        // Then
        assertThat(hashedCodes).hasSize(3);
        assertThat(hashedCodes).containsExactly(
                "hashed_ABCD1234",
                "hashed_EFGH5678",
                "hashed_IJKL9012"
        );
    }

    @Test
    @DisplayName("hashRecoveryCodes with empty list returns empty list")
    void hashRecoveryCodes_EmptyList_ReturnsEmptyList() {
        // Given
        List<String> codes = new ArrayList<>();

        // When
        List<String> hashedCodes = recoveryCodeService.hashRecoveryCodes(codes);

        // Then
        assertThat(hashedCodes).isEmpty();
    }

    @Test
    @DisplayName("validateRecoveryCode with valid code returns true")
    void validateRecoveryCode_ValidCode_ReturnsTrue() {
        // Given
        String code = "ABCD1234";
        Set<String> hashedCodes = Set.of("hashed_ABCD1234", "hashed_EFGH5678");

        // When
        boolean isValid = recoveryCodeService.validateRecoveryCode(code, hashedCodes);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateRecoveryCode with invalid code returns false")
    void validateRecoveryCode_InvalidCode_ReturnsFalse() {
        // Given
        String code = "INVALID1";
        Set<String> hashedCodes = Set.of("hashed_ABCD1234", "hashed_EFGH5678");

        // When
        boolean isValid = recoveryCodeService.validateRecoveryCode(code, hashedCodes);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateRecoveryCode is case-insensitive and handles delimiter")
    void validateRecoveryCode_CaseInsensitiveWithDelimiter_ReturnsTrue() {
        // Given
        String code = "abcd-1234"; // lowercase with delimiter
        Set<String> hashedCodes = Set.of("hashed_ABCD1234"); // stored without delimiter

        // When
        boolean isValid = recoveryCodeService.validateRecoveryCode(code, hashedCodes);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("removeUsedCode removes the specified code")
    void removeUsedCode_ValidCode_RemovesCode() {
        // Given
        String codeToRemove = "ABCD1234";
        Set<String> hashedCodes = new HashSet<>(Set.of(
                "hashed_ABCD1234",
                "hashed_EFGH5678",
                "hashed_IJKL9012"
        ));

        // When
        Set<String> remainingCodes = recoveryCodeService.removeUsedCode(codeToRemove, hashedCodes);

        // Then
        assertThat(remainingCodes).hasSize(2);
        assertThat(remainingCodes).doesNotContain("hashed_ABCD1234");
        assertThat(remainingCodes).contains("hashed_EFGH5678", "hashed_IJKL9012");
    }

    @Test
    @DisplayName("getRemainingCodesCount returns correct count")
    void getRemainingCodesCount_ReturnsCorrectCount() {
        // Given
        Set<String> hashedCodes = Set.of("hash1", "hash2", "hash3");

        // When
        int count = recoveryCodeService.getRemainingCodesCount(hashedCodes);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("getRemainingCodesCount with empty set returns zero")
    void getRemainingCodesCount_EmptySet_ReturnsZero() {
        // Given
        Set<String> hashedCodes = new HashSet<>();

        // When
        int count = recoveryCodeService.getRemainingCodesCount(hashedCodes);

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("formatCodesForDisplay adds delimiter")
    void formatCodesForDisplay_AddsDelimiter() {
        // Given
        List<String> codes = List.of("ABCD1234", "EFGH5678");

        // When
        List<String> formattedCodes = recoveryCodeService.formatCodesForDisplay(codes);

        // Then
        assertThat(formattedCodes).containsExactly(
                "ABCD-1234",
                "EFGH-5678"
        );
    }

    @Test
    @DisplayName("formatCodesForDisplay handles empty list")
    void formatCodesForDisplay_HandlesEmptyList() {
        // Given
        List<String> codes = new ArrayList<>();

        // When
        List<String> formattedCodes = recoveryCodeService.formatCodesForDisplay(codes);

        // Then
        assertThat(formattedCodes).isEmpty();
    }
}
