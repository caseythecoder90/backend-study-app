package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 provider information and available authentication options")
public class OAuth2ProviderDto {

    @Schema(description = "List of available OAuth2 providers", example = "[\"google\", \"github\"]")
    private List<String> providers;

    @Schema(description = "Base URL for OAuth2 authorization endpoints",
            example = "http://localhost:8080/oauth2/authorization")
    private String authorizationBaseUrl;
}