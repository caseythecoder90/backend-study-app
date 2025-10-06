package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.AIAudioApiDocumentation;
import com.flashcards.backend.flashcards.dto.AISpeechToTextRequestDto;
import com.flashcards.backend.flashcards.dto.AISpeechToTextResponseDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechRequestDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechResponseDto;
import com.flashcards.backend.flashcards.service.ai.AIAudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@AIAudioApiDocumentation
public class AIAudioController {

    private final AIAudioService aiAudioService;

    @PostMapping("/text-to-speech")
    @PreAuthorize("hasRole('USER')")
    @AIAudioApiDocumentation.TextToSpeech
    public ResponseEntity<AITextToSpeechResponseDto> convertTextToSpeech(
            @Valid @RequestBody AITextToSpeechRequestDto request) {

        log.info("Converting text to speech for user: {}, outputType: {}, voice: {}",
                request.getUserId(), request.getOutputType(), request.getVoice());

        AITextToSpeechResponseDto response = aiAudioService.convertTextToSpeech(request);

        log.info("Successfully converted text to speech for user: {}", request.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/text-to-speech/stream", produces = "audio/mpeg")
    @PreAuthorize("hasRole('USER')")
    @AIAudioApiDocumentation.TextToSpeechStream
    public ResponseEntity<byte[]> convertTextToSpeechStream(
            @Valid @RequestBody AITextToSpeechRequestDto request) {

        log.info("Streaming text to speech for user: {}, outputType: {}, voice: {}",
                request.getUserId(), request.getOutputType(), request.getVoice());

        byte[] audioBytes = aiAudioService.convertTextToSpeechStream(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setContentLength(audioBytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"");

        log.info("Successfully streamed {} bytes of audio for user: {}", audioBytes.length, request.getUserId());
        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }

    @PostMapping(value = "/speech-to-text", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('USER')")
    @AIAudioApiDocumentation.SpeechToText
    public ResponseEntity<AISpeechToTextResponseDto> convertSpeechToText(
            @Valid @ModelAttribute AISpeechToTextRequestDto request) {

        log.info("Converting speech to text for user: {}, action: {}",
                request.getUserId(), request.getAction());

        AISpeechToTextResponseDto response = aiAudioService.convertSpeechToText(request);

        log.info("Successfully converted speech to text for user: {}, action: {}",
                request.getUserId(), response.getActionPerformed());
        return ResponseEntity.ok(response);
    }
}