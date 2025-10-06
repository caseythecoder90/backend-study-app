package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Base strategy interface for AI operations.
 * Defines the contract for different AI-powered operations (text generation, vision, etc.).
 *
 * @param <I> Input type for the operation (e.g., AIGenerateRequestDto, AIImageGenerateRequestDto)
 * @param <O> Output type for the operation (e.g., List&lt;CreateFlashcardDto&gt;, AISummaryResponseDto)
 */
public interface AIOperationStrategy<I, O> {

    /**
     * Execute the AI operation with the given input and model.
     *
     * @param input The operation-specific input
     * @param model The AI model to use
     * @return The operation-specific output
     */
    O execute(I input, AIModelEnum model);

    /**
     * Build the prompt/message for this operation.
     *
     * @param input The operation-specific input
     * @return Prompt or UserMessage ready for the AI model
     */
    Message buildMessage(I input);

    /**
     * Parse the AI response into the expected output format.
     *
     * @param response The raw AI response text
     * @param input The original input (may be needed for context)
     * @return The parsed output
     */
    O parseResponse(String response, I input);

    /**
     * Check if this operation requires vision capabilities.
     *
     * @return true if vision models are required, false otherwise
     */
    default boolean requiresVision() {
        return false;
    }

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
     * @throws com.flashcards.backend.flashcards.exception.ServiceException if validation fails
     */
    void validateInput(I input);

    /**
     * Get a human-readable name for this operation (for logging/errors).
     *
     * @return The operation name
     */
    String getOperationName();
}