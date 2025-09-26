package com.flashcards.backend.flashcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.flashcards.backend.flashcards.constants.AuthConstants.RECOVERY_CODE_CHARSET;
import static com.flashcards.backend.flashcards.constants.AuthConstants.RECOVERY_CODE_COUNT;
import static com.flashcards.backend.flashcards.constants.AuthConstants.RECOVERY_CODE_DELIMITER;
import static com.flashcards.backend.flashcards.constants.AuthConstants.RECOVERY_CODE_LENGTH;
import static com.flashcards.backend.flashcards.constants.AuthConstants.RECOVERY_CODE_SEGMENT_LENGTH;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryCodeService {
    private final PasswordService passwordService;
    private final SecureRandom secureRandom = new SecureRandom();

    public List<String> generateRecoveryCodes() {
        log.debug("Generating {} recovery codes", RECOVERY_CODE_COUNT);

        List<String> codes = IntStream.range(0, RECOVERY_CODE_COUNT)
                .mapToObj(i -> generateSingleCode())
                .collect(Collectors.toList());

        log.debug("Generated {} recovery codes successfully", codes.size());
        return codes;
    }

    public List<String> hashRecoveryCodes(List<String> codes) {
        if (isEmpty(codes)) {
            log.warn("Attempted to hash empty or null recovery codes list");
            return new ArrayList<>();
        }

        return codes.stream()
                .filter(code -> isNotBlank(code))
                .map(passwordService::encryptPassword)
                .collect(Collectors.toList());
    }

    public boolean validateRecoveryCode(String code, Set<String> hashedCodes) {
        if (isBlank(code) || isEmpty(hashedCodes)) {
            log.warn("Invalid recovery code validation attempt - code or hashes are empty");
            return false;
        }

        String formattedCode = formatCodeForValidation(code);

        return hashedCodes.stream()
                .anyMatch(hashedCode -> passwordService.verifyPassword(formattedCode, hashedCode));
    }

    public Set<String> removeUsedCode(String code, Set<String> hashedCodes) {
        if (isEmpty(hashedCodes)) {
            return Set.of();
        }

        if (isBlank(code)) {
            return hashedCodes;
        }

        String formattedCode = formatCodeForValidation(code);

        return hashedCodes.stream()
                .filter(hashedCode -> isFalse(passwordService.verifyPassword(formattedCode, hashedCode)))
                .collect(Collectors.toSet());
    }

    public int getRemainingCodesCount(Set<String> hashedCodes) {
        return isEmpty(hashedCodes) ? 0 : hashedCodes.size();
    }

    public List<String> formatCodesForDisplay(List<String> codes) {
        if (isEmpty(codes)) {
            return new ArrayList<>();
        }

        return codes.stream()
                .filter(code -> isNotBlank(code))
                .map(this::formatCodeWithDelimiter)
                .collect(Collectors.toList());
    }

    private String generateSingleCode() {
        StringBuilder code = new StringBuilder(RECOVERY_CODE_LENGTH);

        IntStream.range(0, RECOVERY_CODE_LENGTH)
                .forEach(i -> {
                    int randomIndex = secureRandom.nextInt(RECOVERY_CODE_CHARSET.length());
                    code.append(RECOVERY_CODE_CHARSET.charAt(randomIndex));
                });

        return code.toString();
    }

    private String formatCodeWithDelimiter(String code) {
        if (isBlank(code) || code.length() != RECOVERY_CODE_LENGTH) {
            return code;
        }

        return new StringBuilder(code)
                .insert(RECOVERY_CODE_SEGMENT_LENGTH, RECOVERY_CODE_DELIMITER)
                .toString();
    }

    private String formatCodeForValidation(String code) {
        return remove(upperCase(trimToEmpty(code)), RECOVERY_CODE_DELIMITER);
    }
}