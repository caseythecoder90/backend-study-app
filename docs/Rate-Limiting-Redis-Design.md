# Rate Limiting with Redis - Design Document

This document outlines the design and implementation of Redis-based rate limiting for the Flashcards application using the Bucket4j library.

## Executive Summary

**Current State:** No rate limiting implemented
**Risk:** Cost overruns from AI API abuse, DDoS vulnerabilities
**Solution:** Redis-backed distributed rate limiting with Bucket4j
**Estimated Effort:** 2-3 days

## Why Rate Limiting is Critical

### 1. Cost Protection (AI APIs)
- OpenAI GPT-4o: $5.00 per 1M tokens
- OpenAI TTS: $15.00 per 1M characters
- OpenAI Whisper: $0.006 per minute
- **Without limits:** Single user could generate $1000+ in costs per day

### 2. Security (Brute Force Prevention)
- Login attempts: No limit = brute force attacks possible
- Password reset: No limit = email spam/DoS
- TOTP verification: No limit = 2FA bypass attempts

### 3. Fair Usage
- Prevent single user from monopolizing resources
- Ensure consistent performance for all users

## Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request
       ▼
┌──────────────────────────────────────┐
│       API Gateway/Controller         │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│     RateLimitInterceptor             │
│  (Pre-Controller Interceptor)        │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│      RateLimitService                │
│   (Business Logic Layer)             │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│      Bucket4j + Redis                │
│   (Distributed Token Bucket)         │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│         Redis Cluster                │
│  (Shared State Across Instances)     │
└──────────────────────────────────────┘
```

## Rate Limiting Strategies

### 1. Token Bucket Algorithm (Recommended)

**How it works:**
- Each user has a "bucket" with N tokens
- Each request consumes 1 token
- Tokens refill at rate R per time period
- If bucket empty, request is rejected

**Advantages:**
- Handles bursts gracefully
- Simple to reason about
- Industry standard (used by AWS, GitHub)

**Example:**
```
Limit: 10 requests per minute
- Bucket capacity: 10 tokens
- Refill rate: 10 tokens per 60 seconds
- User makes 10 requests in 5 seconds: ✅ All succeed (burst allowed)
- User makes 11th request: ❌ Rejected (bucket empty)
- After 6 seconds: 1 token refilled
- User makes request: ✅ Succeeds
```

### 2. Sliding Window Log (More Accurate, Higher Cost)

**How it works:**
- Store timestamp of each request in Redis sorted set
- Count requests in sliding window
- Remove old entries

**Advantages:**
- Most accurate
- No "edge" issues at time boundary

**Disadvantages:**
- Higher memory usage
- More complex

## Dependencies

### pom.xml

```xml
<dependencies>
    <!-- Bucket4j Core -->
    <dependency>
        <groupId>com.bucket4j</groupId>
        <artifactId>bucket4j-core</artifactId>
        <version>8.10.0</version>
    </dependency>

    <!-- Bucket4j Redis Integration -->
    <dependency>
        <groupId>com.bucket4j</groupId>
        <artifactId>bucket4j-redis</artifactId>
        <version>8.10.0</version>
    </dependency>

    <!-- Lettuce (Redis client, already included in Spring Boot) -->
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
    </dependency>

    <!-- Spring Data Redis (if not already included) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

## Configuration

### application.yml

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: -1ms

rate-limit:
  enabled: true
  default:
    capacity: 100               # Default bucket capacity
    refill-tokens: 100          # Tokens to refill
    refill-period: 60           # Refill period in seconds

  # Endpoint-specific limits
  endpoints:
    # Authentication endpoints (strict limits to prevent brute force)
    auth-login:
      capacity: 5
      refill-tokens: 5
      refill-period: 300        # 5 requests per 5 minutes
    auth-totp-verify:
      capacity: 3
      refill-tokens: 3
      refill-period: 300        # 3 requests per 5 minutes
    auth-password-reset:
      capacity: 3
      refill-tokens: 3
      refill-period: 3600       # 3 requests per hour

    # AI endpoints (expensive operations)
    ai-generate-flashcards:
      capacity: 10
      refill-tokens: 10
      refill-period: 60         # 10 requests per minute
    ai-summarize:
      capacity: 15
      refill-tokens: 15
      refill-period: 60         # 15 requests per minute
    ai-image-to-flashcards:
      capacity: 5
      refill-tokens: 5
      refill-period: 60         # 5 requests per minute (expensive)

    # Audio endpoints (very expensive)
    audio-text-to-speech:
      capacity: 10
      refill-tokens: 10
      refill-period: 60         # 10 requests per minute
    audio-speech-to-text:
      capacity: 5
      refill-tokens: 5
      refill-period: 60         # 5 requests per minute

    # Standard CRUD endpoints (generous limits)
    flashcard-create:
      capacity: 30
      refill-tokens: 30
      refill-period: 60         # 30 requests per minute
    deck-create:
      capacity: 20
      refill-tokens: 20
      refill-period: 60         # 20 requests per minute
```

## Implementation

### 1. Rate Limit Configuration Class

```java
package com.flashcards.backend.flashcards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private RateLimitConfig defaultConfig = new RateLimitConfig(100, 100, 60);

    private Map<String, RateLimitConfig> endpoints = new HashMap<>();

    @Data
    public static class RateLimitConfig {
        private int capacity;
        private int refillTokens;
        private int refillPeriod; // in seconds

        public RateLimitConfig() {}

        public RateLimitConfig(int capacity, int refillTokens, int refillPeriod) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriod = refillPeriod;
        }
    }
}
```

### 2. Redis Configuration for Bucket4j

```java
package com.flashcards.backend.flashcards.config;

import com.bucket4j.distributed.ExpirationAfterWriteStrategy;
import com.bucket4j.distributed.proxy.ProxyManager;
import com.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
public class Bucket4jRedisConfiguration {

    @Bean
    public ProxyManager<String> proxyManager(RedisConnectionFactory redisConnectionFactory) {
        // Get Redis connection URI from factory
        String redisUri = String.format("redis://%s:%d",
                redisConnectionFactory.getConnection().getNativeConnection().toString(),
                6379); // Default port, should be configured

        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(ByteArrayCodec.INSTANCE);

        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1))
                )
                .build();
    }
}
```

### 3. Rate Limit Service

```java
package com.flashcards.backend.flashcards.service;

import com.bucket4j.Bandwidth;
import com.bucket4j.Bucket;
import com.bucket4j.BucketConfiguration;
import com.bucket4j.distributed.proxy.ProxyManager;
import com.flashcards.backend.flashcards.config.RateLimitProperties;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.RATE_LIMIT_EXCEEDED;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties rateLimitProperties;

    /**
     * Check if request is allowed under rate limit.
     *
     * @param key Unique key for rate limiting (e.g., "userId:endpoint")
     * @param endpointName Name of endpoint for configuration lookup
     * @return true if allowed, throws ServiceException if rate limited
     */
    public boolean tryConsume(String key, String endpointName) {
        if (!rateLimitProperties.isEnabled()) {
            return true; // Rate limiting disabled
        }

        Bucket bucket = getBucket(key, endpointName);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            long availableTokens = bucket.getAvailableTokens();
            log.warn("Rate limit exceeded for key: {}, endpoint: {}, available tokens: {}",
                     key, endpointName, availableTokens);

            throw new ServiceException(
                    RATE_LIMIT_EXCEEDED.formatted(endpointName),
                    ErrorCode.RATE_LIMIT_EXCEEDED
            );
        }
    }

    /**
     * Get remaining tokens for a key.
     */
    public long getRemainingTokens(String key, String endpointName) {
        Bucket bucket = getBucket(key, endpointName);
        return bucket.getAvailableTokens();
    }

    /**
     * Get or create bucket for given key.
     */
    private Bucket getBucket(String key, String endpointName) {
        String fullKey = "rate-limit:" + key;

        Supplier<BucketConfiguration> configSupplier = () -> {
            RateLimitProperties.RateLimitConfig config = rateLimitProperties.getEndpoints()
                    .getOrDefault(endpointName, rateLimitProperties.getDefaultConfig());

            return BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(config.getCapacity())
                            .refillGreedy(config.getRefillTokens(), Duration.ofSeconds(config.getRefillPeriod()))
                            .build())
                    .build();
        };

        return proxyManager.builder().build(fullKey, configSupplier);
    }
}
```

### 4. Custom Annotation for Rate Limiting

```java
package com.flashcards.backend.flashcards.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable rate limiting on controller methods.
 *
 * Example:
 * @RateLimited(key = "userId", endpointName = "ai-generate-flashcards")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Key type for rate limiting.
     * Supported: "userId", "ipAddress", "sessionId", "custom"
     */
    String keyType() default "userId";

    /**
     * Endpoint name for configuration lookup.
     * Must match key in application.yml rate-limit.endpoints
     */
    String endpointName();

    /**
     * Optional: Custom key extractor bean name (for keyType = "custom")
     */
    String customKeyExtractor() default "";
}
```

### 5. Rate Limit Interceptor (AOP)

```java
package com.flashcards.backend.flashcards.interceptor;

import com.flashcards.backend.flashcards.annotation.RateLimited;
import com.flashcards.backend.flashcards.service.RateLimitService;
import com.flashcards.backend.flashcards.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RequestContextUtil requestContextUtil;

    @Around("@annotation(rateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String key = extractKey(rateLimited);
        String endpointName = rateLimited.endpointName();

        log.debug("Checking rate limit for key: {}, endpoint: {}", key, endpointName);

        // Check rate limit (throws ServiceException if exceeded)
        rateLimitService.tryConsume(key, endpointName);

        // Proceed with method execution
        return joinPoint.proceed();
    }

    private String extractKey(RateLimited rateLimited) {
        return switch (rateLimited.keyType()) {
            case "userId" -> {
                String userId = requestContextUtil.getCurrentUserId();
                yield userId != null ? userId : "anonymous";
            }
            case "ipAddress" -> {
                HttpServletRequest request = requestContextUtil.getCurrentRequest();
                yield request != null ? getClientIP(request) : "unknown";
            }
            case "sessionId" -> {
                HttpServletRequest request = requestContextUtil.getCurrentRequest();
                yield request != null ? request.getSession().getId() : "no-session";
            }
            default -> throw new IllegalArgumentException("Unsupported key type: " + rateLimited.keyType());
        };
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 6. Request Context Utility

```java
package com.flashcards.backend.flashcards.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestContextUtil {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Returns userId or email
        }
        return null;
    }

    public HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
```

### 7. Error Code Addition

```java
// Add to ErrorCode enum
public enum ErrorCode {
    // ... existing codes

    // Rate limiting
    RATE_LIMIT_EXCEEDED("SVC_027", "Rate limit exceeded for this operation"),

    // ...
}
```

### 8. Error Message Addition

```java
// Add to ErrorMessages.java
public class ErrorMessages {
    // ... existing messages

    // Rate limiting
    public static final String RATE_LIMIT_EXCEEDED =
            "Rate limit exceeded for %s. Please try again later.";

    // ...
}
```

## Usage in Controllers

### Example 1: AI Controller

```java
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIExecutionService aiExecutionService;
    private final FlashcardService flashcardService;

    @PostMapping("/generate-flashcards")
    @RateLimited(keyType = "userId", endpointName = "ai-generate-flashcards")
    @AIApiDocumentation.GenerateFlashcards
    public ResponseEntity<List<FlashcardDto>> generateFlashcards(
            @Valid @RequestBody AIGenerateRequestDto request) {

        // Rate limiting happens automatically via @RateLimited annotation
        List<CreateFlashcardDto> generatedFlashcards = aiExecutionService.executeOperation(
                textToFlashcardsStrategy, request, request.getModel());

        List<FlashcardDto> savedFlashcards = flashcardService.createMultipleFlashcards(generatedFlashcards);

        return ResponseEntity.ok(savedFlashcards);
    }

    @PostMapping("/summarize")
    @RateLimited(keyType = "userId", endpointName = "ai-summarize")
    @AIApiDocumentation.Summarize
    public ResponseEntity<AISummaryResponseDto> summarize(
            @Valid @RequestBody AISummaryRequestDto request) {

        AISummaryResponseDto summary = aiExecutionService.executeOperation(
                contentToSummaryStrategy, request, request.getModel());

        return ResponseEntity.ok(summary);
    }
}
```

### Example 2: Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @RateLimited(keyType = "ipAddress", endpointName = "auth-login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        // Rate limited by IP address to prevent brute force
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-totp")
    @RateLimited(keyType = "userId", endpointName = "auth-totp-verify")
    public ResponseEntity<AuthResponseDto> verifyTotp(@Valid @RequestBody TotpVerificationDto request) {
        // Rate limited by userId to prevent 2FA brute force
        AuthResponseDto response = authService.verifyTotp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @RateLimited(keyType = "ipAddress", endpointName = "auth-password-reset")
    public ResponseEntity<MessageDto> forgotPassword(@Valid @RequestBody ForgotPasswordDto request) {
        // Rate limited by IP to prevent email spam
        authService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(new MessageDto("Password reset email sent"));
    }
}
```

### Example 3: Audio Controller

```java
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AIAudioController {

    private final AIAudioService aiAudioService;

    @PostMapping("/text-to-speech")
    @RateLimited(keyType = "userId", endpointName = "audio-text-to-speech")
    @AIAudioApiDocumentation.TextToSpeech
    public ResponseEntity<AITextToSpeechResponseDto> convertTextToSpeech(
            @Valid @RequestBody AITextToSpeechRequestDto request) {

        AITextToSpeechResponseDto response = aiAudioService.convertTextToSpeech(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/speech-to-text")
    @RateLimited(keyType = "userId", endpointName = "audio-speech-to-text")
    @AIAudioApiDocumentation.SpeechToText
    public ResponseEntity<AISpeechToTextResponseDto> convertSpeechToText(
            @RequestParam("userId") String userId,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam(value = "action", required = false) String action) {

        // Rate limited per user for expensive audio transcription
        AISpeechToTextRequestDto request = AISpeechToTextRequestDto.builder()
                .userId(userId)
                .audioFile(audioFile)
                .action(action)
                .build();

        AISpeechToTextResponseDto response = aiAudioService.convertSpeechToText(request);
        return ResponseEntity.ok(response);
    }
}
```

## Error Handling

### GlobalExceptionHandler Addition

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ... existing exception handlers

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex, HttpServletRequest request) {

        // Special handling for rate limit errors
        if (ex.getErrorCode() == ErrorCode.RATE_LIMIT_EXCEEDED) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .errorCode(ex.getErrorCode().getCode())
                    .build();

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Limit", "10") // Could be dynamic
                    .header("X-RateLimit-Remaining", "0")
                    .header("Retry-After", "60") // Seconds
                    .body(errorResponse);
        }

        // ... existing service exception handling
    }
}
```

### Response Headers

Rate limit responses include helpful headers:
```
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704067200
Retry-After: 60
Content-Type: application/json

{
  "timestamp": "2024-01-15T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded for ai-generate-flashcards. Please try again later.",
  "path": "/api/ai/generate-flashcards",
  "errorCode": "SVC_027"
}
```

## Monitoring and Observability

### 1. Metrics Collection

```java
@Component
@RequiredArgsConstructor
public class RateLimitMetrics {

    private final MeterRegistry meterRegistry;

    public void recordRateLimitHit(String endpoint, String userId) {
        meterRegistry.counter("rate_limit.hit",
                "endpoint", endpoint,
                "userId", userId
        ).increment();
    }

    public void recordRateLimitExceeded(String endpoint, String userId) {
        meterRegistry.counter("rate_limit.exceeded",
                "endpoint", endpoint,
                "userId", userId
        ).increment();
    }

    public void recordRemainingTokens(String endpoint, long tokens) {
        meterRegistry.gauge("rate_limit.remaining_tokens",
                Tags.of("endpoint", endpoint),
                tokens);
    }
}
```

### 2. Logging

```java
// In RateLimitService
@Slf4j
@Service
public class RateLimitService {

    public boolean tryConsume(String key, String endpointName) {
        // ... existing code

        if (bucket.tryConsume(1)) {
            long remaining = bucket.getAvailableTokens();
            log.debug("Rate limit check passed. Key: {}, Endpoint: {}, Remaining: {}",
                     key, endpointName, remaining);

            // Alert if getting close to limit
            if (remaining < 2) {
                log.warn("User {} approaching rate limit for {}, only {} tokens remaining",
                         key, endpointName, remaining);
            }

            return true;
        } else {
            // ... existing error handling
        }
    }
}
```

### 3. Admin Endpoint for Monitoring

```java
@RestController
@RequestMapping("/api/admin/rate-limits")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RateLimitAdminController {

    private final RateLimitService rateLimitService;
    private final ProxyManager<String> proxyManager;

    @GetMapping("/user/{userId}/endpoint/{endpointName}")
    public ResponseEntity<RateLimitStatusDto> getUserRateLimit(
            @PathVariable String userId,
            @PathVariable String endpointName) {

        String key = userId + ":" + endpointName;
        long remaining = rateLimitService.getRemainingTokens(key, endpointName);

        RateLimitStatusDto status = RateLimitStatusDto.builder()
                .userId(userId)
                .endpoint(endpointName)
                .remainingTokens(remaining)
                .limitReached(remaining == 0)
                .build();

        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/user/{userId}/endpoint/{endpointName}")
    public ResponseEntity<Void> resetUserRateLimit(
            @PathVariable String userId,
            @PathVariable String endpointName) {

        String fullKey = "rate-limit:" + userId + ":" + endpointName;
        // Reset bucket (remove from Redis)
        proxyManager.removeProxy(fullKey);

        return ResponseEntity.noContent().build();
    }
}
```

## Testing

### 1. Unit Test for RateLimitService

```java
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private ProxyManager<String> proxyManager;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    @DisplayName("tryConsume - when tokens available - returns true")
    void tryConsume_whenTokensAvailable_returnsTrue() {
        // Given
        Bucket mockBucket = mock(Bucket.class);
        when(mockBucket.tryConsume(1)).thenReturn(true);
        when(proxyManager.builder()).thenReturn(mockBucketBuilder);
        // ... mock setup

        // When
        boolean result = rateLimitService.tryConsume("user-123", "test-endpoint");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("tryConsume - when no tokens - throws ServiceException")
    void tryConsume_whenNoTokens_throwsServiceException() {
        // Given
        Bucket mockBucket = mock(Bucket.class);
        when(mockBucket.tryConsume(1)).thenReturn(false);
        // ... mock setup

        // When/Then
        assertThatThrownBy(() -> rateLimitService.tryConsume("user-123", "test-endpoint"))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
    }
}
```

### 2. Integration Test with Redis

```java
@SpringBootTest
@Testcontainers
class RateLimitIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RateLimitService rateLimitService;

    @Test
    @DisplayName("Rate limit - enforces limit after configured requests")
    void rateLimit_enforcesLimitAfterConfiguredRequests() {
        String key = "test-user-" + UUID.randomUUID();
        String endpoint = "test-endpoint";

        // Configure endpoint with 5 requests limit
        // Assume configured in application-test.yml

        // Should succeed 5 times
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimitService.tryConsume(key, endpoint)).isTrue();
        }

        // 6th request should fail
        assertThatThrownBy(() -> rateLimitService.tryConsume(key, endpoint))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("Rate limit - refills tokens after time period")
    void rateLimit_refillsTokensAfterTimePeriod() throws InterruptedException {
        String key = "test-user-refill";
        String endpoint = "test-endpoint";

        // Exhaust tokens
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryConsume(key, endpoint);
        }

        // Should fail immediately
        assertThatThrownBy(() -> rateLimitService.tryConsume(key, endpoint))
                .isInstanceOf(ServiceException.class);

        // Wait for refill period (assume 1 second for test)
        Thread.sleep(1100);

        // Should succeed after refill
        assertThat(rateLimitService.tryConsume(key, endpoint)).isTrue();
    }
}
```

## Deployment Considerations

### 1. Redis High Availability

For production, use Redis Cluster or Redis Sentinel:

```yaml
spring:
  data:
    redis:
      cluster:
        nodes:
          - redis-node-1:6379
          - redis-node-2:6379
          - redis-node-3:6379
        max-redirects: 3
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: true
```

### 2. Railway Deployment

Railway supports Redis via addons:

```bash
# Add Redis to Railway project
railway add redis

# Environment variables automatically set:
# REDIS_URL=redis://...
```

Update `application.yml`:
```yaml
spring:
  data:
    redis:
      url: ${REDIS_URL}
```

### 3. Performance Tuning

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20    # Max connections
          max-idle: 10      # Idle connections
          min-idle: 5       # Minimum idle
```

## Rate Limit Strategy by Endpoint Category

| Category | Limit | Rationale |
|----------|-------|-----------|
| **Auth (Login/TOTP)** | 5/5min per IP | Brute force prevention |
| **Password Reset** | 3/hour per IP | Email spam prevention |
| **AI Flashcard Gen** | 10/min per user | Cost control (~$0.50/request) |
| **AI Summarization** | 15/min per user | Cheaper operation |
| **Image to Flashcards** | 5/min per user | Expensive vision model |
| **Audio TTS** | 10/min per user | $15 per 1M chars |
| **Audio STT** | 5/min per user | $0.006 per minute |
| **CRUD Flashcards** | 30/min per user | Generous for normal usage |
| **CRUD Decks** | 20/min per user | Less frequent operation |

## Security Considerations

1. **Key Expiration:** Buckets expire after 1 hour of inactivity
2. **Distributed:** Works across multiple server instances
3. **No Client Bypass:** Server-side enforcement
4. **Admin Override:** Admins can reset limits if needed

## Related Documentation

- [AI Endpoints Code Improvements](./AI-Endpoints-Code-Improvements.md) - Rate limiting is Issue #1
- [Security Measures Review](./Security-Measures-Review.md) (to be created)
- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md)