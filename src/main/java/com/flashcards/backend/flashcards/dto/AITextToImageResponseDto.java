package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for AI-generated educational images.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing AI-generated educational images")
public class AITextToImageResponseDto {

    @Schema(description = "List of generated image URLs")
    private List<GeneratedImage> images;

    @Schema(description = "AI model used for generation", example = "DALL_E_3")
    private AIModelEnum modelUsed;

    @Schema(description = "Original description prompt",
            example = "A flowchart showing the lifecycle of a Spring Boot application")
    private String originalDescription;

    @Schema(description = "Revised/improved prompt used by the AI (if applicable)")
    private String revisedPrompt;

    @Schema(description = "Timestamp when images were generated")
    private Instant generatedAt;

    @Schema(description = "Time taken to generate images in milliseconds", example = "3450")
    private Long generationTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Information about a single generated image")
    public static class GeneratedImage {

        @Schema(description = "URL to access the generated image")
        private String url;

        @Schema(description = "Base64-encoded image data (if requested)")
        private String b64Json;

        @Schema(description = "Revised prompt for this specific image (DALL-E 3)")
        private String revisedPrompt;
    }
}