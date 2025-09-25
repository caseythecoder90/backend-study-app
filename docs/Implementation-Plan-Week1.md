# Week 1 Implementation Plan - Flashcard Application

## Overview
Complete authentication enhancement with recovery codes, add OAuth, implement rate limiting, caching, and AI features. All authentication details are documented in `Authentication-Complete-Guide.md`.

## Phase 1: Authentication Enhancement (Days 1-2)

### OAuth Integration Strategy
**Dependencies to add:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

**OAuth Providers:**
- Google OAuth2
- GitHub OAuth2
- Discord OAuth2

**New Components:**
- `OAuthService` - Handle OAuth flow and user mapping
- `OAuthUserInfo` interface with provider implementations
- `OAuth2SuccessHandler` - Custom success handler
- Update `User` model with OAuth fields

**Database Changes:**
- Add `oauthProvider`, `oauthId`, `recoveryCodeHashes` to User collection

### TOTP Recovery Codes Implementation
**Flow:**
1. During TOTP setup - generate 10 recovery codes
2. Hash and store codes in database
3. Show codes once to user with download option
4. Each code can be used only once
5. Regenerate codes option after login

**Components:**
- `RecoveryCodeService` - Generate, validate, invalidate codes
- New endpoints: `/auth/totp/recovery-codes`, `/auth/login/recovery`
- Update User model with recovery code hashes

## Phase 2: Rate Limiting (Day 2-3)

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

**Rate Limits:**
- Global: 1000 requests/hour per IP
- User: 500 requests/hour per authenticated user
- AI generation: 10 requests/hour
- Auth endpoints: 20 requests/hour

**Components:**
- `RateLimitingFilter`
- `RateLimitService`
- `RateLimitConfig`

## Phase 3: Caching (Day 3-4)

**Implementation:**
- L1: Caffeine for app-level cache
- L2: Redis for distributed cache
- Cache user profiles, public decks, flashcards

**TTL Strategy:**
- User profiles: 30 minutes
- Public decks: 2 hours
- Flashcard content: 1 hour
- AI results: 24 hours

## Phase 4: Core Features (Day 4-5)

**Enhancements:**
- Study session management
- Spaced repetition algorithm
- Advanced search with full-text
- Collaborative features

## Phase 5: AI Integration (Day 5-7)

**AI Providers:**
- OpenAI GPT-4 for text analysis
- Claude API as backup
- Local embeddings for privacy

**Features:**
- Text-to-flashcards conversion
- Code-to-flashcards generation
- Auto-difficulty assignment
- Queue-based processing

## Configuration Updates

```yaml
spring:
  cache:
    type: redis
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}

rate-limiting:
  global-limit: 1000
  user-limit: 500
  ai-limit: 10

ai:
  providers:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4-turbo
```

## Database Schema Updates

```javascript
// MongoDB indexes
db.users.createIndex({"oauthProvider": 1, "oauthId": 1})
db.flashcards.createIndex({"tags": 1})
db.flashcards.createIndex({"$text": {"front.text": 1, "back.text": 1}})
db.study_sessions.createIndex({"userId": 1, "studiedAt": -1})
db.rate_limits.createIndex({"identifier": 1, "window": 1})
```

## Timeline
- **Day 1-2**: OAuth + Recovery codes
- **Day 3**: Rate limiting
- **Day 4**: Caching
- **Day 5**: Core features
- **Day 6-7**: AI integration

## Testing Strategy
- Unit tests for all new services
- Integration tests for OAuth flow
- Performance tests for rate limiting
- End-to-end tests for complete user flows