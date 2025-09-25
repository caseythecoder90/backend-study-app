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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.flashcards.backend.flashcards.constants.SecurityConstants.PROTECTED_AUTH_ENDPOINTS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.PUBLIC_AUTH_ENDPOINTS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.CORS_MAX_AGE_SECONDS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_DELETE;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_GET;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_OPTIONS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_POST;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.HTTP_METHOD_PUT;
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/search").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
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
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of(
                HTTP_METHOD_GET,
                HTTP_METHOD_POST,
                HTTP_METHOD_PUT,
                HTTP_METHOD_DELETE,
                HTTP_METHOD_OPTIONS
        ));
        configuration.setAllowedHeaders(List.of("*"));
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