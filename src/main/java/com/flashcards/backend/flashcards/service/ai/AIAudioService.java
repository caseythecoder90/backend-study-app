package com.flashcards.backend.flashcards.service.ai;

import com.flashcards.backend.flashcards.dto.AIGenerateRequestDto;
import com.flashcards.backend.flashcards.dto.AISpeechToTextRequestDto;
import com.flashcards.backend.flashcards.dto.AISpeechToTextResponseDto;
import com.flashcards.backend.flashcards.dto.AISummaryRequestDto;
import com.flashcards.backend.flashcards.dto.AISummaryResponseDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechRequestDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechResponseDto;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.enums.AIModelEnum;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.service.ai.strategy.ContentToSummaryStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.SpeechToTextStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.TextToFlashcardsStrategy;
import com.flashcards.backend.flashcards.service.ai.strategy.TextToSpeechStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Service for handling AI audio operations (Text-to-Speech and Speech-to-Text).
 * Orchestrates audio strategies and optional post-processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAudioService {

    private final TextToSpeechStrategy textToSpeechStrategy;
    private final SpeechToTextStrategy speechToTextStrategy;
    private final AIExecutionService aiExecutionService;
    private final ContentToSummaryStrategy contentToSummaryStrategy;
    private final TextToFlashcardsStrategy textToFlashcardsStrategy;

    /**
     * Convert text to speech audio.
     *
     * @param request The TTS request containing text, voice, and options
     * @return Response with audio data
     */
    public AITextToSpeechResponseDto convertTextToSpeech(AITextToSpeechRequestDto request) {
        log.info("Converting text to speech for user: {}, outputType: {}, voice: {}",
                request.getUserId(), request.getOutputType(), request.getVoice());

        try {
            return textToSpeechStrategy.execute(request);
        } catch (ServiceException e) {
            log.error("Service exception in text-to-speech conversion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in text-to-speech conversion: {}", e.getMessage(), e);
            throw new ServiceException(
                    "Failed to convert text to speech",
                    ErrorCode.SERVICE_AI_GENERATION_ERROR,
                    e
            );
        }
    }

    /**
     * Convert text to speech audio and return raw bytes for streaming.
     *
     * @param request The TTS request containing text, voice, and options
     * @return Raw audio bytes
     */
    public byte[] convertTextToSpeechStream(AITextToSpeechRequestDto request) {
        log.info("Converting text to speech stream for user: {}, outputType: {}, voice: {}",
                request.getUserId(), request.getOutputType(), request.getVoice());

        try {
            AITextToSpeechResponseDto response = textToSpeechStrategy.execute(request);
            // Decode base64 to raw bytes
            return java.util.Base64.getDecoder().decode(response.getAudioData());
        } catch (ServiceException e) {
            log.error("Service exception in text-to-speech stream conversion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in text-to-speech stream conversion: {}", e.getMessage(), e);
            throw new ServiceException(
                    "Failed to convert text to speech stream",
                    ErrorCode.SERVICE_AI_GENERATION_ERROR,
                    e
            );
        }
    }

    /**
     * Convert speech to text and optionally process the transcription.
     *
     * @param request The STT request containing audio file and optional processing options
     * @return Response with transcribed text and optional processed results
     */
    public AISpeechToTextResponseDto convertSpeechToText(AISpeechToTextRequestDto request) {
        log.info("Converting speech to text for user: {}, action: {}",
                request.getUserId(), request.getAction());

        try {
            // Step 1: Transcribe audio
            AISpeechToTextResponseDto transcriptionResponse = speechToTextStrategy.execute(request);

            // Step 2: Process transcription if action specified
            if (isBlank(request.getAction()) || Objects.equals(request.getAction(), "TRANSCRIPTION_ONLY")) {
                return transcriptionResponse;
            }

            String transcribedText = transcriptionResponse.getTranscribedText();

            if (Objects.equals(request.getAction(), "SUMMARY")) {
                return processTranscriptionForSummary(transcriptionResponse, transcribedText, request);
            } else if (Objects.equals(request.getAction(), "FLASHCARDS")) {
                return processTranscriptionForFlashcards(transcriptionResponse, transcribedText, request);
            } else {
                log.warn("Unknown action type: {}, returning transcription only", request.getAction());
                return transcriptionResponse;
            }

        } catch (ServiceException e) {
            log.error("Service exception in speech-to-text conversion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in speech-to-text conversion: {}", e.getMessage(), e);
            throw new ServiceException(
                    "Failed to convert speech to text",
                    ErrorCode.SERVICE_AI_GENERATION_ERROR,
                    e
            );
        }
    }

    private AISpeechToTextResponseDto processTranscriptionForSummary(
            AISpeechToTextResponseDto baseResponse,
            String transcribedText,
            AISpeechToTextRequestDto request) {

        log.debug("Generating summary from transcribed text");

        AISummaryRequestDto summaryRequest = AISummaryRequestDto.builder()
                .userId(request.getUserId())
                .sourceType(AISummaryRequestDto.SummarySourceType.TEXT)
                .text(transcribedText)
                .format(AISummaryRequestDto.SummaryFormat.PARAGRAPH)
                .length(AISummaryRequestDto.SummaryLength.MEDIUM)
                .model(AIModelEnum.GPT_4O_MINI)
                .build();

        AISummaryResponseDto summary = aiExecutionService.executeOperation(
                contentToSummaryStrategy,
                summaryRequest,
                AIModelEnum.GPT_4O_MINI
        );

        baseResponse.setSummary(summary);
        baseResponse.setActionPerformed("SUMMARY");

        return baseResponse;
    }

    private AISpeechToTextResponseDto processTranscriptionForFlashcards(
            AISpeechToTextResponseDto baseResponse,
            String transcribedText,
            AISpeechToTextRequestDto request) {

        log.debug("Generating flashcards from transcribed text");

        AIGenerateRequestDto flashcardRequest = AIGenerateRequestDto.builder()
                .userId(request.getUserId())
                .deckId(request.getDeckId())
                .text(transcribedText)
                .count(Objects.nonNull(request.getFlashcardCount()) ? request.getFlashcardCount() : 5)
                .model(AIModelEnum.GPT_4O_MINI)
                .build();

        List<CreateFlashcardDto> flashcards = aiExecutionService.executeOperation(
                textToFlashcardsStrategy,
                flashcardRequest,
                AIModelEnum.GPT_4O_MINI
        );

        baseResponse.setFlashcards(flashcards);
        baseResponse.setActionPerformed("FLASHCARDS");

        return baseResponse;
    }
}