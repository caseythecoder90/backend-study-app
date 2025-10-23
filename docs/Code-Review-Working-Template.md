# Code Review & Cleanup - Working Template

This is your step-by-step guide to complete Issue #6. Use this as a checklist and update as you go.

## 🔄 Iterative Workflow (Recommended)

Instead of following the day-by-day plan strictly, you can use this iterative approach:

**For Each Component (Controller/Service/DAO):**
1. **Review** - Read the code, note issues
2. **Manual Test** - Test the endpoints with Postman
3. **Refactor** - Fix issues found in review and testing
4. **Write Unit Tests** - Test the component you just fixed
5. **Verify** - Run tests, check coverage
6. **Commit** - Commit your changes with clear message

This way you feel confident about each component before moving to the next.

## 📊 Component Progress Tracker

Track as you go (replace ⬜ with ✅ as you complete):

| Component | Reviewed | Tested | Refactored | Unit Tests | Coverage |
|-----------|----------|--------|------------|------------|----------|
| AuthController | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| AuthService | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| UserController | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| UserService | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| DeckController | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| DeckService | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| FlashcardController | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| FlashcardService | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| AIController | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| AIExecutionService | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| AIAudioController | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| AIAudioService | ⬜ | ⬜ | ⬜ | ⬜ | _% |
| TotpService | ⬜ | ⬜ | ⬜ | ⬜ | _% |

## 🚀 Component-by-Component Workflow (Best for Iterative Approach)

### Phase 1: Setup & Environment (30 mins)
```bash
# Verify everything runs
./mvnw clean install
docker-compose up -d
./mvnw spring-boot:run

# Set up Postman (import collection provided below)
# Create environment with base URL: http://localhost:8080
```

### Phase 2: Iterative Component Review

**Cycle 1: Authentication (AuthController + AuthService + UserDao)**
1. **Review**: Read AuthController.java, AuthService.java, UserDao.java
   - Check against CLAUDE.md standards (see checklist in Day 2-3 sections)
   - Note issues in progress tracker
2. **Manual Test** (Postman):
   - POST /api/auth/register
   - POST /api/auth/login
   - POST /api/auth/setup-totp
   - POST /api/auth/verify-totp
3. **Refactor**: Fix issues found during review and testing
4. **Write Tests**: Create AuthServiceTest.java (8-10 tests minimum)
5. **Verify**: Run `./mvnw test -Dtest=AuthServiceTest`
6. **Commit**: `git commit -m "refactor: review and test AuthService"`

**Cycle 2: User Management (UserController + UserService + UserDao)**
1. Review code against standards
2. Manual test user endpoints (if any)
3. Fix issues
4. Write UserServiceTest.java (5-8 tests)
5. Run tests
6. Commit

**Cycle 3: Deck Management (DeckController + DeckService + DeckDao)**
1. Review code
2. Manual test:
   - POST /api/decks (create)
   - GET /api/decks (list)
   - GET /api/decks/{id} (get one)
   - PUT /api/decks/{id} (update)
   - DELETE /api/decks/{id} (delete)
3. Fix issues
4. Write DeckServiceTest.java (8-10 tests)
5. Run tests
6. Commit

**Cycle 4: Flashcard Management (FlashcardController + FlashcardService + FlashcardDao)**
1. Review code (PRIORITY: most important feature)
2. Manual test:
   - POST /api/flashcards
   - GET /api/decks/{deckId}/flashcards
   - GET /api/flashcards/{id}
   - PUT /api/flashcards/{id}
   - DELETE /api/flashcards/{id}
3. Fix issues
4. Write FlashcardServiceTest.java (10-15 tests - comprehensive!)
5. Run tests
6. Commit

**Cycle 5: AI Features (AIController + AIExecutionService + Strategies)**
1. Review AIController.java, AIExecutionService.java
2. Review strategy implementations (GPT, Claude, Gemini)
3. Manual test:
   - POST /api/ai/generate-flashcards (with GPT_4O_MINI)
   - POST /api/ai/summarize
   - Test fallback behavior (if primary fails)
4. Fix validation and error handling issues
5. Write AIExecutionServiceTest.java (6-8 tests)
6. Run tests
7. Commit

**Cycle 6: Audio Features (AIAudioController + AIAudioService + Audio Strategies)**
1. Review AIAudioController.java, AIAudioService.java
2. **Prepare test audio files first**:
   - Download/create sample.mp3 and sample.wav
   - Place in src/test/resources/audio/
3. Manual test:
   - POST /api/audio/text-to-speech
   - POST /api/audio/text-to-speech/stream
   - POST /api/audio/speech-to-text (upload MP3)
   - Test with different voice options
4. Fix issues (especially file validation)
5. Write AIAudioServiceTest.java (6-8 tests)
6. Run tests
7. Commit

**Cycle 7: TOTP/2FA (TotpService)**
1. Review TotpService.java
2. Manual test (already tested in Cycle 1 but verify separately):
   - Setup TOTP
   - Verify code
   - Test with wrong code
3. Fix issues
4. Write TotpServiceTest.java (5-6 tests)
5. Run tests
6. Commit

### Phase 3: Final Verification (1 hour)
```bash
# Run ALL tests
./mvnw clean test

# Generate coverage report
./mvnw jacoco:report
open target/site/jacoco/index.html

# Verify 40%+ coverage achieved
# Fix any failing tests
```

### Phase 4: Documentation (30 mins)
- Create .env.example (template in Day 5 section)
- Export Postman collection
- Update README with findings
- Document bugs in GitHub issue

### Phase 5: Create Pull Request
```bash
git push origin chore/code-review-and-cleanup
# Create PR on GitHub
```

---

## 📅 Day-by-Day Plan (Alternative Approach)

If you prefer a time-boxed approach instead of component-by-component, follow this schedule:

### Day 1: Setup & Manual Testing (3-4 hours)

#### Morning: Environment Setup
- [ ] Create branch: `git checkout -b chore/code-review-and-cleanup`
- [ ] Ensure all dependencies are installed: `./mvnw clean install`
- [ ] Verify MongoDB is running: `docker-compose up -d mongodb`
- [ ] Verify Redis is running (for future rate limiting)
- [ ] Test application starts: `./mvnw spring-boot:run`
- [ ] Import Postman collection (we'll create this)

#### Afternoon: Manual Testing Session

**Authentication Testing:**
```
Test 1: Register User
POST http://localhost:8080/api/auth/register
Body: {
  "email": "test@example.com",
  "password": "Test123!@#",
  "username": "testuser",
  "firstName": "Test",
  "lastName": "User"
}
Expected: 201 Created
Result: _______________
Issues: _______________

Test 2: Login
POST http://localhost:8080/api/auth/login
Body: {
  "email": "test@example.com",
  "password": "Test123!@#"
}
Expected: 200 OK with JWT token
Result: _______________
Token: _______________

Test 3: TOTP Setup
POST http://localhost:8080/api/auth/setup-totp
Headers: Authorization: Bearer {token}
Body: { "userId": "..." }
Expected: 200 OK with QR code
Result: _______________

Test 4: TOTP Verify
POST http://localhost:8080/api/auth/verify-totp
Body: {
  "userId": "...",
  "totpCode": "123456"
}
Expected: 200 OK
Result: _______________
```

**Flashcard Testing:**
```
Test 5: Create Deck
POST http://localhost:8080/api/decks
Headers: Authorization: Bearer {token}
Body: {
  "title": "Test Deck",
  "description": "My test deck",
  "category": "Testing",
  "userId": "..."
}
Expected: 201 Created
Result: _______________
DeckId: _______________

Test 6: Create Flashcard
POST http://localhost:8080/api/flashcards
Body: {
  "deckId": "...",
  "userId": "...",
  "front": {
    "text": "What is Spring Boot?",
    "type": "TEXT_ONLY"
  },
  "back": {
    "text": "A Java framework",
    "type": "TEXT_ONLY"
  },
  "difficulty": "MEDIUM"
}
Expected: 201 Created
Result: _______________

Test 7: Get Flashcards by Deck
GET http://localhost:8080/api/decks/{deckId}/flashcards
Expected: 200 OK with array
Result: _______________
```

**AI Testing:**
```
Test 8: Generate Flashcards (AI)
POST http://localhost:8080/api/ai/generate-flashcards
Body: {
  "userId": "...",
  "deckId": "...",
  "text": "Spring Boot is a framework that makes it easy to create Java applications. It provides auto-configuration and embedded servers.",
  "count": 3,
  "model": "GPT_4O_MINI"
}
Expected: 200 OK with flashcards
Result: _______________
Time: _______________
Cost estimate: $_______________

Test 9: Summarize Text
POST http://localhost:8080/api/ai/summarize
Body: {
  "userId": "...",
  "sourceType": "TEXT",
  "text": "Long text here...",
  "format": "PARAGRAPH",
  "length": "MEDIUM",
  "model": "GPT_4O_MINI"
}
Expected: 200 OK
Result: _______________
```

**Audio Testing:**
```
Test 10: Text to Speech
POST http://localhost:8080/api/audio/text-to-speech
Body: {
  "userId": "...",
  "text": "This is a test of the text to speech system.",
  "outputType": "RECITATION",
  "voice": "ALLOY",
  "speed": 1.0
}
Expected: 200 OK with base64 audio
Result: _______________

Test 11: Text to Speech (Streaming)
POST http://localhost:8080/api/audio/text-to-speech/stream
Body: (same as above)
Expected: 200 OK with binary audio
Result: _______________

Test 12: Speech to Text
POST http://localhost:8080/api/audio/speech-to-text
(multipart/form-data)
- userId: "..."
- audioFile: (upload MP3 file)
- action: "TRANSCRIPTION_ONLY"
Expected: 200 OK with transcribed text
Result: _______________

Test 13: Speech to Text with Flashcards
(Same endpoint, action: "FLASHCARDS", deckId: "...", flashcardCount: 5)
Expected: 200 OK with flashcards
Result: _______________
```

**End of Day 1 Deliverable:**
- [ ] Completed manual testing checklist
- [ ] Documented all bugs found in issue comments
- [ ] Created list of API endpoints that work vs don't work

---

### Day 2: Code Review - Controllers (3-4 hours)

#### Review Checklist for Each Controller

**Controllers to Review:**
1. AuthController
2. UserController
3. DeckController
4. FlashcardController
5. AIController
6. AIAudioController

**For Each Controller, Check:**

```java
// ✅ GOOD EXAMPLE - AuthController
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // ✅ Final fields

    @PostMapping("/login")
    @ApiDocumentation.Login // ✅ Custom documentation annotation
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) { // ✅ @Valid

        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response); // ✅ Proper response
    }
}
```

**Check These Items:**

**1. Class Level:**
- [ ] Has `@RestController` annotation
- [ ] Has `@RequestMapping` with base path
- [ ] Has `@RequiredArgsConstructor` or proper constructor injection
- [ ] No `@CrossOrigin(origins = "*")` (security issue)

**2. Dependencies:**
- [ ] All fields are `private final`
- [ ] Using constructor injection (not `@Autowired` on fields)
- [ ] No circular dependencies

**3. Method Level:**
- [ ] Proper HTTP method annotations (@GetMapping, @PostMapping, etc.)
- [ ] Has documentation annotation or Swagger annotations
- [ ] Uses `@Valid` on request bodies
- [ ] Returns `ResponseEntity<?>` not raw types

**4. Error Handling:**
- [ ] No try-catch in controllers (let GlobalExceptionHandler handle it)
- [ ] Proper HTTP status codes (201 for creation, 204 for delete, etc.)

**5. API Path Standards:**
- [ ] Uses RESTful conventions (nouns, not verbs)
- [ ] Consistent naming (/api/{resource}/{id})
- [ ] No API paths in constants (define in @RequestMapping)

**Record Your Findings:**

**AuthController Review:**
```
✅ Good:
- Proper annotations
- Uses @Valid on all request bodies
- Clean structure

⚠️ Issues:
- Line 45: Missing @Valid on TotpVerificationDto
- Line 78: Should return 201 for registration, not 200

🔧 Fixes Applied:
- Added @Valid annotation
- Changed to ResponseEntity.status(HttpStatus.CREATED)
```

**AIController Review:**
```
✅ Good:
- Well organized
- Good error handling

⚠️ Issues:
- (Your findings here)

🔧 Fixes Applied:
- (Your fixes here)
```

**Continue for all controllers...**

---

### Day 3: Code Review - Services & DAOs (3-4 hours)

#### Service Layer Review

**Services to Review:**
1. AuthService
2. UserService
3. FlashcardService
4. DeckService
5. TotpService
6. AIExecutionService
7. AIAudioService

**For Each Service, Check:**

```java
// ✅ GOOD EXAMPLE
@Service
@RequiredArgsConstructor
@Slf4j // ✅ Logging
public class FlashcardService {

    private final FlashcardDao flashcardDao; // ✅ DAO dependency
    private final FlashcardMapper flashcardMapper; // ✅ Mapper

    public FlashcardDto createFlashcard(CreateFlashcardDto request) {
        // ✅ Input validation
        if (isBlank(request.getText())) {
            throw new ServiceException(
                "Text is required",
                ErrorCode.SERVICE_VALIDATION_ERROR
            );
        }

        // ✅ Business logic
        Flashcard flashcard = flashcardMapper.toEntity(request);

        try {
            Flashcard saved = flashcardDao.save(flashcard);
            return flashcardMapper.toDto(saved);
        } catch (DaoException e) {
            // ✅ Wrap DAO exceptions
            throw new ServiceException(
                "Failed to create flashcard",
                ErrorCode.SERVICE_FLASHCARD_CREATE_ERROR,
                e
            );
        }
    }
}
```

**Check These Items:**

**1. Class Structure:**
- [ ] Has `@Service` annotation
- [ ] Has `@Slf4j` for logging
- [ ] Has `@RequiredArgsConstructor`
- [ ] Only depends on DAOs and other services (not repositories directly)

**2. Error Handling:**
- [ ] Catches `DaoException` and wraps in `ServiceException`
- [ ] Uses specific error codes (not generic `SERVICE_ERROR`)
- [ ] Logs errors appropriately

**3. Validation:**
- [ ] Uses `StringUtils.isBlank()` not `str == null || str.isEmpty()`
- [ ] Uses `Objects.nonNull()` not `obj != null`
- [ ] Uses `CollectionUtils.isNotEmpty()` for collections
- [ ] Business logic validation in service, not just DTO validation

**4. Business Logic:**
- [ ] Service contains business rules
- [ ] No direct database queries (uses DAO)
- [ ] Transactional methods have `@Transactional`

**5. Logging:**
- [ ] Uses appropriate log levels (debug, info, warn, error)
- [ ] Logs method entry for complex operations
- [ ] Logs errors with exception
- [ ] No `System.out.println()`

**Record Your Findings:**

**FlashcardService Review:**
```
✅ Good:
- Proper error handling
- Uses DAO layer correctly

⚠️ Issues:
- Line 67: Direct null check instead of Objects.isNull()
- Line 89: Missing logging for delete operation
- Line 102: Generic error message

🔧 Fixes Applied:
- Changed to Objects.isNull(flashcard)
- Added log.info("Deleting flashcard: {}", id)
- Updated error message to be specific
```

#### DAO Layer Review

**DAOs to Review:**
1. FlashcardDao
2. DeckDao
3. UserDao
4. StudySessionDao

**For Each DAO, Check:**

```java
// ✅ GOOD EXAMPLE
@Repository
@RequiredArgsConstructor
public class FlashcardDao {

    private final FlashcardRepository flashcardRepository;
    private final MongoTemplate mongoTemplate;

    public Flashcard save(Flashcard flashcard) {
        return executeWithExceptionHandling(
            () -> flashcardRepository.save(flashcard),
            "save",
            ENTITY_FLASHCARD
        );
    }

    // ✅ Functional exception handling
    private <T> T executeWithExceptionHandling(
            Supplier<T> operation,
            String operationName,
            String entityName) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("{} operation failed for {}: {}",
                      operationName, entityName, e.getMessage());
            throw new DaoException(
                DAO_OPERATION_FAILED.formatted(operationName, entityName),
                ErrorCode.DAO_OPERATION_ERROR,
                e
            );
        }
    }
}
```

**Check These Items:**

**1. Exception Handling:**
- [ ] All repository calls wrapped in try-catch
- [ ] Throws `DaoException` with specific error code
- [ ] Uses `executeWithExceptionHandling` pattern
- [ ] Logs database errors

**2. Query Methods:**
- [ ] Custom queries use MongoTemplate or @Query
- [ ] Complex queries documented with comments
- [ ] Aggregation queries optimized

---

### Day 4: Add Basic Tests (4-5 hours)

#### Test Structure

Create test files in `src/test/java/com/flashcards/backend/flashcards/`:

**1. FlashcardServiceTest.java**

```java
package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.dao.FlashcardDao;
import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.flashcards.backend.flashcards.mapper.FlashcardMapper;
import com.flashcards.backend.flashcards.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardDao flashcardDao;

    @Mock
    private FlashcardMapper flashcardMapper;

    @InjectMocks
    private FlashcardService flashcardService;

    private CreateFlashcardDto validRequest;
    private Flashcard mockFlashcard;
    private FlashcardDto mockFlashcardDto;

    @BeforeEach
    void setUp() {
        // TODO: Set up test data
        validRequest = CreateFlashcardDto.builder()
            .deckId("deck-123")
            .userId("user-123")
            // ... add more fields
            .build();

        mockFlashcard = Flashcard.builder()
            .id("flashcard-123")
            .build();

        mockFlashcardDto = FlashcardDto.builder()
            .id("flashcard-123")
            .build();
    }

    @Test
    @DisplayName("createFlashcard - with valid data - returns saved flashcard")
    void createFlashcard_withValidData_returnsSavedFlashcard() {
        // Given
        when(flashcardMapper.toEntity(any())).thenReturn(mockFlashcard);
        when(flashcardDao.save(any())).thenReturn(mockFlashcard);
        when(flashcardMapper.toDto(any())).thenReturn(mockFlashcardDto);

        // When
        FlashcardDto result = flashcardService.createFlashcard(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("flashcard-123");
        verify(flashcardDao, times(1)).save(any());
    }

    @Test
    @DisplayName("createFlashcard - with invalid data - throws ServiceException")
    void createFlashcard_withInvalidData_throwsServiceException() {
        // Given
        CreateFlashcardDto invalidRequest = CreateFlashcardDto.builder()
            .deckId("") // Invalid
            .build();

        // When/Then
        assertThatThrownBy(() -> flashcardService.createFlashcard(invalidRequest))
            .isInstanceOf(ServiceException.class);
    }

    // TODO: Add more test cases:
    // - getFlashcardById - with existing ID - returns flashcard
    // - getFlashcardById - with non-existing ID - throws exception
    // - updateFlashcard - with valid data - returns updated flashcard
    // - deleteFlashcard - with existing ID - deletes successfully
    // - getFlashcardsByDeckId - returns list of flashcards
}
```

**2. AuthServiceTest.java**

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login - with valid credentials - returns auth response")
    void login_withValidCredentials_returnsAuthResponse() {
        // TODO: Implement
    }

    @Test
    @DisplayName("login - with invalid password - throws exception")
    void login_withInvalidPassword_throwsException() {
        // TODO: Implement
    }

    @Test
    @DisplayName("register - with new user - creates user successfully")
    void register_withNewUser_createsUserSuccessfully() {
        // TODO: Implement
    }

    @Test
    @DisplayName("register - with existing email - throws exception")
    void register_withExistingEmail_throwsException() {
        // TODO: Implement
    }
}
```

**Your Task: Write Tests**
- [ ] FlashcardService: 10 tests
- [ ] DeckService: 8 tests
- [ ] AuthService: 10 tests
- [ ] TotpService: 6 tests

**Run tests:**
```bash
./mvnw test
./mvnw jacoco:report
open target/site/jacoco/index.html
```

**Target: 40% coverage minimum**

---

### Day 5: Documentation & Cleanup (2-3 hours)

#### Create Documentation Files

**1. Create .env.example**

Create: `.env.example` in project root

```env
# MongoDB Configuration
MONGO_URL=mongodb://localhost:27017
MONGODB_DATABASE=flashcards

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=your-secret-key-here-min-32-chars
JWT_EXPIRATION=86400000
JWT_ISSUER=flashcards-app

# OAuth2 - Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# OAuth2 - GitHub
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
GITHUB_REDIRECT_URI=http://localhost:8080/login/oauth2/code/github

# AI Providers
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...
VERTEX_AI_PROJECT_ID=your-project-id
VERTEX_AI_LOCATION=us-central1
VERTEX_AI_CREDENTIALS_URI=file:///path/to/credentials.json

# Jasypt Encryption
JASYPT_ENCRYPTOR_PASSWORD=your-encryption-password

# Admin User (for initial setup)
ADMIN_EMAIL=admin@example.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin-password
ADMIN_FIRST_NAME=Admin
ADMIN_LAST_NAME=User

# OAuth Success/Failure URLs
OAUTH_SUCCESS_REDIRECT_URL=http://localhost:3000/auth/success
OAUTH_FAILURE_REDIRECT_URL=http://localhost:3000/auth/error
```

**2. Update README.md**

Add this section to README:

```markdown
## Current MVP Status

### Implemented Features ✅
- **Authentication & Authorization**
  - Email/password registration and login
  - OAuth2 (Google, GitHub)
  - TOTP two-factor authentication
  - JWT-based session management
  - Role-based access control

- **Flashcard Management**
  - CRUD operations for decks and flashcards
  - Support for code blocks with syntax highlighting
  - Tagging and categorization
  - Difficulty levels

- **AI-Powered Features**
  - Generate flashcards from text (GPT-4o, Claude, Gemini)
  - Summarize content (text, images, PDFs)
  - Image-to-flashcards using vision models
  - Automatic fallback to cheaper models

- **Audio Features**
  - Text-to-speech (6 voices, adjustable speed)
  - Audio summarization
  - Speech-to-text transcription
  - Voice-to-flashcards generation

### In Progress 🚧
- Study sessions with spaced repetition (SM-2 algorithm)
- Comprehensive testing suite (target: 80% coverage)
- Rate limiting and security hardening
- Performance optimization

### Testing
```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Known Issues 🐛
(Add issues you found during testing)
```

**3. Create Manual Testing Checklist**

Save your testing results to: `docs/manual-testing-results.md`

---

## ✅ Final Checklist Before Creating PR

Before you create your pull request, verify:

### Code Quality
- [ ] No unused imports
- [ ] No `System.out.println()` (use logging)
- [ ] No commented-out code blocks
- [ ] All TODOs either fixed or documented
- [ ] Constants used instead of magic numbers
- [ ] Proper use of StringUtils, Objects, CollectionUtils

### Testing
- [ ] All tests pass: `./mvnw test`
- [ ] Coverage measured: `./mvnw jacoco:report`
- [ ] Target coverage achieved (40%+ overall)
- [ ] No failing tests

### Documentation
- [ ] .env.example created
- [ ] README updated with current status
- [ ] Postman collection created and tested
- [ ] Manual testing results documented

### Git
- [ ] All changes committed
- [ ] Commit messages follow convention
- [ ] Branch is up to date with main

---

## 📝 Create Pull Request

**Title:**
```
chore: code review, cleanup, and testing baseline
```

**Description:**
```markdown
## Summary
Completed comprehensive code review and established testing baseline before implementing major features.

## Changes Made

### Code Review & Fixes
- Reviewed all 6 controllers for CLAUDE.md compliance
- Reviewed all 7 services for proper error handling
- Fixed [X] issues found during review
- Standardized logging patterns
- Removed unused imports and dead code

### Bug Fixes
- Fixed AIAudioService line 78: null check for optional field
- Fixed FlashcardController: changed POST /flashcards to return 201
- Fixed [add other bugs you found]

### Testing
- Added unit tests for FlashcardService (10 tests)
- Added unit tests for AuthService (10 tests)
- Added unit tests for DeckService (8 tests)
- Added unit tests for TotpService (6 tests)
- **Coverage: [X]%** (baseline established)

### Documentation
- Created .env.example with all required variables
- Created Postman collection with all endpoints
- Updated README with current MVP status
- Documented manual testing results

## Manual Testing Results
✅ Authentication: All flows working
✅ Flashcard CRUD: All operations working
⚠️ AI Features: Working but slow (15s for summarization)
🐛 Audio STT: Rejects WAV files (MP3 only)

## Breaking Changes
None

## Checklist
- [x] Code follows CLAUDE.md standards
- [x] All tests passing
- [x] Documentation updated
- [x] No breaking changes

## Related Issues
Closes #6
```

**Then:**
```bash
git push origin chore/code-review-and-cleanup
```

Go to GitHub and create the PR, then request review (or merge if you're solo).

---

## 🎯 Success Criteria

You've successfully completed this issue when:

✅ All manual tests documented
✅ All code reviewed with findings documented
✅ Critical bugs fixed
✅ Basic test suite created (40%+ coverage)
✅ Postman collection created and tested
✅ .env.example created
✅ README updated
✅ Pull request created and merged

**Congratulations! You now have a solid foundation to build on.** 🎉

Next: Move on to Issue #1 (Rate Limiting) with confidence!
