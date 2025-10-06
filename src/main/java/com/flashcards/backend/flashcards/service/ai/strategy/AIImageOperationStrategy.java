package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ServiceException;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;

/**
 * Base strategy interface for AI image generation operations.
 * Separate from AIOperationStrategy because image generation uses ImageModel API, not ChatModel.
 *
 * @param <I> Input type for the operation (e.g., AIImageGenerateRequestDto)
 * @param <O> Output type for the operation (e.g., AIImageGenerateResponseDto)
 */
public interface AIImageOperationStrategy<I, O> {

    /**
     * Execute the AI image generation operation with the given input and model.
     *
     * @param input The operation-specific input
     * @param model The AI model to use
     * @return The operation-specific output
     */
    O execute(I input, AIModelEnum model);

    /**
     * Build the image prompt for this operation.
     *
     * @param input The operation-specific input
     * @return ImagePrompt ready for the AI model
     */
    ImagePrompt buildImagePrompt(I input);

    /**
     * Parse the image generation response into the expected output format.
     *
     * @param imageResponse The image generation response from the AI
     * @param input The original input (may be needed for context)
     * @return The parsed output
     */
    O parseResponse(ImageResponse imageResponse, I input);

    /**
     * Get the default model to use if none is specified.
     *
     * @return The default AI model for this operation
     */
    AIModelEnum getDefaultModel();

    /**
     * Validate the input before processing.
     *
     * @param input The operation-specific input
     * @throws ServiceException if validation fails
     */
    void validateInput(I input);

    /**
     * Get a human-readable name for this operation (for logging/errors).
     *
     * @return The operation name
     */
    String getOperationName();

    /**
     * Build ImageOptions for the specific model.
     *
     * @param input The operation-specific input
     * @param model The AI model to use
     * @return ImageOptions configured for the operation
     */
    ImageOptions buildImageOptions(I input, AIModelEnum model);
}