package com.flashcards.backend.flashcards.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.AUTH_CREDENTIALS_INVALID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ERROR_FIELD_ERROR;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ERROR_FIELD_MESSAGE;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ERROR_FIELD_PATH;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ERROR_FIELD_STATUS;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ERROR_FIELD_TIMESTAMP;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.ERROR_TYPE_UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        log.warn("Unauthorized access attempt to: {} - {}", request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_FIELD_TIMESTAMP, LocalDateTime.now().toString());
        errorResponse.put(ERROR_FIELD_STATUS, HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put(ERROR_FIELD_ERROR, ERROR_TYPE_UNAUTHORIZED);
        errorResponse.put(ERROR_FIELD_MESSAGE, AUTH_CREDENTIALS_INVALID);
        errorResponse.put(ERROR_FIELD_PATH, request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}