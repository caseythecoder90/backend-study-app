package com.flashcards.backend.flashcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.flashcards.backend.flashcards.constants.SecurityConstants.SECURITY_HEADER_AUTHORIZATION;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.SWAGGER_SECURITY_SCHEME_BEARER_FORMAT;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.SWAGGER_SECURITY_SCHEME_DESCRIPTION;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.SWAGGER_SECURITY_SCHEME_NAME;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.info.version:1.0.0}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flashcards API")
                        .version(version)
                        .description("Backend APIs for flashcard application with AI-powered card generation")
                        .contact(new Contact()
                                .name("Casey Quinn")
                                .email("caseythecoder90@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SWAGGER_SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SWAGGER_SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_HEADER_AUTHORIZATION)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat(SWAGGER_SECURITY_SCHEME_BEARER_FORMAT)
                                .description(SWAGGER_SECURITY_SCHEME_DESCRIPTION)));
    }
}