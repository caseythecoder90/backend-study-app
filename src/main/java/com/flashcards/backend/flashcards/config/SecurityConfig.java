package com.flashcards.backend.flashcards.config;

import com.flashcards.backend.flashcards.oauth.OAuth2AuthenticationSuccessHandler;
import com.flashcards.backend.flashcards.security.JwtAuthenticationEntryPoint;
import com.flashcards.backend.flashcards.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.flashcards.backend.flashcards.constants.SecurityConstants.ADMIN_ONLY_ENDPOINTS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_HEADER_ACCEPT;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_HEADER_AUTHORIZATION;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_HEADER_CONTENT_TYPE;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_HEADER_X_REQUESTED_WITH;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_MAX_AGE_SECONDS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_ORIGIN_LOCALHOST;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_ORIGIN_LOCALHOST_IP;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_ORIGIN_PRODUCTION;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_DELETE;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_GET;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_OPTIONS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_POST;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_PUT;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.PROTECTED_AUTH_ENDPOINTS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.PUBLIC_AUTH_ENDPOINTS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.PUBLIC_ENDPOINTS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.SWAGGER_ENDPOINTS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none'"))
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> contentType.disable())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                                .preload(true))
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions
                                .policy("camera=(), microphone=(), geolocation=(), payment=()"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/search").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/oauth-test.html", "/auth-success.html", "/auth-error.html").permitAll()
                        .requestMatchers(ADMIN_ONLY_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(PROTECTED_AUTH_ENDPOINTS).authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Specific allowed origins for development and production
        configuration.setAllowedOrigins(List.of(
                CORS_ORIGIN_LOCALHOST,
                CORS_ORIGIN_LOCALHOST_IP,
                CORS_ORIGIN_PRODUCTION
        ));

        // Standard HTTP methods
        configuration.setAllowedMethods(List.of(
                HTTP_METHOD_GET,
                HTTP_METHOD_POST,
                HTTP_METHOD_PUT,
                HTTP_METHOD_DELETE,
                HTTP_METHOD_OPTIONS
        ));

        // Specific headers instead of wildcard
        configuration.setAllowedHeaders(List.of(
                CORS_HEADER_AUTHORIZATION,
                CORS_HEADER_CONTENT_TYPE,
                CORS_HEADER_ACCEPT,
                CORS_HEADER_X_REQUESTED_WITH
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}