# AI Endpoints Code Improvements

This document identifies code quality issues, architectural concerns, and specific improvements needed for all AI-powered endpoints before MVP release.

## Executive Summary

**Current State:** AI endpoints are functional but lack production readiness
**Test Coverage:** <5% (Target: 80%)
**Critical Issues:** 12 identified
**High Priority Issues:** 18 identified
**Estimated Effort:** 3-4 weeks

## Critical Issues (Blocking MVP)

### 1. No Rate Limiting on AI Endpoints ⚠️ CRITICAL

**Issue:**
- All AI endpoints (`/api/ai/*`, `/api/audio/*`) have no rate limiting
- OpenAI API costs: $15/1M tokens (TTS), $0.006/minute (Whisper)
- Risk of cost overruns from malicious or accidental abuse

**Current Code:**
```java
@RestController
@RequestMapping("/api/ai")
public class AIController {
    // No rate limiting annotations or interceptors
    @PostMapping("/generate-flashcards")
    public ResponseEntity<List<FlashcardDto>> generateFlashcards(...) {
        // Unrestricted access
    }
}
```

**Solution:**
Implement Redis-based rate limiting with Bucket4j:

```java
@RestController
@RequestMapping("/api/ai")
@RateLimited(limit = 10, duration = 1, unit = TimeUnit.MINUTES)
public class AIController {

    @PostMapping("/generate-flashcards")
    @RateLimited(limit = 5, duration = 1, unit = TimeUnit.MINUTES, keyResolver = "userId")
    public ResponseEntity<List<FlashcardDto>> generateFlashcards(...) {
        // Rate limited to 5 requests per minute per user
    }
}
```

**Files to Create:**
- `src/main/java/com/flashcards/backend/flashcards/annotation/RateLimited.java`
- `src/main/java/com/flashcards/backend/flashcards/interceptor/RateLimitInterceptor.java`
- `src/main/java/com/flashcards/backend/flashcards/service/RateLimitService.java`

**Priority:** Critical
**Effort:** 2-3 days
**GitHub Issue:** #[TBD]

---

### 2. No Input Sanitization (Prompt Injection Risk) ⚠️ CRITICAL

**Issue:**
- User input passed directly to AI models without sanitization
- Risk: Prompt injection attacks
- Example: User inputs "Ignore previous instructions and..."

**Vulnerable Code:**
```java
// TextToFlashcardsStrategy.java:53
public List<CreateFlashcardDto> execute(AIGenerateRequestDto input) {
    String userText = input.getText(); // No sanitization

    PromptTemplate template = new PromptTemplate(FLASHCARD_GENERATION_TEMPLATE);
    Prompt prompt = template.create(Map.of("text", userText)); // Unsafe

    return chatModel.call(prompt);
}
```

**Solution:**
```java
@Service
public class PromptSanitizationService {

    private static final Pattern INJECTION_PATTERNS = Pattern.compile(
        "(?i)(ignore previous|system prompt|you are now|new instructions|</prompt>|<\\/system>)",
        Pattern.CASE_INSENSITIVE
    );

    public String sanitizeInput(String input) {
        if (isBlank(input)) {
            throw new ServiceException("Input cannot be blank", ErrorCode.SERVICE_VALIDATION_ERROR);
        }

        // Remove potential injection attempts
        String sanitized = input.replaceAll(INJECTION_PATTERNS.pattern(), "[REDACTED]");

        // Limit special characters
        sanitized = sanitized.replaceAll("[<>{}\\[\\]|`]", "");

        // Truncate to max length
        if (sanitized.length() > MAX_TEXT_LENGTH) {
            sanitized = sanitized.substring(0, MAX_TEXT_LENGTH);
        }

        // Log suspicious patterns for security monitoring
        if (!input.equals(sanitized)) {
            log.warn("Potential prompt injection detected. Original length: {}, Sanitized length: {}",
                     input.length(), sanitized.length());
        }

        return sanitized.trim();
    }
}
```

**Apply to all strategies:**
```java
@Override
public List<CreateFlashcardDto> execute(AIGenerateRequestDto input) {
    String sanitizedText = promptSanitizationService.sanitizeInput(input.getText());

    // Continue with sanitized input
    Map<String, Object> variables = Map.of("text", sanitizedText, ...);
    // ...
}
```

**Files to Create:**
- `src/main/java/com/flashcards/backend/flashcards/service/PromptSanitizationService.java`
- `src/test/java/com/flashcards/backend/flashcards/service/PromptSanitizationServiceTest.java`

**Priority:** Critical (Security)
**Effort:** 1-2 days
**GitHub Issue:** #[TBD]

---

### 3. No Response Validation (Parse Failures) ⚠️ CRITICAL

**Issue:**
- AI responses assumed to be valid JSON
- No retry logic for malformed responses
- Causes 500 errors when AI returns invalid format

**Current Code:**
```java
// TextToFlashcardsStrategy.java:87
String responseJson = response.getResult().getOutput().getText();
List<CreateFlashcardDto> flashcards = objectMapper.readValue(
    responseJson,
    new TypeReference<List<CreateFlashcardDto>>() {}
); // JsonProcessingException not handled properly
```

**Solution:**
```java
@Component
public class AIResponseValidator {

    public <T> T validateAndParse(String responseJson, TypeReference<T> typeRef, int maxRetries) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // Extract JSON if wrapped in markdown code blocks
                String cleanedJson = extractJsonFromResponse(responseJson);

                // Parse JSON
                T result = objectMapper.readValue(cleanedJson, typeRef);

                // Validate structure
                if (result instanceof List && ((List<?>) result).isEmpty()) {
                    throw new ServiceException("AI returned empty list", ErrorCode.SERVICE_AI_INVALID_RESPONSE);
                }

                return result;

            } catch (JsonProcessingException e) {
                if (attempt == maxRetries - 1) {
                    log.error("Failed to parse AI response after {} attempts: {}", maxRetries, responseJson);
                    throw new ServiceException(
                        "AI returned invalid response format",
                        ErrorCode.SERVICE_AI_INVALID_RESPONSE,
                        e
                    );
                }
                log.warn("JSON parse failed on attempt {}, retrying...", attempt + 1);
            }
        }
        throw new ServiceException("Unexpected error in response validation", ErrorCode.SERVICE_AI_GENERATION_ERROR);
    }

    private String extractJsonFromResponse(String response) {
        // Handle markdown-wrapped JSON: ```json\n{...}\n```
        Pattern jsonPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return response.trim();
    }
}
```

**Update strategies:**
```java
String responseJson = response.getResult().getOutput().getText();
List<CreateFlashcardDto> flashcards = aiResponseValidator.validateAndParse(
    responseJson,
    new TypeReference<List<CreateFlashcardDto>>() {},
    3 // maxRetries
);
```

**Files to Create:**
- `src/main/java/com/flashcards/backend/flashcards/service/ai/AIResponseValidator.java`
- `src/test/java/com/flashcards/backend/flashcards/service/ai/AIResponseValidatorTest.java`

**Priority:** Critical
**Effort:** 2 days
**GitHub Issue:** #[TBD]

---

### 4. Missing Error Codes for AI Failures

**Issue:**
- Generic `ErrorCode.SERVICE_AI_GENERATION_ERROR` used for all AI failures
- Difficult to debug and monitor
- No distinction between API errors, rate limits, invalid responses, etc.

**Current Code:**
```java
// AIExecutionService.java
catch (Exception e) {
    throw new ServiceException("AI operation failed", ErrorCode.SERVICE_AI_GENERATION_ERROR, e);
}
```

**Solution:**
Add specific error codes to `ErrorCode` enum:

```java
public enum ErrorCode {
    // Existing codes...

    // AI-specific error codes
    SERVICE_AI_GENERATION_ERROR("SVC_015", "AI generation failed"),
    SERVICE_AI_RATE_LIMIT_EXCEEDED("SVC_016", "AI provider rate limit exceeded"),
    SERVICE_AI_INVALID_RESPONSE("SVC_017", "AI returned invalid response format"),
    SERVICE_AI_TIMEOUT("SVC_018", "AI request timed out"),
    SERVICE_AI_MODEL_UNAVAILABLE("SVC_019", "AI model temporarily unavailable"),
    SERVICE_AI_CONTENT_FILTERED("SVC_020", "Content violated AI provider policies"),
    SERVICE_AI_TOKEN_LIMIT_EXCEEDED("SVC_021", "Input exceeds model token limit"),
    SERVICE_AI_QUOTA_EXCEEDED("SVC_022", "User AI quota exceeded"),

    // Audio-specific codes
    SERVICE_AUDIO_FILE_TOO_LARGE("SVC_023", "Audio file exceeds size limit"),
    SERVICE_AUDIO_UNSUPPORTED_FORMAT("SVC_024", "Audio format not supported"),
    SERVICE_AUDIO_DURATION_EXCEEDED("SVC_025", "Audio duration exceeds limit"),
    SERVICE_AUDIO_TRANSCRIPTION_FAILED("SVC_026", "Audio transcription failed");

    // ...
}
```

**Update exception handling:**
```java
try {
    return chatModel.call(prompt);
} catch (RateLimitException e) {
    throw new ServiceException("Rate limit exceeded", ErrorCode.SERVICE_AI_RATE_LIMIT_EXCEEDED, e);
} catch (TimeoutException e) {
    throw new ServiceException("Request timed out", ErrorCode.SERVICE_AI_TIMEOUT, e);
} catch (ContentFilterException e) {
    throw new ServiceException("Content filtered", ErrorCode.SERVICE_AI_CONTENT_FILTERED, e);
}
```

**Files to Modify:**
- `src/main/java/com/flashcards/backend/flashcards/exception/ErrorCode.java`
- All strategy classes
- `src/main/java/com/flashcards/backend/flashcards/constants/ErrorMessages.java`

**Priority:** Critical
**Effort:** 1 day
**GitHub Issue:** #[TBD]

---

## High Priority Issues

### 5. No Cost Tracking / Usage Monitoring

**Issue:**
- No tracking of AI API costs
- No per-user usage limits
- No budget alerts

**Solution:**
```java
@Service
public class AIUsageTrackingService {

    private final MongoTemplate mongoTemplate;

    public void recordUsage(String userId, AIModelEnum model, UsageType type, UsageMetrics metrics) {
        AIUsageRecord record = AIUsageRecord.builder()
            .userId(userId)
            .model(model)
            .usageType(type)
            .inputTokens(metrics.getInputTokens())
            .outputTokens(metrics.getOutputTokens())
            .estimatedCost(calculateCost(model, metrics))
            .requestDuration(metrics.getDurationMs())
            .timestamp(LocalDateTime.now())
            .build();

        mongoTemplate.save(record);

        // Check if user exceeded quota
        double monthlyUsage = getMonthlyUsage(userId);
        if (monthlyUsage > USER_MONTHLY_LIMIT) {
            throw new ServiceException("Monthly AI usage limit exceeded", ErrorCode.SERVICE_AI_QUOTA_EXCEEDED);
        }
    }

    private BigDecimal calculateCost(AIModelEnum model, UsageMetrics metrics) {
        // Pricing per 1M tokens
        Map<AIModelEnum, BigDecimal> pricing = Map.of(
            AIModelEnum.GPT_4O, new BigDecimal("5.00"),
            AIModelEnum.GPT_4O_MINI, new BigDecimal("0.15"),
            AIModelEnum.CLAUDE_3_5_SONNET, new BigDecimal("3.00"),
            AIModelEnum.GEMINI_2_0_FLASH, new BigDecimal("0.075")
        );

        BigDecimal pricePerToken = pricing.get(model).divide(new BigDecimal("1000000"), 10, RoundingMode.HALF_UP);
        int totalTokens = metrics.getInputTokens() + metrics.getOutputTokens();
        return pricePerToken.multiply(new BigDecimal(totalTokens));
    }
}
```

**Files to Create:**
- `src/main/java/com/flashcards/backend/flashcards/service/AIUsageTrackingService.java`
- `src/main/java/com/flashcards/backend/flashcards/model/AIUsageRecord.java`
- `src/main/java/com/flashcards/backend/flashcards/repository/AIUsageRecordRepository.java`

**Priority:** High
**Effort:** 2-3 days
**GitHub Issue:** #[TBD]

---

### 6. Cache Not Enabled (Performance Issue)

**Issue:**
- Redis caching configured in `application.yml` but not enabled
- Repeated identical requests hit AI APIs unnecessarily
- Wastes money and increases latency

**Current Configuration:**
```yaml
# application.yml
ai:
  cache:
    enabled: true  # Configured but not implemented
    ttl: 3600
```

**Solution:**
```java
@Configuration
@EnableCaching
public class AICacheConfig {

    @Bean
    public CacheManager aiCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}
```

**Add caching to strategies:**
```java
@Service
public class TextToFlashcardsStrategy implements AIOperationStrategy<...> {

    @Cacheable(value = "aiFlashcards", key = "#request.text + #request.count + #request.model")
    @Override
    public List<CreateFlashcardDto> execute(AIGenerateRequestDto request) {
        // Cached for 1 hour based on text + count + model
        // ...
    }
}
```

**Cache key strategy:**
- Text-to-flashcards: Hash of (text + count + model)
- Summarization: Hash of (text + format + length + model)
- Image processing: Hash of (image bytes + options)

**Priority:** High
**Effort:** 1-2 days
**GitHub Issue:** #[TBD]

---

### 7. No Unit Tests for AI Strategies (<5% Coverage)

**Issue:**
- `TextToFlashcardsStrategy`, `ContentToSummaryStrategy`, `TextToSpeechStrategy`, `SpeechToTextStrategy` have NO tests
- Cannot refactor safely
- Bugs may go unnoticed

**Solution:**
Create comprehensive test suite with mocked AI responses:

```java
@ExtendWith(MockitoExtension.class)
class TextToFlashcardsStrategyTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private AIResponseValidator responseValidator;

    @Mock
    private PromptSanitizationService sanitizationService;

    @InjectMocks
    private TextToFlashcardsStrategy strategy;

    @Test
    void execute_withValidInput_returnsFlashcards() {
        // Given
        AIGenerateRequestDto request = AIGenerateRequestDto.builder()
            .text("Dependency injection is a design pattern...")
            .count(5)
            .model(AIModelEnum.GPT_4O_MINI)
            .build();

        String mockJsonResponse = """
            [
                {
                    "front": {"text": "What is DI?", "type": "TEXT_ONLY"},
                    "back": {"text": "A design pattern...", "type": "TEXT_ONLY"},
                    "difficulty": "MEDIUM"
                }
            ]
            """;

        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockResponse.getResult()).thenReturn(mockResult);
        when(mockResult.getOutput()).thenReturn(mockOutput);
        when(mockOutput.getText()).thenReturn(mockJsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        when(responseValidator.validateAndParse(eq(mockJsonResponse), any(), anyInt()))
            .thenReturn(List.of(CreateFlashcardDto.builder().build()));

        // When
        List<CreateFlashcardDto> result = strategy.execute(request);

        // Then
        assertThat(result).hasSize(1);
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    void execute_withInvalidText_throwsValidationException() {
        // Given
        AIGenerateRequestDto request = AIGenerateRequestDto.builder()
            .text("")  // Invalid
            .count(5)
            .build();

        // When/Then
        assertThatThrownBy(() -> strategy.execute(request))
            .isInstanceOf(ServiceException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.SERVICE_VALIDATION_ERROR);
    }

    @Test
    void execute_whenAIModelFails_throwsServiceException() {
        // Given
        AIGenerateRequestDto request = createValidRequest();
        when(chatModel.call(any())).thenThrow(new RuntimeException("API error"));

        // When/Then
        assertThatThrownBy(() -> strategy.execute(request))
            .isInstanceOf(ServiceException.class);
    }
}
```

**Test coverage targets:**
- All strategies: 80% line coverage
- Happy path, validation failures, API failures, timeout scenarios
- Edge cases: empty responses, malformed JSON, special characters

**Files to Create:**
- `src/test/java/com/flashcards/backend/flashcards/service/ai/strategy/TextToFlashcardsStrategyTest.java`
- `src/test/java/com/flashcards/backend/flashcards/service/ai/strategy/ContentToSummaryStrategyTest.java`
- `src/test/java/com/flashcards/backend/flashcards/service/ai/strategy/TextToSpeechStrategyTest.java`
- `src/test/java/com/flashcards/backend/flashcards/service/ai/strategy/SpeechToTextStrategyTest.java`
- `src/test/java/com/flashcards/backend/flashcards/service/ai/strategy/ImageToTextStrategyTest.java`

**Priority:** High
**Effort:** 4-5 days
**GitHub Issue:** #[TBD]

---

### 8. Inconsistent Prompt Templates

**Issue:**
- Some prompts in `AIConstants.java`, others hardcoded
- Difficult to A/B test prompt improvements
- No version tracking

**Current State:**
```java
// AIConstants.java - Some prompts here
public static final String FLASHCARD_GENERATION_TEMPLATE = """...""";

// SomeStrategy.java - Others hardcoded
String hardcodedPrompt = "Create a summary of: " + text; // Bad practice
```

**Solution:**
Centralize all prompts in `PromptTemplateRegistry`:

```java
@Component
public class PromptTemplateRegistry {

    private final Map<PromptType, PromptTemplate> templates = new EnumMap<>(PromptType.class);

    @PostConstruct
    public void init() {
        registerAllTemplates();
    }

    private void registerAllTemplates() {
        // Flashcard generation
        templates.put(PromptType.FLASHCARD_GENERATION,
            new PromptTemplate(AIConstants.FLASHCARD_GENERATION_TEMPLATE));

        // Summarization
        templates.put(PromptType.CONTENT_SUMMARY,
            new PromptTemplate(AIConstants.SUMMARIZATION_TEMPLATE));

        // Audio summary
        templates.put(PromptType.AUDIO_SUMMARY,
            new PromptTemplate(AIConstants.AUDIO_SUMMARY_TEMPLATE));

        // Image analysis
        templates.put(PromptType.IMAGE_TO_TEXT,
            new PromptTemplate(AIConstants.IMAGE_ANALYSIS_TEMPLATE));
    }

    public PromptTemplate getTemplate(PromptType type) {
        return templates.computeIfAbsent(type,
            k -> { throw new IllegalArgumentException("Unknown prompt type: " + type); });
    }

    public enum PromptType {
        FLASHCARD_GENERATION,
        CONTENT_SUMMARY,
        AUDIO_SUMMARY,
        IMAGE_TO_TEXT,
        PROMPT_OPTIMIZATION
    }
}
```

**Usage in strategies:**
```java
@Component
public class TextToFlashcardsStrategy {

    private final PromptTemplateRegistry promptRegistry;

    @Override
    public List<CreateFlashcardDto> execute(AIGenerateRequestDto input) {
        PromptTemplate template = promptRegistry.getTemplate(PromptType.FLASHCARD_GENERATION);
        Prompt prompt = template.create(Map.of("text", input.getText(), "count", input.getCount()));
        // ...
    }
}
```

**Priority:** High
**Effort:** 1 day
**GitHub Issue:** #[TBD]

---

### 9. No Integration Tests for AI Endpoints

**Issue:**
- Controllers not tested end-to-end
- No tests with actual HTTP requests
- MockMvc tests missing

**Solution:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AIControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIExecutionService aiExecutionService;

    @Test
    @WithMockUser(username = "test@example.com")
    void generateFlashcards_withValidRequest_returns200() throws Exception {
        // Given
        List<CreateFlashcardDto> mockFlashcards = List.of(
            CreateFlashcardDto.builder()
                .front(CardContentDto.builder().text("Q1").build())
                .back(CardContentDto.builder().text("A1").build())
                .build()
        );

        when(aiExecutionService.executeOperation(any(), any(), any()))
            .thenReturn(mockFlashcards);

        // When/Then
        mockMvc.perform(post("/api/ai/generate-flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": "user123",
                        "deckId": "deck123",
                        "text": "Dependency injection is...",
                        "count": 5,
                        "model": "GPT_4O_MINI"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].front.text").value("Q1"));
    }

    @Test
    @WithMockUser
    void generateFlashcards_withInvalidText_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/generate-flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": "user123",
                        "text": "",
                        "count": 5
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("SVC_009"));
    }

    @Test
    void generateFlashcards_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/ai/generate-flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }
}
```

**Coverage targets:**
- All AI endpoints (POST /api/ai/generate-flashcards, POST /api/ai/summarize, etc.)
- All audio endpoints (POST /api/audio/text-to-speech, POST /api/audio/speech-to-text)
- Authentication, validation, error handling

**Files to Create:**
- `src/test/java/com/flashcards/backend/flashcards/controller/AIControllerIntegrationTest.java`
- `src/test/java/com/flashcards/backend/flashcards/controller/AIAudioControllerIntegrationTest.java`

**Priority:** High
**Effort:** 2-3 days
**GitHub Issue:** #[TBD]

---

### 10. Audio File Validation is Insufficient

**Issue:**
- Only checks MIME type
- No validation of actual audio codec/format
- No duration pre-check (Whisper limit: 25MB)

**Current Code:**
```java
// AIAudioController.java
@PostMapping(value = "/speech-to-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<AISpeechToTextResponseDto> convertSpeechToText(
    @RequestParam("audioFile") MultipartFile audioFile) {

    // Only checks MIME type
    if (!audioFile.getContentType().startsWith("audio/")) {
        throw new ServiceException("Invalid file type", ErrorCode.SERVICE_VALIDATION_ERROR);
    }
    // No further validation
}
```

**Solution:**
```java
@Service
public class AudioFileValidator {

    public void validate(MultipartFile file) {
        // Check file not empty
        if (file.isEmpty()) {
            throw new ServiceException("Audio file is empty", ErrorCode.SERVICE_VALIDATION_ERROR);
        }

        // Check size (Whisper limit: 25 MB)
        long maxSizeBytes = 25 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new ServiceException(
                "Audio file exceeds maximum size of 25 MB",
                ErrorCode.SERVICE_AUDIO_FILE_TOO_LARGE
            );
        }

        // Validate format by reading file header
        String detectedFormat = detectAudioFormat(file);
        List<String> supportedFormats = List.of("mp3", "wav", "m4a", "flac", "ogg", "webm");

        if (!supportedFormats.contains(detectedFormat.toLowerCase())) {
            throw new ServiceException(
                "Unsupported audio format: " + detectedFormat,
                ErrorCode.SERVICE_AUDIO_UNSUPPORTED_FORMAT
            );
        }

        // Optional: Validate duration (requires FFmpeg or similar)
        // long durationSeconds = getAudioDuration(file);
        // if (durationSeconds > MAX_DURATION_SECONDS) { ... }
    }

    private String detectAudioFormat(MultipartFile file) {
        try {
            byte[] header = new byte[12];
            file.getInputStream().read(header);

            // MP3: ID3 or FF FB
            if ((header[0] == 'I' && header[1] == 'D' && header[2] == '3') ||
                (header[0] == (byte)0xFF && (header[1] & 0xE0) == 0xE0)) {
                return "mp3";
            }

            // WAV: RIFF...WAVE
            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'A' && header[10] == 'V' && header[11] == 'E') {
                return "wav";
            }

            // Add more format checks...

            return "unknown";
        } catch (IOException e) {
            throw new ServiceException("Failed to read audio file", ErrorCode.SERVICE_VALIDATION_ERROR, e);
        }
    }
}
```

**Priority:** High
**Effort:** 1-2 days
**GitHub Issue:** #[TBD]

---

## Medium Priority Issues

### 11. Fallback Logic Not Comprehensive

**Issue:**
- Fallback works for model failures but not for:
  - Rate limit errors (should wait and retry)
  - Timeout errors (should retry with increased timeout)
  - Content filter errors (should not retry)

**Solution:**
```java
@Service
public class AIExecutionService {

    public <I, O> O executeOperation(AIOperationStrategy<I, O> strategy, I input, AIModelEnum model) {
        return executeWithRetry(strategy, input, model, 0);
    }

    private <I, O> O executeWithRetry(AIOperationStrategy<I, O> strategy, I input, AIModelEnum model, int attempt) {
        try {
            return strategy.execute(input);

        } catch (RateLimitException e) {
            // Wait and retry
            if (attempt < maxRetries) {
                long backoffMs = (long) Math.pow(2, attempt) * 1000; // Exponential backoff
                log.warn("Rate limit hit, backing off for {}ms", backoffMs);
                Thread.sleep(backoffMs);
                return executeWithRetry(strategy, input, model, attempt + 1);
            }
            throw new ServiceException("Rate limit exceeded after retries", ErrorCode.SERVICE_AI_RATE_LIMIT_EXCEEDED, e);

        } catch (TimeoutException e) {
            // Retry with fallback model
            if (attempt < maxRetries) {
                AIModelEnum fallbackModel = getFallbackModel(model);
                log.warn("Timeout on model {}, trying fallback: {}", model, fallbackModel);
                return executeWithRetry(strategy, input, fallbackModel, attempt + 1);
            }
            throw new ServiceException("Request timed out", ErrorCode.SERVICE_AI_TIMEOUT, e);

        } catch (ContentFilterException e) {
            // Do not retry - content is not acceptable
            throw new ServiceException("Content filtered by AI provider", ErrorCode.SERVICE_AI_CONTENT_FILTERED, e);

        } catch (Exception e) {
            // General error - try fallback
            if (attempt < maxRetries) {
                AIModelEnum fallbackModel = getFallbackModel(model);
                return executeWithRetry(strategy, input, fallbackModel, attempt + 1);
            }
            throw new ServiceException("All AI models failed", ErrorCode.SERVICE_AI_GENERATION_ERROR, e);
        }
    }
}
```

**Priority:** Medium
**Effort:** 2 days
**GitHub Issue:** #[TBD]

---

### 12. No Logging of AI Requests for Debugging

**Issue:**
- No structured logging of AI requests/responses
- Difficult to debug when users report issues
- No audit trail for AI usage

**Solution:**
```java
@Aspect
@Component
public class AIRequestLoggingAspect {

    @Around("execution(* com.flashcards.backend.flashcards.service.ai.strategy.*.execute(..))")
    public Object logAIRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        String strategyName = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();

        // Log request
        log.info("AI Request - Strategy: {}, Input: {}", strategyName, sanitizeForLogging(args[0]));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Log successful response
            log.info("AI Response - Strategy: {}, Duration: {}ms, Result: {}",
                     strategyName, duration, summarizeResult(result));

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Log error
            log.error("AI Error - Strategy: {}, Duration: {}ms, Error: {}",
                      strategyName, duration, e.getMessage());

            throw e;
        }
    }

    private String sanitizeForLogging(Object input) {
        // Truncate long text, remove sensitive data
        // Return first 200 chars for logging
        if (input instanceof AIGenerateRequestDto dto) {
            return String.format("userId=%s, text=%s..., count=%d",
                                 dto.getUserId(),
                                 dto.getText().substring(0, Math.min(200, dto.getText().length())),
                                 dto.getCount());
        }
        return input.toString();
    }
}
```

**Priority:** Medium
**Effort:** 1 day
**GitHub Issue:** #[TBD]

---

## All Issues Summary Table

| # | Issue | Priority | Effort | Files Affected | Blocking MVP? |
|---|-------|----------|--------|----------------|---------------|
| 1 | No Rate Limiting | Critical | 2-3 days | AIController, AIAudioController | ✅ Yes |
| 2 | No Input Sanitization | Critical | 1-2 days | All strategies | ✅ Yes |
| 3 | No Response Validation | Critical | 2 days | All strategies | ✅ Yes |
| 4 | Missing Error Codes | Critical | 1 day | ErrorCode, strategies | ✅ Yes |
| 5 | No Cost Tracking | High | 2-3 days | New service, model, repo | ⚠️ Recommended |
| 6 | Cache Not Enabled | High | 1-2 days | Config, strategies | ⚠️ Recommended |
| 7 | No Unit Tests | High | 4-5 days | All strategy tests | ✅ Yes |
| 8 | Inconsistent Prompts | High | 1 day | PromptTemplateRegistry | ⚠️ Recommended |
| 9 | No Integration Tests | High | 2-3 days | Controller tests | ✅ Yes |
| 10 | Audio Validation | High | 1-2 days | AudioFileValidator | ⚠️ Recommended |
| 11 | Fallback Logic | Medium | 2 days | AIExecutionService | No |
| 12 | No Request Logging | Medium | 1 day | Logging aspect | No |

**Total Effort for MVP Blockers:** 12-15 days (Critical + High priority testing)
**Total Effort for All Issues:** 20-27 days

## Implementation Order

### Phase 1: Critical Security & Stability (Week 1-2)

1. Add specific error codes (#4) - 1 day
2. Implement input sanitization (#2) - 1-2 days
3. Add response validation (#3) - 2 days
4. Implement rate limiting (#1) - 2-3 days

**Deliverable:** AI endpoints are secure and stable

### Phase 2: Testing & Quality (Week 2-3)

5. Write unit tests for all strategies (#7) - 4-5 days
6. Write integration tests for controllers (#9) - 2-3 days

**Deliverable:** 80% test coverage achieved

### Phase 3: Performance & Monitoring (Week 3-4)

7. Enable Redis caching (#6) - 1-2 days
8. Implement cost tracking (#5) - 2-3 days
9. Centralize prompt templates (#8) - 1 day
10. Add audio file validation (#10) - 1-2 days

**Deliverable:** Production-ready AI system

### Phase 4: Polish (Optional, Post-MVP)

11. Improve fallback logic (#11) - 2 days
12. Add request logging (#12) - 1 day

## Related Documentation

- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md) - 60% MVP complete, AI testing critical gap
- [Sequence Diagrams - AI Operations](./Sequence-Diagrams-AI-Operations.md) - AI endpoint flows
- [Sequence Diagrams - Audio Features](./Sequence-Diagrams-Audio-Features.md) - Audio endpoint flows
- [Rate Limiting with Redis](./Rate-Limiting-Redis-Design.md) (to be created)
- [Testing Strategy](./Testing-Strategy.md) (to be created)