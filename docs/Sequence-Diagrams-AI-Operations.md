# AI Operations Sequence Diagrams

This document contains sequence diagrams for all AI-powered operations including flashcard generation, summarization, and prompt generation.

## 1. AI Flashcard Generation (Text-to-Flashcards with Fallback)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIController
    participant AIExecutionService
    participant TextToFlashcardsStrategy
    participant ChatModel as Primary AI Model<br/>(e.g., GPT-4o)
    participant FallbackModel as Fallback Model<br/>(e.g., Claude 3.5 Haiku)
    participant FlashcardService
    participant FlashcardDao
    participant MongoDB

    User->>Frontend: Paste article text<br/>Select deck<br/>Set flashcard count
    Frontend->>AIController: POST /api/ai/generate-flashcards<br/>{text, deckId, count, model}

    AIController->>AIController: Validate JWT token<br/>Extract userId

    AIController->>AIExecutionService: executeOperation(<br/>textToFlashcardsStrategy,<br/>request, model)

    AIExecutionService->>TextToFlashcardsStrategy: validateInput(request)
    TextToFlashcardsStrategy->>TextToFlashcardsStrategy: Check text length ≤ 10000<br/>Check count ≤ 20

    alt Validation passes
        TextToFlashcardsStrategy-->>AIExecutionService: Valid

        AIExecutionService->>AIExecutionService: selectAIModel(modelEnum)
        AIExecutionService->>TextToFlashcardsStrategy: execute(request)

        TextToFlashcardsStrategy->>TextToFlashcardsStrategy: Build prompt with template<br/>FLASHCARD_GENERATION_TEMPLATE

        TextToFlashcardsStrategy->>ChatModel: call(prompt)<br/>with model, temperature, maxTokens

        alt Primary model succeeds
            ChatModel-->>TextToFlashcardsStrategy: Return JSON response
            TextToFlashcardsStrategy->>TextToFlashcardsStrategy: Parse JSON to List<CreateFlashcardDto>
            TextToFlashcardsStrategy-->>AIExecutionService: Return flashcards
            AIExecutionService-->>AIController: Return flashcards

            AIController->>FlashcardService: createMultipleFlashcards(flashcards, deckId)
            FlashcardService->>FlashcardDao: saveAll(flashcards)
            FlashcardDao->>MongoDB: Insert flashcard documents
            MongoDB-->>FlashcardDao: Return saved documents
            FlashcardDao-->>FlashcardService: Return FlashcardDto list
            FlashcardService-->>AIController: Return saved flashcards

            AIController-->>Frontend: 200 OK {flashcards}
            Frontend->>User: Display generated flashcards

        else Primary model fails
            ChatModel-->>TextToFlashcardsStrategy: Throw ServiceException

            TextToFlashcardsStrategy-->>AIExecutionService: Propagate exception
            AIExecutionService->>AIExecutionService: Catch exception<br/>Check fallback enabled<br/>Check retry count < maxRetries

            AIExecutionService->>AIExecutionService: Get next fallback model<br/>(GPT_4O_MINI or CLAUDE_3_5_HAIKU)

            AIExecutionService->>TextToFlashcardsStrategy: execute(request) with fallback model
            TextToFlashcardsStrategy->>FallbackModel: call(prompt)

            alt Fallback model succeeds
                FallbackModel-->>TextToFlashcardsStrategy: Return JSON response
                TextToFlashcardsStrategy->>TextToFlashcardsStrategy: Parse JSON
                TextToFlashcardsStrategy-->>AIExecutionService: Return flashcards
                AIExecutionService-->>AIController: Return flashcards

                Note over AIController: Continue with FlashcardService<br/>to save flashcards as above

            else All models fail
                FallbackModel-->>TextToFlashcardsStrategy: Throw exception
                TextToFlashcardsStrategy-->>AIExecutionService: Propagate exception
                AIExecutionService-->>AIController: Throw ServiceException<br/>"All AI models failed"
                AIController-->>Frontend: 500 Internal Server Error<br/>{errorCode: "SVC_015"}
                Frontend->>User: Show error message
            end
        end

    else Validation fails
        TextToFlashcardsStrategy-->>AIExecutionService: Throw ServiceException
        AIExecutionService-->>AIController: Propagate exception
        AIController-->>Frontend: 400 Bad Request<br/>{errorCode: "SVC_009"}
        Frontend->>User: Show validation error
    end
```

## 2. Content Summarization (Text/Image/Document)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIController
    participant AIExecutionService
    participant ContentToSummaryStrategy
    participant VisionService as AI Vision Service<br/>(for images)
    participant ChatModel as AI Chat Model
    participant MongoDB

    User->>Frontend: Upload content<br/>(text, image, or document)
    Frontend->>AIController: POST /api/ai/summarize<br/>{sourceType, text?, file?, format, length}

    AIController->>AIExecutionService: executeOperation(<br/>contentToSummaryStrategy,<br/>request, model)

    AIExecutionService->>ContentToSummaryStrategy: validateInput(request)
    ContentToSummaryStrategy->>ContentToSummaryStrategy: Check sourceType not null<br/>Check text length if TEXT<br/>Check file type if IMAGE/PDF

    alt Source is TEXT
        ContentToSummaryStrategy->>ContentToSummaryStrategy: Build prompt with<br/>SUMMARIZATION_TEMPLATE

        ContentToSummaryStrategy->>ChatModel: call(prompt)
        ChatModel-->>ContentToSummaryStrategy: Return summary text

    else Source is IMAGE
        ContentToSummaryStrategy->>VisionService: analyzeImage(file)
        VisionService->>VisionService: Convert to base64<br/>Build vision prompt
        VisionService->>ChatModel: call(multimodal prompt)
        ChatModel-->>VisionService: Return image description
        VisionService-->>ContentToSummaryStrategy: Return extracted text

        ContentToSummaryStrategy->>ContentToSummaryStrategy: Build summarization prompt
        ContentToSummaryStrategy->>ChatModel: call(prompt)
        ChatModel-->>ContentToSummaryStrategy: Return summary

    else Source is PDF
        ContentToSummaryStrategy->>ContentToSummaryStrategy: Extract text from PDF
        ContentToSummaryStrategy->>ContentToSummaryStrategy: Build summarization prompt
        ContentToSummaryStrategy->>ChatModel: call(prompt)
        ChatModel-->>ContentToSummaryStrategy: Return summary
    end

    ContentToSummaryStrategy->>ContentToSummaryStrategy: Format summary based on<br/>requested format (PARAGRAPH/BULLETS/STRUCTURED)

    ContentToSummaryStrategy-->>AIExecutionService: Return AISummaryResponseDto
    AIExecutionService-->>AIController: Return summary
    AIController-->>Frontend: 200 OK {summary, wordCount, format}
    Frontend->>User: Display formatted summary
```

## 3. Image-to-Flashcards (Vision AI)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIController
    participant AIVisionService
    participant AIExecutionService
    participant ImageToTextStrategy
    participant TextToFlashcardsStrategy
    participant VertexAIModel as Vertex AI Gemini<br/>(Vision Model)
    participant ChatModel as AI Chat Model
    participant FlashcardService
    participant MongoDB

    User->>Frontend: Upload diagram/chart/notes image
    Frontend->>AIController: POST /api/ai/generate-flashcards-from-image<br/>{file, deckId, count, model}

    AIController->>AIVisionService: generateFlashcardsFromImage(request)

    AIVisionService->>AIExecutionService: executeOperation(<br/>imageToTextStrategy, request)

    AIExecutionService->>ImageToTextStrategy: validateInput(request)
    ImageToTextStrategy->>ImageToTextStrategy: Check file type<br/>(JPEG, PNG, WebP)<br/>Check file size ≤ 10MB

    ImageToTextStrategy->>ImageToTextStrategy: Convert image to base64

    ImageToTextStrategy->>VertexAIModel: call(vision prompt)<br/>with image data

    VertexAIModel->>VertexAIModel: Analyze image content<br/>Extract text, diagrams, concepts

    VertexAIModel-->>ImageToTextStrategy: Return extracted text<br/>and visual descriptions

    ImageToTextStrategy-->>AIExecutionService: Return extracted content
    AIExecutionService-->>AIVisionService: Return text content

    AIVisionService->>AIExecutionService: executeOperation(<br/>textToFlashcardsStrategy,<br/>extracted text)

    AIExecutionService->>TextToFlashcardsStrategy: execute(request)
    TextToFlashcardsStrategy->>ChatModel: call(flashcard generation prompt)
    ChatModel-->>TextToFlashcardsStrategy: Return flashcard JSON
    TextToFlashcardsStrategy-->>AIExecutionService: Return List<CreateFlashcardDto>
    AIExecutionService-->>AIVisionService: Return flashcards

    AIVisionService->>FlashcardService: createMultipleFlashcards(flashcards, deckId)
    FlashcardService->>MongoDB: Save flashcards
    MongoDB-->>FlashcardService: Return saved flashcards

    FlashcardService-->>AIVisionService: Return FlashcardDto list
    AIVisionService-->>AIController: Return flashcards
    AIController-->>Frontend: 200 OK {flashcards}
    Frontend->>User: Display generated flashcards<br/>with image context
```

## 4. AI Prompt Optimization

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AIController
    participant AIExecutionService
    participant PromptOptimizationStrategy
    participant ChatModel

    User->>Frontend: Enter basic prompt<br/>"Explain dependency injection"
    Frontend->>AIController: POST /api/ai/optimize-prompt<br/>{originalPrompt, targetAudience, purpose}

    AIController->>AIExecutionService: executeOperation(<br/>promptOptimizationStrategy,<br/>request)

    AIExecutionService->>PromptOptimizationStrategy: execute(request)

    PromptOptimizationStrategy->>PromptOptimizationStrategy: Build meta-prompt:<br/>"Enhance this prompt for better AI responses:<br/>Original: {originalPrompt}<br/>Target: {targetAudience}<br/>Purpose: {purpose}"

    PromptOptimizationStrategy->>ChatModel: call(meta-prompt)

    ChatModel->>ChatModel: Analyze prompt<br/>Add context, structure<br/>Optimize for clarity

    ChatModel-->>PromptOptimizationStrategy: Return optimized prompt

    PromptOptimizationStrategy-->>AIExecutionService: Return AIPromptGenerateResponseDto
    AIExecutionService-->>AIController: Return response
    AIController-->>Frontend: 200 OK<br/>{optimizedPrompt, improvements}
    Frontend->>User: Show before/after comparison
```

## 5. AI Model Selection and Caching

```mermaid
sequenceDiagram
    participant AIController
    participant AIExecutionService
    participant CacheManager as Redis Cache
    participant ModelSelector as AI Model Selector
    participant ChatModel

    AIController->>AIExecutionService: executeOperation(strategy, request, model)

    AIExecutionService->>AIExecutionService: Generate cache key<br/>hash(model + prompt + params)

    AIExecutionService->>CacheManager: get(cacheKey)

    alt Cache hit
        CacheManager-->>AIExecutionService: Return cached response
        AIExecutionService-->>AIController: Return cached result
        Note over AIExecutionService: Cache TTL: 3600s (1 hour)

    else Cache miss
        CacheManager-->>AIExecutionService: null

        AIExecutionService->>ModelSelector: selectModel(modelEnum)

        alt Model is OpenAI
            ModelSelector-->>AIExecutionService: Return OpenAiChatModel
        else Model is Anthropic
            ModelSelector-->>AIExecutionService: Return AnthropicChatModel
        else Model is Vertex AI
            ModelSelector-->>AIExecutionService: Return VertexAiGeminiChat
        end

        AIExecutionService->>ChatModel: call(prompt, options)
        ChatModel-->>AIExecutionService: Return response

        AIExecutionService->>CacheManager: set(cacheKey, response, TTL)
        CacheManager-->>AIExecutionService: Cached

        AIExecutionService-->>AIController: Return result
    end
```

## Implementation Status

| Feature | Status | AI Models Used | Notes |
|---------|--------|----------------|-------|
| Text-to-Flashcards | ✅ Implemented | GPT-4o, GPT-4o-mini, Claude 3.5 Sonnet, Gemini 2.0 | Fallback working |
| Content Summarization | ✅ Implemented | GPT-4o-mini, Claude 3.5 Haiku | Supports TEXT source only |
| Image-to-Flashcards | ✅ Implemented | Vertex AI Gemini 2.0/2.5 Pro | Vision capabilities |
| Image Summarization | ⚠️ Partial | Vertex AI Gemini | Needs more testing |
| PDF Summarization | ❌ Not Implemented | - | Planned for MVP |
| Prompt Optimization | ✅ Implemented | GPT-4o-mini | Internal optimization |
| Response Caching | ⚠️ Configured | - | Redis configured but not enabled |

## AI Provider Configuration

### Current Configuration (application.yml)

1. **OpenAI**
   - Models: gpt-4o, gpt-4o-mini, gpt-3.5-turbo
   - Temperature: 0.7
   - Max Tokens: 2000
   - Timeout: 60s

2. **Anthropic (Claude)**
   - Models: claude-3-5-sonnet, claude-3-5-haiku
   - Temperature: 0.7
   - Max Tokens: 2000
   - Timeout: 60s

3. **Vertex AI (Google Gemini)**
   - Models: gemini-2.0-flash, gemini-2.5-pro, gemini-2.5-flash
   - Location: us-central1
   - Max Output Tokens: 2000
   - Temperature: 0.7

### Fallback Chain

Configured in application.yml:
```yaml
ai:
  fallback:
    enabled: true
    max-retries: 2
    fallback-models:
      - GPT_4O_MINI
      - CLAUDE_3_5_HAIKU
      - GEMINI_1_5_FLASH
```

## Code Quality Issues (From Review)

### Issues Identified in AIService and Strategies:

1. **Rate Limiting**
   - ⚠️ No rate limiting on AI endpoints
   - Can lead to cost overruns and API quota exhaustion
   - **Fix:** Implement Redis-based rate limiting (10 requests/minute per user)

2. **Prompt Templates**
   - ✅ Constants defined in AIConstants.java
   - ⚠️ Some hardcoded prompts in strategy classes
   - **Fix:** Extract all prompts to constants

3. **Error Handling**
   - ✅ Using ServiceException with ErrorCode enum
   - ⚠️ Generic error messages don't help debugging
   - **Fix:** Add more specific error codes for AI failures

4. **Validation**
   - ✅ Input validation in strategies
   - ⚠️ No output validation (e.g., checking if AI returned valid JSON)
   - **Fix:** Add response validation with retry on parse failure

5. **Monitoring**
   - ❌ No metrics or logging for AI usage
   - ❌ No cost tracking
   - **Fix:** Add request logging, token usage tracking, cost monitoring

6. **Testing**
   - ❌ No unit tests for strategies (<5% coverage)
   - ❌ No integration tests for AI endpoints
   - **Fix:** Achieve 80% coverage with mocked AI responses

## Security Considerations

1. **API Key Management**
   - ✅ Stored in environment variables
   - ✅ Not committed to git
   - ⚠️ Consider using secret management service (AWS Secrets Manager, Google Secret Manager)

2. **Input Sanitization**
   - ⚠️ Limited sanitization of user input
   - Risk: Prompt injection attacks
   - **Fix:** Add input sanitization, content filtering

3. **Output Filtering**
   - ❌ No content filtering on AI responses
   - Risk: Inappropriate content generation
   - **Fix:** Add content moderation layer

4. **Cost Controls**
   - ⚠️ No per-user spending limits
   - ⚠️ No monthly budget alerts
   - **Fix:** Implement usage quotas, budget monitoring

## Related Documentation

- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md)
- [Audio Features Sequence Diagrams](./Sequence-Diagrams-Audio-Features.md)
- [Code Improvements for AI Endpoints](./AI-Endpoints-Code-Improvements.md) (to be created)
- [Rate Limiting with Redis](./Rate-Limiting-Redis-Design.md) (to be created)