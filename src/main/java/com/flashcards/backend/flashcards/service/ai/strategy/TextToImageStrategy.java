package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.dto.AITextToImageRequestDto;
import com.flashcards.backend.flashcards.dto.AITextToImageResponseDto;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Strategy for generating educational images from text descriptions using DALL-E.
 * Creates flowcharts, diagrams, illustrations, etc. for educational purposes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextToImageStrategy implements AIImageOperationStrategy<AITextToImageRequestDto, AITextToImageResponseDto> {

    private long startTime; // Track generation time

    @Override
    public AITextToImageResponseDto execute(AITextToImageRequestDto input, AIModelEnum model) {
        throw new UnsupportedOperationException("Use AIImageGenerationService.executeImageOperation() instead");
    }

    @Override
    public ImagePrompt buildImagePrompt(AITextToImageRequestDto input) {
        // Track start time for metrics
        startTime = System.currentTimeMillis();

        // Enhance the description for educational context
        String enhancedPrompt = enhanceEducationalPrompt(input.getDescription());

        log.debug("Building image prompt: {}", enhancedPrompt);

        // Build image options
        AIModelEnum model = input.getModel() != null ? input.getModel() : getDefaultModel();
        ImageOptions options = buildImageOptions(input, model);

        return new ImagePrompt(enhancedPrompt, options);
    }

    @Override
    public AITextToImageResponseDto parseResponse(ImageResponse imageResponse, AITextToImageRequestDto input) {
        List<AITextToImageResponseDto.GeneratedImage> generatedImages = imageResponse.getResults().stream()
            .map(this::convertToGeneratedImage)
            .toList();

        log.info("Successfully generated {} images", generatedImages.size());

        return AITextToImageResponseDto.builder()
            .images(generatedImages)
            .modelUsed(input.getModel() != null ? input.getModel() : getDefaultModel())
            .originalDescription(input.getDescription())
            .revisedPrompt(extractRevisedPrompt(imageResponse))
            .generatedAt(Instant.now())
            .generationTimeMs(System.currentTimeMillis() - startTime)
            .build();
    }

    @Override
    public AIModelEnum getDefaultModel() {
        return AIModelEnum.DALL_E_3;
    }

    @Override
    public void validateInput(AITextToImageRequestDto input) {
        requireNonNull(input, "Image generation request cannot be null");
        requireNonNull(input.getDescription(), "Description cannot be null");
        requireNonNull(input.getUserId(), "User ID cannot be null");

        // Validate count
        if (input.getCount() < 1 || input.getCount() > 4) {
            throw new ServiceException(
                "Image count must be between 1 and 4, got: " + input.getCount(),
                ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }

        // Validate description length
        if (input.getDescription().length() > 1000) {
            throw new ServiceException(
                "Description cannot exceed 1000 characters",
                ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }
    }

    @Override
    public String getOperationName() {
        return "TextToImage";
    }

    @Override
    public ImageOptions buildImageOptions(AITextToImageRequestDto input, AIModelEnum model) {
        // Note: Only OpenAI DALL-E supported currently
        // Spring AI 1.0.2 uses different method names
        return OpenAiImageOptions.builder()
            .model(model.getModelId())
            .width(extractWidth(input.getSize()))
            .height(extractHeight(input.getSize()))
            .quality(input.getQuality())
            .style(input.getStyle())
            .build();
    }

    /**
     * Enhance the prompt with educational context.
     */
    private String enhanceEducationalPrompt(String originalDescription) {
        // Add context to make it clear this is for educational purposes
        return "Educational diagram or illustration: " + originalDescription +
            ". The image should be clear, professional, and suitable for learning materials.";
    }

    /**
     * Convert ImageGeneration to GeneratedImage DTO.
     */
    private AITextToImageResponseDto.GeneratedImage convertToGeneratedImage(ImageGeneration generation) {
        return AITextToImageResponseDto.GeneratedImage.builder()
            .url(generation.getOutput().getUrl())
            .b64Json(generation.getOutput().getB64Json())
            .revisedPrompt(extractRevisedPromptFromMetadata(generation))
            .build();
    }

    /**
     * Extract revised prompt from response metadata (DALL-E 3 feature).
     */
    private String extractRevisedPrompt(ImageResponse response) {
        if (response.getResults().isEmpty()) {
            return null;
        }

        ImageGeneration first = response.getResults().getFirst();
        return extractRevisedPromptFromMetadata(first);
    }

    /**
     * Extract revised prompt from image generation metadata.
     * Note: Spring AI 1.0.2 may not expose revised prompt directly.
     * This is a DALL-E 3 feature that may need custom extraction.
     */
    private String extractRevisedPromptFromMetadata(ImageGeneration generation) {
        if (generation.getMetadata() == null) {
            return null;
        }

        // Spring AI 1.0.2: Metadata structure varies by provider
        // For DALL-E 3, the revised prompt may not be directly accessible
        // through the standard metadata interface in this version
        // Return null for now - can be enhanced when Spring AI adds direct support
        // TODO: Check if future Spring AI versions expose revised prompt directly
        return null;
    }

    /**
     * Extract width from size string (e.g., "1024x1024" -> 1024).
     */
    private Integer extractWidth(String size) {
        if (size == null || !size.contains("x")) {
            return 1024; // default
        }
        return Integer.parseInt(size.split("x")[0]);
    }

    /**
     * Extract height from size string (e.g., "1024x1024" -> 1024).
     */
    private Integer extractHeight(String size) {
        if (size == null || !size.contains("x")) {
            return 1024; // default
        }
        return Integer.parseInt(size.split("x")[1]);
    }
}