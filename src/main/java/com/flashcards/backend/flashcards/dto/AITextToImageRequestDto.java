package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating educational images from text descriptions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate educational images from text descriptions")
public class AITextToImageRequestDto {

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user requesting image generation", example = "user123")
    private String userId;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Description of the educational image to generate",
            example = "A flowchart showing the lifecycle of a Spring Boot application")
    private String description;

    @Schema(description = "AI model to use for generation (defaults to DALL-E 3)",
            example = "DALL_E_3")
    private AIModelEnum model;

    @Schema(description = "Image size (e.g., '1024x1024', '1792x1024', '1024x1792')",
            example = "1024x1024",
            defaultValue = "1024x1024")
    @Builder.Default
    private String size = "1024x1024";

    @Schema(description = "Image quality ('standard' or 'hd')",
            example = "standard",
            defaultValue = "standard")
    @Builder.Default
    private String quality = "standard";

    @Schema(description = "Number of images to generate (1-4)",
            example = "1",
            defaultValue = "1")
    @Builder.Default
    private Integer count = 1;

    @Schema(description = "Style of the image ('vivid' or 'natural')",
            example = "vivid",
            defaultValue = "vivid")
    @Builder.Default
    private String style = "vivid";
}