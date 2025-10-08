# Audio Features Sequence Diagrams

This document contains sequence diagrams for AI-powered audio features including Text-to-Speech (TTS) and Speech-to-Text (STT) with post-processing.

## 1. Text-to-Speech (Full Recitation)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIAudioController
    participant AIAudioService
    participant TextToSpeechStrategy
    participant OpenAIChatModel as GPT-4o-mini<br/>(for summary)
    participant OpenAISpeechModel as OpenAI TTS Model<br/>(tts-1 or tts-1-hd)

    User->>Frontend: Paste article text<br/>Select "Full Recitation"<br/>Choose voice (e.g., Nova)
    Frontend->>AIAudioController: POST /api/audio/text-to-speech<br/>{userId, text, outputType: "RECITATION",<br/>voice: "NOVA", speed: 1.0}

    AIAudioController->>AIAudioController: Validate JWT<br/>Validate @Valid annotations

    AIAudioController->>AIAudioService: convertTextToSpeech(request)

    AIAudioService->>TextToSpeechStrategy: validateInput(request)
    TextToSpeechStrategy->>TextToSpeechStrategy: Check text not blank<br/>Check length ≤ 4096 chars

    alt Validation passes
        TextToSpeechStrategy-->>AIAudioService: Valid

        AIAudioService->>TextToSpeechStrategy: execute(request)

        Note over TextToSpeechStrategy: Output type is RECITATION<br/>Skip summarization step

        TextToSpeechStrategy->>TextToSpeechStrategy: Build OpenAiAudioSpeechOptions<br/>model: tts-1<br/>voice: NOVA<br/>format: mp3<br/>speed: 1.0

        TextToSpeechStrategy->>OpenAISpeechModel: call(SpeechPrompt(text, options))

        OpenAISpeechModel->>OpenAISpeechModel: Generate audio from text<br/>using selected voice

        OpenAISpeechModel-->>TextToSpeechStrategy: Return SpeechResponse<br/>with audio bytes

        TextToSpeechStrategy->>TextToSpeechStrategy: Encode audio to base64

        TextToSpeechStrategy-->>AIAudioService: Return AITextToSpeechResponseDto<br/>{audioData (base64), format: "mp3",<br/>voice, outputType, processedText, model}

        AIAudioService-->>AIAudioController: Return response

        AIAudioController-->>Frontend: 200 OK<br/>{audioData, format, durationSeconds, ...}

        Frontend->>Frontend: Decode base64 to Blob<br/>Create audio element
        Frontend->>User: Play audio with controls

    else Validation fails
        TextToSpeechStrategy-->>AIAudioService: Throw ServiceException<br/>"Text exceeds max length"
        AIAudioService-->>AIAudioController: Propagate exception
        AIAudioController-->>Frontend: 400 Bad Request<br/>{errorCode: "SVC_009"}
        Frontend->>User: Show error message
    end
```

## 2. Text-to-Speech (Audio Summary)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIAudioController
    participant AIAudioService
    participant TextToSpeechStrategy
    participant OpenAIChatModel as GPT-4o-mini<br/>(for summary generation)
    participant OpenAISpeechModel as OpenAI TTS Model

    User->>Frontend: Paste long article (2000+ words)<br/>Select "Audio Summary"<br/>Set word count: 250
    Frontend->>AIAudioController: POST /api/audio/text-to-speech<br/>{userId, text, outputType: "SUMMARY",<br/>voice: "ALLOY", summaryWordCount: 250}

    AIAudioController->>AIAudioService: convertTextToSpeech(request)

    AIAudioService->>TextToSpeechStrategy: execute(request)

    TextToSpeechStrategy->>TextToSpeechStrategy: Detect outputType = SUMMARY<br/>Trigger summarization

    TextToSpeechStrategy->>TextToSpeechStrategy: Build summarization prompt:<br/>AUDIO_SUMMARY_TEMPLATE<br/>Variables: {text, wordCount: 250}

    TextToSpeechStrategy->>OpenAIChatModel: call(prompt)

    OpenAIChatModel->>OpenAIChatModel: Generate audio-optimized summary<br/>Target: 250 words<br/>Natural for listening

    OpenAIChatModel-->>TextToSpeechStrategy: Return summary text

    TextToSpeechStrategy->>TextToSpeechStrategy: Log summary generation<br/>textToConvert = summary

    TextToSpeechStrategy->>TextToSpeechStrategy: Build speech options<br/>voice: ALLOY

    TextToSpeechStrategy->>OpenAISpeechModel: call(SpeechPrompt(summary, options))

    OpenAISpeechModel-->>TextToSpeechStrategy: Return audio bytes

    TextToSpeechStrategy->>TextToSpeechStrategy: Encode to base64

    TextToSpeechStrategy-->>AIAudioService: Return response<br/>processedText = summary (not original)

    AIAudioService-->>AIAudioController: Return response

    AIAudioController-->>Frontend: 200 OK<br/>{audioData, processedText (summary shown)}

    Frontend->>User: Play summary audio<br/>Show summarized text
```

## 3. Text-to-Speech Streaming (Binary Audio)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIAudioController
    participant AIAudioService
    participant TextToSpeechStrategy
    participant OpenAISpeechModel

    User->>Frontend: Request audio for article
    Frontend->>AIAudioController: POST /api/audio/text-to-speech/stream<br/>Content-Type: application/json<br/>{userId, text, voice: "FABLE", speed: 1.25}

    Note over Frontend,AIAudioController: Design fixed from GET with query params<br/>to POST with JSON body to avoid URL length limits

    AIAudioController->>AIAudioService: convertTextToSpeechStream(request)

    AIAudioService->>TextToSpeechStrategy: execute(request)

    TextToSpeechStrategy->>OpenAISpeechModel: call(SpeechPrompt)
    OpenAISpeechModel-->>TextToSpeechStrategy: Return audio bytes

    TextToSpeechStrategy->>TextToSpeechStrategy: Encode to base64

    TextToSpeechStrategy-->>AIAudioService: Return AITextToSpeechResponseDto

    AIAudioService->>AIAudioService: Decode base64 to raw bytes

    AIAudioService-->>AIAudioController: Return byte[]

    AIAudioController->>AIAudioController: Set headers:<br/>Content-Type: audio/mpeg<br/>Content-Length: {size}<br/>Content-Disposition: inline

    AIAudioController-->>Frontend: 200 OK<br/>Body: Raw MP3 bytes<br/>(binary stream)

    Frontend->>Frontend: Create Blob from response<br/>const audioUrl = URL.createObjectURL(blob)

    Frontend->>User: Play audio directly<br/>without decode step

    Note over Frontend: Streaming endpoint preferred<br/>for frontend applications:<br/>- No base64 overhead<br/>- Direct browser playback<br/>- Smaller payload
```

## 4. Speech-to-Text (Transcription Only)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIAudioController
    participant AIAudioService
    participant SpeechToTextStrategy
    participant OpenAIWhisperModel as Whisper-1 Model

    User->>Frontend: Record audio via microphone<br/>or upload audio file
    Frontend->>AIAudioController: POST /api/audio/speech-to-text<br/>Content-Type: multipart/form-data<br/>{userId, audioFile, action: "TRANSCRIPTION_ONLY"}

    AIAudioController->>AIAudioController: Validate file:<br/>- Type: audio/*, .mp3, .wav, .m4a<br/>- Size: ≤ 25 MB (Whisper limit)

    AIAudioController->>AIAudioService: convertSpeechToText(request)

    AIAudioService->>SpeechToTextStrategy: execute(request)

    SpeechToTextStrategy->>SpeechToTextStrategy: Extract audio bytes from MultipartFile<br/>Get original filename

    SpeechToTextStrategy->>SpeechToTextStrategy: Create ByteArrayResource with filename<br/>(OpenAI API requires filename)

    SpeechToTextStrategy->>SpeechToTextStrategy: Build AudioTranscriptionOptions<br/>model: whisper-1<br/>language: en (optional)<br/>temperature: 0.0

    SpeechToTextStrategy->>OpenAIWhisperModel: call(AudioTranscriptionPrompt)

    OpenAIWhisperModel->>OpenAIWhisperModel: Transcribe audio<br/>Detect language<br/>Calculate duration

    OpenAIWhisperModel-->>SpeechToTextStrategy: Return AudioTranscriptionResponse<br/>{transcribedText, metadata}

    SpeechToTextStrategy->>SpeechToTextStrategy: Build AISpeechToTextResponseDto<br/>actionPerformed: "TRANSCRIPTION_ONLY"

    SpeechToTextStrategy-->>AIAudioService: Return response

    AIAudioService->>AIAudioService: Check action = "TRANSCRIPTION_ONLY"<br/>Skip post-processing

    AIAudioService-->>AIAudioController: Return response

    AIAudioController-->>Frontend: 200 OK<br/>{transcribedText, detectedLanguage,<br/>durationSeconds, model}

    Frontend->>User: Display transcribed text<br/>with edit capability
```

## 5. Speech-to-Text with Summary Generation

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIAudioController
    participant AIAudioService
    participant SpeechToTextStrategy
    participant OpenAIWhisperModel
    participant AIExecutionService
    participant ContentToSummaryStrategy
    participant OpenAIChatModel

    User->>Frontend: Record lecture audio (30 mins)
    Frontend->>AIAudioController: POST /api/audio/speech-to-text<br/>{userId, audioFile, action: "SUMMARY"}

    AIAudioController->>AIAudioService: convertSpeechToText(request)

    AIAudioService->>SpeechToTextStrategy: execute(request)

    Note over SpeechToTextStrategy: Step 1: Transcribe audio

    SpeechToTextStrategy->>OpenAIWhisperModel: call(AudioTranscriptionPrompt)
    OpenAIWhisperModel-->>SpeechToTextStrategy: Return transcribed text

    SpeechToTextStrategy-->>AIAudioService: Return base response with transcription

    AIAudioService->>AIAudioService: Check action = "SUMMARY"<br/>Trigger post-processing

    Note over AIAudioService: Step 2: Generate summary

    AIAudioService->>AIAudioService: processTranscriptionForSummary()<br/>Build AISummaryRequestDto<br/>sourceType: TEXT<br/>format: PARAGRAPH<br/>length: MEDIUM

    AIAudioService->>AIExecutionService: executeOperation(<br/>contentToSummaryStrategy,<br/>summaryRequest, GPT_4O_MINI)

    AIExecutionService->>ContentToSummaryStrategy: execute(summaryRequest)

    ContentToSummaryStrategy->>OpenAIChatModel: call(summarization prompt)
    OpenAIChatModel-->>ContentToSummaryStrategy: Return summary

    ContentToSummaryStrategy-->>AIExecutionService: Return AISummaryResponseDto

    AIExecutionService-->>AIAudioService: Return summary

    AIAudioService->>AIAudioService: Attach summary to base response<br/>Set actionPerformed: "SUMMARY"

    AIAudioService-->>AIAudioController: Return complete response

    AIAudioController-->>Frontend: 200 OK<br/>{transcribedText, summary: {summary, wordCount},<br/>actionPerformed: "SUMMARY"}

    Frontend->>User: Display:<br/>1. Full transcription (expandable)<br/>2. Summary (highlighted)
```

## 6. Speech-to-Text with Flashcard Generation

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIAudioController
    participant AIAudioService
    participant SpeechToTextStrategy
    participant OpenAIWhisperModel
    participant AIExecutionService
    participant TextToFlashcardsStrategy
    participant OpenAIChatModel

    User->>Frontend: Record study notes audio<br/>Select target deck<br/>Set flashcard count: 10
    Frontend->>AIAudioController: POST /api/audio/speech-to-text<br/>{userId, audioFile, action: "FLASHCARDS",<br/>deckId, flashcardCount: 10}

    AIAudioController->>AIAudioService: convertSpeechToText(request)

    AIAudioService->>SpeechToTextStrategy: execute(request)

    Note over SpeechToTextStrategy: Step 1: Transcribe

    SpeechToTextStrategy->>OpenAIWhisperModel: call(AudioTranscriptionPrompt)
    OpenAIWhisperModel-->>SpeechToTextStrategy: Return transcribed text

    SpeechToTextStrategy-->>AIAudioService: Return transcription response

    AIAudioService->>AIAudioService: Check action = "FLASHCARDS"<br/>Trigger flashcard generation

    Note over AIAudioService: Step 2: Generate flashcards

    AIAudioService->>AIAudioService: processTranscriptionForFlashcards()<br/>Build AIGenerateRequestDto<br/>text: transcribedText<br/>count: 10<br/>model: GPT_4O_MINI

    AIAudioService->>AIExecutionService: executeOperation(<br/>textToFlashcardsStrategy,<br/>flashcardRequest, GPT_4O_MINI)

    AIExecutionService->>TextToFlashcardsStrategy: execute(flashcardRequest)

    TextToFlashcardsStrategy->>OpenAIChatModel: call(flashcard generation prompt)

    OpenAIChatModel->>OpenAIChatModel: Generate flashcards from transcription<br/>Extract key concepts<br/>Create Q&A pairs

    OpenAIChatModel-->>TextToFlashcardsStrategy: Return flashcard JSON

    TextToFlashcardsStrategy->>TextToFlashcardsStrategy: Parse JSON to List<CreateFlashcardDto>

    TextToFlashcardsStrategy-->>AIExecutionService: Return flashcards

    AIExecutionService-->>AIAudioService: Return List<CreateFlashcardDto>

    AIAudioService->>AIAudioService: Attach flashcards to base response<br/>Set actionPerformed: "FLASHCARDS"

    AIAudioService-->>AIAudioController: Return complete response

    AIAudioController-->>Frontend: 200 OK<br/>{transcribedText, flashcards: [...]<br/>actionPerformed: "FLASHCARDS"}

    Frontend->>User: Display:<br/>1. Transcription<br/>2. Generated flashcards (editable)<br/>3. Save to deck option

    User->>Frontend: Review and save flashcards
    Frontend->>AIAudioController: POST /api/flashcards/bulk-create<br/>(save edited flashcards)
```

## Implementation Status

| Feature | Status | Models Used | Notes |
|---------|--------|-------------|-------|
| TTS - Full Recitation | ✅ Implemented | OpenAI TTS-1, TTS-1-HD | 6 voices available |
| TTS - Audio Summary | ✅ Implemented | GPT-4o-mini (summary) + TTS-1 (audio) | Two-step process |
| TTS - Binary Streaming | ✅ Implemented | OpenAI TTS-1 | Preferred for frontend |
| STT - Transcription | ✅ Implemented | Whisper-1 | ≤25MB file limit |
| STT - With Summary | ✅ Implemented | Whisper-1 + GPT-4o-mini | Post-processing |
| STT - With Flashcards | ✅ Implemented | Whisper-1 + GPT-4o-mini | Full pipeline |

## API Endpoints

| Endpoint | Method | Input | Output | Use Case |
|----------|--------|-------|--------|----------|
| `/api/audio/text-to-speech` | POST | JSON with text | JSON with base64 audio + metadata | Backend integration, metadata needed |
| `/api/audio/text-to-speech/stream` | POST | JSON with text | Binary MP3 stream | Frontend audio player, direct playback |
| `/api/audio/speech-to-text` | POST | Multipart with audio file | JSON with transcription ± processing | Voice notes, lecture transcription |

## Audio Configuration

### Supported Voices (OpenAI TTS)

| Voice | Description | Best For |
|-------|-------------|----------|
| ALLOY | Neutral and balanced | General purpose, professional content |
| ECHO | Clear and articulate | Educational content, tutorials |
| FABLE | Warm and expressive | Storytelling, narrative content |
| ONYX | Deep and authoritative | Serious topics, announcements |
| NOVA | Energetic and friendly | Engaging content, conversational |
| SHIMMER | Soft and calm | Meditation, relaxing content |

### Audio Formats and Limits

**Text-to-Speech:**
- Input: Text (max 4096 characters per request)
- Output: MP3 (default), OPUS, AAC, FLAC
- Models: tts-1 (faster), tts-1-hd (higher quality)
- Speed: 0.25x to 4.0x (default: 1.0x)

**Speech-to-Text:**
- Input: Audio files (mp3, wav, m4a, webm, etc.)
- Max file size: 25 MB (Whisper API limit)
- Max duration: ~3 hours (depends on quality)
- Output: Plain text transcription
- Language detection: Automatic

## Code Quality Issues

### Issues Identified:

1. **Bean Injection Conflict** (FIXED)
   - ✅ Multiple ChatModel beans caused autowiring failure
   - ✅ Fixed with @Qualifier("openAiChatModel") in TextToSpeechStrategy

2. **Missing Filename** (FIXED)
   - ✅ ByteArrayResource needed filename for OpenAI API
   - ✅ Fixed by overriding getFilename() method

3. **Missing SourceType** (FIXED)
   - ✅ NullPointerException in summary validation
   - ✅ Fixed by adding sourceType to AISummaryRequestDto builder

4. **Query Parameter Design** (FIXED)
   - ✅ Original design used GET with query params for streaming
   - ✅ Fixed to POST with JSON body to avoid URL length limits

5. **Error Handling**
   - ⚠️ Generic ServiceException for all audio errors
   - **Fix:** Add specific error codes (AUDIO_FILE_TOO_LARGE, UNSUPPORTED_AUDIO_FORMAT, etc.)

6. **Validation**
   - ⚠️ File type validation is basic (checks MIME type only)
   - ⚠️ No duration pre-check before sending to Whisper
   - **Fix:** Add audio file validation utility (check codec, duration, sample rate)

7. **Testing**
   - ❌ No unit tests for audio strategies
   - ❌ No integration tests with real audio files
   - **Fix:** Add tests with mock audio responses

8. **Rate Limiting**
   - ❌ No rate limiting on audio endpoints
   - Risk: Expensive audio API calls can be abused
   - **Fix:** Implement stricter limits (e.g., 5 TTS requests/minute, 3 STT requests/minute)

9. **Cost Tracking**
   - ❌ No tracking of audio generation costs
   - OpenAI TTS pricing: $15 per 1M characters
   - Whisper pricing: $0.006 per minute
   - **Fix:** Add usage metrics and cost calculation

## Security Considerations

1. **File Upload Security**
   - ⚠️ Basic validation of audio files
   - Risk: Malicious files could be uploaded
   - **Fix:**
     - Scan audio files for malware
     - Validate audio codecs and containers
     - Limit concurrent uploads per user

2. **Audio Content Moderation**
   - ❌ No content moderation on transcribed text
   - Risk: Inappropriate audio content transcribed
   - **Fix:** Add content filtering on STT output

3. **Generated Audio Storage**
   - ❌ Audio not stored, generated on-demand each time
   - Pro: No storage costs, privacy-friendly
   - Con: Repeated generation costs
   - **Consider:** Cache audio for repeated requests (with TTL)

4. **API Key Exposure**
   - ✅ OpenAI API key in environment variables
   - ✅ Not exposed to frontend
   - ✅ Server-side only

## Performance Considerations

1. **TTS Response Times**
   - Short text (<500 chars): ~2-3 seconds
   - Long text (2000+ chars): ~8-10 seconds
   - Summary generation adds ~3-5 seconds
   - **Optimization:** Consider caching common audio (e.g., deck intros)

2. **STT Response Times**
   - 1 minute audio: ~5-10 seconds
   - 10 minute audio: ~30-60 seconds
   - 30 minute audio: ~2-3 minutes
   - **Optimization:** Consider async processing for long audio

3. **Streaming vs Base64**
   - Base64 encoding increases payload size by ~33%
   - Streaming endpoint reduces data transfer and processing
   - **Recommendation:** Frontend should use streaming endpoint

## Related Documentation

- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md)
- [AI Operations Sequence Diagrams](./Sequence-Diagrams-AI-Operations.md)
- [Authentication Flow](./Sequence-Diagrams-Authentication.md)
- [AI Endpoints Code Improvements](./AI-Endpoints-Code-Improvements.md) (to be created)
- [Rate Limiting with Redis](./Rate-Limiting-Redis-Design.md) (to be created)