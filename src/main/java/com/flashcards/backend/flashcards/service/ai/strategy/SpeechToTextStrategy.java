package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AISpeechToTextRequestDto;
import com.flashcards.backend.flashcards.dto.AISpeechToTextResponseDto;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.flashcards.backend.flashcards.constants.AIConstants.DEFAULT_STT_MODEL;
import static com.flashcards.backend.flashcards.constants.AIConstants.DEFAULT_STT_RESPONSE_FORMAT;
import static com.flashcards.backend.flashcards.constants.AIConstants.MAX_AUDIO_FILE_SIZE_MB;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Strategy for converting speech to text (transcription).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpeechToTextStrategy implements AIAudioOperationStrategy<AISpeechToTextRequestDto, AISpeechToTextResponseDto> {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final AIConfigProperties aiProperties;

    @Override
    public AISpeechToTextResponseDto execute(AISpeechToTextRequestDto input) {
        validateInput(input);

        String transcribedText = transcribeAudio(input);

        return AISpeechToTextResponseDto.builder()
                .transcribedText(transcribedText)
                .model(DEFAULT_STT_MODEL)
                .actionPerformed(Objects.nonNull(input.getAction()) ? input.getAction() : "TRANSCRIPTION_ONLY")
                .build();
    }

    private String transcribeAudio(AISpeechToTextRequestDto input) {
        try {
            byte[] audioBytes = input.getAudioFile().getBytes();
            String originalFilename = input.getAudioFile().getOriginalFilename();

            // Create a ByteArrayResource with a filename
            Resource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return Objects.nonNull(originalFilename) ? originalFilename : "audio.mp3";
                }
            };

            OpenAiAudioTranscriptionOptions.Builder optionsBuilder = OpenAiAudioTranscriptionOptions.builder()
                    .model(DEFAULT_STT_MODEL)
                    .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.valueOf(DEFAULT_STT_RESPONSE_FORMAT.toUpperCase()));

            // Add language if specified
            if (isNotBlank(input.getLanguage()) && !Objects.equals(input.getLanguage(), "auto")) {
                optionsBuilder.language(input.getLanguage());
            }

            // Add prompt if specified (helps guide the transcription)
            if (isNotBlank(input.getPrompt())) {
                optionsBuilder.prompt(input.getPrompt());
            }

            OpenAiAudioTranscriptionOptions options = optionsBuilder.build();

            AudioTranscriptionPrompt transcriptionPrompt = new AudioTranscriptionPrompt(audioResource, options);
            AudioTranscriptionResponse response = transcriptionModel.call(transcriptionPrompt);

            String transcription = response.getResult().getOutput();

            log.debug("Transcribed {} bytes of audio to {} characters of text",
                    audioBytes.length, transcription.length());

            return transcription;

        } catch (Exception e) {
            log.error("Failed to transcribe audio: {}", e.getMessage(), e);
            throw new ServiceException(
                    "Failed to transcribe audio file",
                    ErrorCode.SERVICE_AI_GENERATION_ERROR,
                    e
            );
        }
    }

    @Override
    public void validateInput(AISpeechToTextRequestDto input) {
        if (Objects.isNull(input.getAudioFile()) || input.getAudioFile().isEmpty()) {
            throw new ServiceException(
                    "Audio file is required for transcription",
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        // Validate file size
        long fileSizeInMB = input.getAudioFile().getSize() / (1024 * 1024);
        if (fileSizeInMB > MAX_AUDIO_FILE_SIZE_MB) {
            throw new ServiceException(
                    "Audio file size exceeds maximum allowed size of %d MB".formatted(MAX_AUDIO_FILE_SIZE_MB),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }

        // If action is FLASHCARDS, validate deckId and flashcardCount
        if (Objects.equals(input.getAction(), "FLASHCARDS")) {
            if (Objects.isNull(input.getDeckId()) || input.getDeckId().isBlank()) {
                throw new ServiceException(
                        "Deck ID is required when generating flashcards",
                        ErrorCode.SERVICE_VALIDATION_ERROR
                );
            }
            if (Objects.isNull(input.getFlashcardCount()) || input.getFlashcardCount() <= 0) {
                throw new ServiceException(
                        "Flashcard count must be positive when generating flashcards",
                        ErrorCode.SERVICE_VALIDATION_ERROR
                );
            }
        }
    }

    @Override
    public String getOperationName() {
        return "SpeechToText";
    }
}