package com.flashcards.backend.flashcards.service.ai.strategy;

import com.flashcards.backend.flashcards.config.AIConfigProperties;
import com.flashcards.backend.flashcards.dto.AITextToSpeechRequestDto;
import com.flashcards.backend.flashcards.dto.AITextToSpeechResponseDto;
import com.flashcards.backend.flashcards.enums.AudioOutputType;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static com.flashcards.backend.flashcards.constants.AIConstants.AUDIO_SUMMARY_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.AIConstants.DEFAULT_AUDIO_FORMAT;
import static com.flashcards.backend.flashcards.constants.AIConstants.DEFAULT_TTS_MODEL;
import static com.flashcards.backend.flashcards.constants.AIConstants.MAX_AUDIO_TEXT_LENGTH;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.AI_TEXT_LENGTH_EXCEEDED;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Strategy for converting text to speech with optional summarization.
 */
@Slf4j
@Component
public class TextToSpeechStrategy implements AIAudioOperationStrategy<AITextToSpeechRequestDto, AITextToSpeechResponseDto> {

    private final OpenAiAudioSpeechModel speechModel;
    private final ChatModel chatModel;
    private final AIConfigProperties aiProperties;

    public TextToSpeechStrategy(
            OpenAiAudioSpeechModel speechModel,
            @Qualifier("openAiChatModel") ChatModel chatModel,
            AIConfigProperties aiProperties) {
        this.speechModel = speechModel;
        this.chatModel = chatModel;
        this.aiProperties = aiProperties;
    }

    @Override
    public AITextToSpeechResponseDto execute(AITextToSpeechRequestDto input) {
        validateInput(input);

        String textToConvert = input.getText();

        // If summary requested, generate it first
        if (Objects.equals(input.getOutputType(), AudioOutputType.SUMMARY)) {
            textToConvert = generateSummary(input.getText(), input.getSummaryWordCount());
            log.debug("Generated summary for TTS: {}", textToConvert);
        }

        // Convert to speech
        byte[] audioBytes = convertToSpeech(textToConvert, input);

        // Build response
        return AITextToSpeechResponseDto.builder()
                .audioData(Base64.getEncoder().encodeToString(audioBytes))
                .format(DEFAULT_AUDIO_FORMAT)
                .voice(input.getVoice())
                .outputType(input.getOutputType())
                .processedText(textToConvert)
                .model(DEFAULT_TTS_MODEL)
                .build();
    }

    private String generateSummary(String text, Integer wordCount) {
        Map<String, Object> variables = Map.of(
                "text", text,
                "wordCount", Objects.nonNull(wordCount) ? wordCount : 250
        );

        PromptTemplate template = new PromptTemplate(AUDIO_SUMMARY_TEMPLATE);
        Prompt prompt = template.create(variables);

        String summary = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        return summary.trim();
    }

    private byte[] convertToSpeech(String text, AITextToSpeechRequestDto request) {
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .model(DEFAULT_TTS_MODEL)
                .voice(OpenAiAudioApi.SpeechRequest.Voice.valueOf(request.getVoice().name()))
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.valueOf(DEFAULT_AUDIO_FORMAT.toUpperCase()))
                .speed(request.getSpeed().floatValue())
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(text, options);
        SpeechResponse response = speechModel.call(speechPrompt);

        return response.getResult().getOutput();
    }

    @Override
    public void validateInput(AITextToSpeechRequestDto input) {
        if (isBlank(input.getText())) {
            throw new ServiceException(
                    "Text is required for text-to-speech conversion",
                    ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        if (input.getText().length() > MAX_AUDIO_TEXT_LENGTH) {
            throw new ServiceException(
                    AI_TEXT_LENGTH_EXCEEDED.formatted(MAX_AUDIO_TEXT_LENGTH),
                    ErrorCode.SERVICE_AI_INVALID_CONTENT
            );
        }
    }

    @Override
    public String getOperationName() {
        return "TextToSpeech";
    }
}