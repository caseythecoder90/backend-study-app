package com.flashcards.backend.flashcards.security;

import com.flashcards.backend.flashcards.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

import static com.flashcards.backend.flashcards.constants.SecurityConstants.SECURITY_HEADER_AUTHORIZATION;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.SECURITY_HEADER_BEARER_PREFIX;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.SECURITY_HEADER_BEARER_PREFIX_LENGTH;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(SECURITY_HEADER_AUTHORIZATION);

        if (isNotBlank(authHeader) && authHeader.startsWith(SECURITY_HEADER_BEARER_PREFIX)) {
            String token = authHeader.substring(SECURITY_HEADER_BEARER_PREFIX_LENGTH);

            try {
                if (BooleanUtils.isTrue(jwtService.isTokenValid(token)) && isNull(SecurityContextHolder.getContext().getAuthentication())) {
                    String userId = jwtService.extractUserId(token);
                    String username = jwtService.extractUsername(token);

                    if (isNotBlank(userId) && isNotBlank(username)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userId, null, new ArrayList<>()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("User authenticated: {} (ID: {})", username, userId);
                    }
                }
            } catch (Exception e) {
                log.warn("JWT authentication failed for token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}