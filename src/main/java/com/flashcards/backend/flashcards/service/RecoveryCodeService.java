package com.flashcards.backend.flashcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
        if (CollectionUtils.isEmpty(codes)) {
            log.warn("Attempted to hash empty or null recovery codes list");
            return new ArrayList<>();
        }

        return codes.stream()
                .filter(StringUtils::isNotBlank)
                .map(passwordService::encryptPassword)
                .collect(Collectors.toList());
    }

    public boolean validateRecoveryCode(String code, Set<String> hashedCodes) {
        if (StringUtils.isBlank(code) || CollectionUtils.isEmpty(hashedCodes)) {
            log.warn("Invalid recovery code validation attempt - code or hashes are empty");
            return false;
        }

        String formattedCode = formatCodeForValidation(code);

        return hashedCodes.stream()
                .anyMatch(hashedCode -> passwordService.verifyPassword(formattedCode, hashedCode));
    }

    public Set<String> removeUsedCode(String code, Set<String> hashedCodes) {
        if (CollectionUtils.isEmpty(hashedCodes)) {
            return Set.of();
        }

        if (StringUtils.isBlank(code)) {
            return hashedCodes;
        }

        String formattedCode = formatCodeForValidation(code);

        return hashedCodes.stream()
                .filter(hashedCode -> BooleanUtils.isFalse(passwordService.verifyPassword(formattedCode, hashedCode)))
                .collect(Collectors.toSet());
    }

    public int getRemainingCodesCount(Set<String> hashedCodes) {
        return CollectionUtils.isEmpty(hashedCodes) ? 0 : hashedCodes.size();
    }

    public List<String> formatCodesForDisplay(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }

        return codes.stream()
                .filter(StringUtils::isNotBlank)
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
        if (StringUtils.isBlank(code) || code.length() != RECOVERY_CODE_LENGTH) {
            return code;
        }

        return new StringBuilder(code)
                .insert(RECOVERY_CODE_SEGMENT_LENGTH, RECOVERY_CODE_DELIMITER)
                .toString();
    }

    private String formatCodeForValidation(String code) {
        return StringUtils.remove(StringUtils.upperCase(StringUtils.trimToEmpty(code)), RECOVERY_CODE_DELIMITER);
    }
}