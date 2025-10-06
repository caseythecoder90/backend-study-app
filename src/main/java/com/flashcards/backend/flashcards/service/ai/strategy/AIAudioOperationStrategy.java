package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.exception.ServiceException;

/**
 * Base strategy interface for AI audio operations (TTS and STT).
 * Audio operations have different execution patterns than chat-based operations.
 *
 * @param <I> Input type for the operation
 * @param <O> Output type for the operation
 */
public interface AIAudioOperationStrategy<I, O> {

    /**
     * Execute the audio operation with the given input.
     *
     * @param input The operation-specific input
     * @return The operation-specific output
     */
    O execute(I input);

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
}