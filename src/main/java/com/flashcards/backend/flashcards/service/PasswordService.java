package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.flashcards.backend.flashcards.constants.AuthConstants.BCRYPT_PREFIX_2A;
import static com.flashcards.backend.flashcards.constants.AuthConstants.BCRYPT_PREFIX_2B;
import static com.flashcards.backend.flashcards.constants.AuthConstants.BCRYPT_PREFIX_2Y;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_PASSWORD_NULL_EMPTY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_PASSWORD_VERIFICATION_FAILED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public String encryptPassword(String rawPassword) {
        if (isNotBlank(rawPassword)) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            log.debug("Password encrypted successfully");
            return encodedPassword;
        }
        throw new ServiceException(AUTH_PASSWORD_NULL_EMPTY, ErrorCode.AUTH_PASSWORD_INVALID);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (isNotBlank(rawPassword) && isNotBlank(encodedPassword)) {
            boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
            log.debug("Password verification result: {}", matches);
            return matches;
        }
        log.warn(AUTH_PASSWORD_VERIFICATION_FAILED);
        return false;
    }

    public boolean isPasswordEncoded(String password) {
        if (isNotBlank(password)) {
            return password.startsWith(BCRYPT_PREFIX_2A) ||
                   password.startsWith(BCRYPT_PREFIX_2B) ||
                   password.startsWith(BCRYPT_PREFIX_2Y);
        }
        return false;
    }
}