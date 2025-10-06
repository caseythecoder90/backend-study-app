package com.flashcards.backend.flashcards.annotation;

import com.flashcards.backend.flashcards.dto.AISpeechToTextResponseDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechRequestDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechResponseDto;
import com.flashcards.backend.flashcards.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag(name = "AI Audio Operations", description = "AI-powered audio processing including text-to-speech and speech-to-text")
public @interface AIAudioApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "Convert text to speech audio",
            description = "Converts text content to audio using OpenAI's TTS models. " +
                    "Supports different voices, speech rates, and can generate either full recitation or summarized audio. " +
                    "Perfect for creating audio study materials from text content.",
            tags = {"AI Audio Operations"}
    )
    @RequestBody(
            description = "Text-to-speech request containing text, voice selection, and audio options",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AITextToSpeechRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Full Recitation",
                                    summary = "Convert article to audio for listening",
                                    value = """
                                            {
                                              "userId": "user123",
                                              "text": "Spring Framework is a comprehensive programming and configuration model for Java applications. Dependency Injection is a core concept that promotes loose coupling and testability.",
                                              "outputType": "RECITATION",
                                              "voice": "ALLOY",
                                              "speed": 1.0
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Audio Summary",
                                    summary = "Generate audio summary of long content",
                                    value = """
                                            {
                                              "userId": "user456",
                                              "text": "The Factory Design Pattern is a creational pattern that provides an interface for creating objects in a superclass, but allows subclasses to alter the type of objects that will be created. This pattern is particularly useful when you have a superclass with multiple subclasses and you want to create objects based on input. The pattern involves creating a factory class that has a method for creating objects. This method takes some parameter and based on that parameter creates an object.",
                                              "outputType": "SUMMARY",
                                              "voice": "NOVA",
                                              "speed": 1.0,
                                              "summaryWordCount": 100
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated audio",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AITextToSpeechResponseDto.class),
                            examples = @ExampleObject(
                                    name = "Audio Response",
                                    summary = "Generated audio data with metadata",
                                    value = """
                                            {
                                              "audioData": "UklGRiQAAABXQVZFZm10...",
                                              "format": "mp3",
                                              "durationSeconds": 45.5,
                                              "voice": "ALLOY",
                                              "outputType": "RECITATION",
                                              "processedText": "Spring Framework is a comprehensive...",
                                              "model": "tts-1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Text content exceeds maximum allowed length of 4096",
                                              "path": "/api/audio/text-to-speech",
                                              "errorCode": "SVC_009"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "TTS service error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @interface TextToSpeech {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "Convert speech to text and optionally process it",
            description = "Transcribes audio files to text using OpenAI's Whisper model. " +
                    "Optionally processes the transcription by generating a summary or flashcards. " +
                    "Supports various audio formats and languages.",
            tags = {"AI Audio Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully transcribed audio",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AISpeechToTextResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Transcription Only",
                                            summary = "Simple transcription without processing",
                                            value = """
                                                    {
                                                      "transcribedText": "Today I learned about dependency injection in Spring Framework",
                                                      "detectedLanguage": "en",
                                                      "durationSeconds": 12.5,
                                                      "model": "whisper-1",
                                                      "actionPerformed": "TRANSCRIPTION_ONLY"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "With Summary",
                                            summary = "Transcription with automatic summary generation",
                                            value = """
                                                    {
                                                      "transcribedText": "Today I learned about...",
                                                      "detectedLanguage": "en",
                                                      "durationSeconds": 12.5,
                                                      "model": "whisper-1",
                                                      "summary": {
                                                        "summary": "The speaker discussed dependency injection...",
                                                        "wordCount": 50,
                                                        "format": "PARAGRAPH",
                                                        "length": "MEDIUM"
                                                      },
                                                      "actionPerformed": "SUMMARY"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid audio file or request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "File Size Error",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Audio file size exceeds maximum allowed size of 25 MB",
                                              "path": "/api/audio/speech-to-text",
                                              "errorCode": "SVC_009"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Speech-to-text service error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @interface SpeechToText {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "Convert text to speech and stream audio (binary)",
            description = "Converts text content to audio using OpenAI's TTS models and returns raw MP3 audio for direct playback. " +
                    "This endpoint is optimized for frontend applications - returns binary audio stream that can be played directly. " +
                    "Same request format as /text-to-speech but returns raw MP3 instead of JSON with base64.",
            tags = {"AI Audio Operations"}
    )
    @RequestBody(
            description = "Text-to-speech request (same as /text-to-speech endpoint)",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AITextToSpeechRequestDto.class),
                    examples = @ExampleObject(
                            name = "Stream Request",
                            summary = "Request for audio stream",
                            value = """
                                    {
                                      "userId": "user123",
                                      "text": "This is a long article that I want to listen to while commuting.",
                                      "outputType": "RECITATION",
                                      "voice": "NOVA",
                                      "speed": 1.25
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated audio stream",
                    content = @Content(
                            mediaType = "audio/mpeg",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "TTS service error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @interface TextToSpeechStream {}
}