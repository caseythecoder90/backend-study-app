# Testing Strategy

This document outlines the comprehensive testing strategy for the Flashcards application including unit tests, integration tests, and Cucumber BDD tests.

## Executive Summary

**Current Coverage:** <5%
**Target Coverage:** 80% (line coverage)
**Testing Framework:** JUnit 5, Mockito, Spring Boot Test, Cucumber
**Estimated Effort:** 3-4 weeks

## Current State Assessment

### Existing Tests
- ⚠️ Minimal test coverage (<5%)
- ❌ No unit tests for services
- ❌ No integration tests for controllers
- ❌ No DAO layer tests
- ❌ No Cucumber BDD tests
- ❌ No AI strategy tests

### Critical Gaps
1. **Service Layer:** 0% coverage
2. **DAO Layer:** 0% coverage
3. **Controller Layer:** 0% coverage
4. **AI Strategies:** 0% coverage
5. **Security/Auth:** 0% coverage

## Testing Pyramid

```
                    ┌─────────────────┐
                    │   E2E / BDD     │ 10-15 scenarios
                    │   (Cucumber)    │ (Manual + Automated)
                    └─────────────────┘
                  ┌───────────────────────┐
                  │  Integration Tests    │ 50-70 tests
                  │  (@SpringBootTest)    │ (Controllers, Full Stack)
                  └───────────────────────┘
              ┌───────────────────────────────┐
              │      Unit Tests               │ 200-300 tests
              │  (JUnit 5 + Mockito)          │ (Services, DAOs, Utils)
              └───────────────────────────────┘
```

## Test Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Testing Framework -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- JUnit 5 (included in spring-boot-starter-test) -->
    <!-- Mockito (included in spring-boot-starter-test) -->
    <!-- AssertJ (included in spring-boot-starter-test) -->

    <!-- MongoDB Test -->
    <dependency>
        <groupId>de.flapdoodle.embed</groupId>
        <artifactId>de.flapdoodle.embed.mongo</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Spring Security Test -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Cucumber BDD -->
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java</artifactId>
        <version>7.15.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-spring</artifactId>
        <version>7.15.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-junit-platform-engine</artifactId>
        <version>7.15.0</version>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers (alternative to embedded MongoDB) -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mongodb</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>

    <!-- REST Assured (for API testing) -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Unit Testing Strategy

### 1. Service Layer Testing

**Target:** 80% line coverage for all services

#### Example: FlashcardService Test

```java
@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardDao flashcardDao;

    @Mock
    private DeckDao deckDao;

    @Mock
    private FlashcardMapper flashcardMapper;

    @InjectMocks
    private FlashcardService flashcardService;

    private CreateFlashcardDto validRequest;
    private Flashcard mockFlashcard;
    private FlashcardDto mockFlashcardDto;

    @BeforeEach
    void setUp() {
        validRequest = CreateFlashcardDto.builder()
            .deckId("deck-123")
            .userId("user-123")
            .front(CardContentDto.builder().text("What is DI?").type(ContentType.TEXT_ONLY).build())
            .back(CardContentDto.builder().text("Dependency Injection...").type(ContentType.TEXT_ONLY).build())
            .difficulty(DifficultyLevel.MEDIUM)
            .build();

        mockFlashcard = Flashcard.builder()
            .id("flashcard-123")
            .deckId("deck-123")
            .userId("user-123")
            .front(Flashcard.CardContent.builder().text("What is DI?").build())
            .build();

        mockFlashcardDto = FlashcardDto.builder()
            .id("flashcard-123")
            .deckId("deck-123")
            .build();
    }

    @Test
    @DisplayName("createFlashcard - with valid data - returns saved flashcard")
    void createFlashcard_withValidData_returnsSavedFlashcard() {
        // Given
        when(deckDao.existsById("deck-123")).thenReturn(true);
        when(flashcardMapper.toEntity(validRequest)).thenReturn(mockFlashcard);
        when(flashcardDao.save(mockFlashcard)).thenReturn(mockFlashcard);
        when(flashcardMapper.toDto(mockFlashcard)).thenReturn(mockFlashcardDto);

        // When
        FlashcardDto result = flashcardService.createFlashcard(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("flashcard-123");
        verify(deckDao, times(1)).existsById("deck-123");
        verify(flashcardDao, times(1)).save(mockFlashcard);
        verify(deckDao, times(1)).incrementFlashcardCount("deck-123");
    }

    @Test
    @DisplayName("createFlashcard - with non-existent deck - throws ServiceException")
    void createFlashcard_withNonExistentDeck_throwsServiceException() {
        // Given
        when(deckDao.existsById("deck-123")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> flashcardService.createFlashcard(validRequest))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Deck not found")
            .extracting("errorCode").isEqualTo(ErrorCode.SERVICE_DECK_NOT_FOUND);

        verify(flashcardDao, never()).save(any());
    }

    @Test
    @DisplayName("createFlashcard - when DAO throws exception - wraps in ServiceException")
    void createFlashcard_whenDaoThrowsException_wrapsInServiceException() {
        // Given
        when(deckDao.existsById(anyString())).thenReturn(true);
        when(flashcardMapper.toEntity(any())).thenReturn(mockFlashcard);
        when(flashcardDao.save(any())).thenThrow(new DaoException("Database error", ErrorCode.DAO_SAVE_ERROR));

        // When/Then
        assertThatThrownBy(() -> flashcardService.createFlashcard(validRequest))
            .isInstanceOf(ServiceException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.SERVICE_FLASHCARD_CREATE_ERROR);
    }

    @Test
    @DisplayName("getFlashcardsByDeckId - with valid deck - returns flashcards")
    void getFlashcardsByDeckId_withValidDeck_returnsFlashcards() {
        // Given
        List<Flashcard> mockFlashcards = List.of(mockFlashcard);
        when(flashcardDao.findByDeckId("deck-123")).thenReturn(mockFlashcards);
        when(flashcardMapper.toDto(mockFlashcard)).thenReturn(mockFlashcardDto);

        // When
        List<FlashcardDto> result = flashcardService.getFlashcardsByDeckId("deck-123");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("flashcard-123");
        verify(flashcardDao, times(1)).findByDeckId("deck-123");
    }

    @Test
    @DisplayName("deleteFlashcard - with valid ID - deletes successfully")
    void deleteFlashcard_withValidId_deletesSuccessfully() {
        // Given
        when(flashcardDao.findById("flashcard-123")).thenReturn(Optional.of(mockFlashcard));
        doNothing().when(flashcardDao).deleteById("flashcard-123");

        // When
        flashcardService.deleteFlashcard("flashcard-123");

        // Then
        verify(flashcardDao, times(1)).deleteById("flashcard-123");
        verify(deckDao, times(1)).decrementFlashcardCount("deck-123");
    }
}
```

**Coverage Areas for Each Service:**
- ✅ Happy path (successful operations)
- ✅ Validation failures
- ✅ DAO exceptions
- ✅ Edge cases (null, empty, boundary values)
- ✅ Business logic branches

### 2. DAO Layer Testing

**Target:** 80% line coverage with embedded MongoDB

#### Example: FlashcardDao Test

```java
@DataMongoTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlashcardDaoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FlashcardRepository flashcardRepository;

    private FlashcardDao flashcardDao;

    @BeforeEach
    void setUp() {
        flashcardDao = new FlashcardDao(flashcardRepository, mongoTemplate);
        mongoTemplate.dropCollection(Flashcard.class);
    }

    @Test
    @DisplayName("save - with valid flashcard - returns saved entity")
    void save_withValidFlashcard_returnsSavedEntity() {
        // Given
        Flashcard flashcard = Flashcard.builder()
            .deckId("deck-123")
            .userId("user-123")
            .front(Flashcard.CardContent.builder().text("Question").type(ContentType.TEXT_ONLY).build())
            .back(Flashcard.CardContent.builder().text("Answer").type(ContentType.TEXT_ONLY).build())
            .difficulty(DifficultyLevel.MEDIUM)
            .timesStudied(0)
            .timesCorrect(0)
            .timesIncorrect(0)
            .build();

        // When
        Flashcard result = flashcardDao.save(flashcard);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDeckId()).isEqualTo("deck-123");
        assertThat(result.getCreatedAt()).isNotNull();

        // Verify in database
        Optional<Flashcard> found = flashcardRepository.findById(result.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFront().getText()).isEqualTo("Question");
    }

    @Test
    @DisplayName("findByDeckId - with existing deck - returns all flashcards")
    void findByDeckId_withExistingDeck_returnsAllFlashcards() {
        // Given
        Flashcard card1 = createTestFlashcard("deck-123", "Q1", "A1");
        Flashcard card2 = createTestFlashcard("deck-123", "Q2", "A2");
        Flashcard card3 = createTestFlashcard("deck-456", "Q3", "A3");

        mongoTemplate.save(card1);
        mongoTemplate.save(card2);
        mongoTemplate.save(card3);

        // When
        List<Flashcard> result = flashcardDao.findByDeckId("deck-123");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("front.text").containsExactlyInAnyOrder("Q1", "Q2");
    }

    @Test
    @DisplayName("findByDeckId - with non-existent deck - returns empty list")
    void findByDeckId_withNonExistentDeck_returnsEmptyList() {
        // When
        List<Flashcard> result = flashcardDao.findByDeckId("non-existent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById - with existing flashcard - deletes successfully")
    void deleteById_withExistingFlashcard_deletesSuccessfully() {
        // Given
        Flashcard flashcard = createTestFlashcard("deck-123", "Q", "A");
        Flashcard saved = mongoTemplate.save(flashcard);

        // When
        flashcardDao.deleteById(saved.getId());

        // Then
        Optional<Flashcard> found = flashcardRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("updateStudyStatistics - updates times studied and correct")
    void updateStudyStatistics_updatesTimesStudiedAndCorrect() {
        // Given
        Flashcard flashcard = createTestFlashcard("deck-123", "Q", "A");
        Flashcard saved = mongoTemplate.save(flashcard);

        // When
        flashcardDao.updateStudyStatistics(saved.getId(), true);

        // Then
        Flashcard updated = flashcardRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getTimesStudied()).isEqualTo(1);
        assertThat(updated.getTimesCorrect()).isEqualTo(1);
        assertThat(updated.getTimesIncorrect()).isEqualTo(0);
        assertThat(updated.getLastStudiedAt()).isNotNull();
    }

    private Flashcard createTestFlashcard(String deckId, String front, String back) {
        return Flashcard.builder()
            .deckId(deckId)
            .userId("user-123")
            .front(Flashcard.CardContent.builder().text(front).type(ContentType.TEXT_ONLY).build())
            .back(Flashcard.CardContent.builder().text(back).type(ContentType.TEXT_ONLY).build())
            .difficulty(DifficultyLevel.MEDIUM)
            .build();
    }
}
```

**Coverage Areas for DAOs:**
- ✅ CRUD operations
- ✅ Custom queries
- ✅ Update operations
- ✅ Complex aggregations
- ✅ Error scenarios (duplicate keys, invalid references)

### 3. AI Strategy Testing

**Target:** 80% coverage with mocked AI models

#### Example: TextToFlashcardsStrategy Test

```java
@ExtendWith(MockitoExtension.class)
class TextToFlashcardsStrategyTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private PromptSanitizationService sanitizationService;

    @Mock
    private AIResponseValidator responseValidator;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TextToFlashcardsStrategy strategy;

    @Test
    @DisplayName("execute - with valid input - returns flashcards")
    void execute_withValidInput_returnsFlashcards() throws JsonProcessingException {
        // Given
        AIGenerateRequestDto request = AIGenerateRequestDto.builder()
            .userId("user-123")
            .deckId("deck-123")
            .text("Dependency injection is a design pattern in Spring.")
            .count(3)
            .model(AIModelEnum.GPT_4O_MINI)
            .build();

        String sanitizedText = "Dependency injection is a design pattern in Spring.";
        when(sanitizationService.sanitizeInput(anyString())).thenReturn(sanitizedText);

        String mockJsonResponse = """
            [
                {
                    "front": {"text": "What is DI?", "type": "TEXT_ONLY"},
                    "back": {"text": "A design pattern", "type": "TEXT_ONLY"},
                    "difficulty": "MEDIUM"
                }
            ]
            """;

        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Result mockResult = mock(Result.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);

        when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockResult);
        when(mockResult.getOutput()).thenReturn(mockMessage);
        when(mockMessage.getText()).thenReturn(mockJsonResponse);

        List<CreateFlashcardDto> expectedFlashcards = List.of(
            CreateFlashcardDto.builder()
                .front(CardContentDto.builder().text("What is DI?").type(ContentType.TEXT_ONLY).build())
                .back(CardContentDto.builder().text("A design pattern").type(ContentType.TEXT_ONLY).build())
                .difficulty(DifficultyLevel.MEDIUM)
                .build()
        );

        when(responseValidator.validateAndParse(eq(mockJsonResponse), any(TypeReference.class), anyInt()))
            .thenReturn(expectedFlashcards);

        // When
        List<CreateFlashcardDto> result = strategy.execute(request);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFront().getText()).isEqualTo("What is DI?");
        verify(chatModel, times(1)).call(any(Prompt.class));
        verify(sanitizationService, times(1)).sanitizeInput(request.getText());
    }

    @Test
    @DisplayName("validateInput - with blank text - throws ServiceException")
    void validateInput_withBlankText_throwsServiceException() {
        // Given
        AIGenerateRequestDto request = AIGenerateRequestDto.builder()
            .text("")
            .count(5)
            .build();

        // When/Then
        assertThatThrownBy(() -> strategy.validateInput(request))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Text is required")
            .extracting("errorCode").isEqualTo(ErrorCode.SERVICE_VALIDATION_ERROR);
    }

    @Test
    @DisplayName("validateInput - with excessive count - throws ServiceException")
    void validateInput_withExcessiveCount_throwsServiceException() {
        // Given
        AIGenerateRequestDto request = AIGenerateRequestDto.builder()
            .text("Valid text")
            .count(25) // Exceeds MAX_FLASHCARDS_PER_REQUEST (20)
            .build();

        // When/Then
        assertThatThrownBy(() -> strategy.validateInput(request))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("exceeds maximum")
            .extracting("errorCode").isEqualTo(ErrorCode.SERVICE_AI_INVALID_COUNT);
    }
}
```

## Integration Testing Strategy

### 1. Controller Integration Tests

**Target:** All endpoints tested with MockMvc

#### Example: FlashcardController Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class FlashcardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;
    private String userId;
    private String deckId;

    @BeforeEach
    void setUp() {
        // Clean database
        flashcardRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .roles(Set.of(Role.USER))
            .enabled(true)
            .build();
        user = userRepository.save(user);
        userId = user.getId();

        // Generate JWT token
        jwtToken = jwtUtil.generateToken(userId, user.getEmail(), user.getRoles());

        // Create test deck
        Deck deck = Deck.builder()
            .title("Test Deck")
            .userId(userId)
            .flashcardCount(0)
            .build();
        deck = deckRepository.save(deck);
        deckId = deck.getId();
    }

    @Test
    @DisplayName("POST /api/flashcards - with valid data - creates flashcard")
    void createFlashcard_withValidData_returns201() throws Exception {
        // Given
        String requestBody = String.format("""
            {
                "deckId": "%s",
                "userId": "%s",
                "front": {
                    "text": "What is Spring Boot?",
                    "type": "TEXT_ONLY"
                },
                "back": {
                    "text": "A framework for building Java applications",
                    "type": "TEXT_ONLY"
                },
                "difficulty": "MEDIUM"
            }
            """, deckId, userId);

        // When/Then
        mockMvc.perform(post("/api/flashcards")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.deckId").value(deckId))
            .andExpect(jsonPath("$.front.text").value("What is Spring Boot?"))
            .andExpect(jsonPath("$.back.text").value("A framework for building Java applications"))
            .andExpect(jsonPath("$.difficulty").value("MEDIUM"));

        // Verify in database
        List<Flashcard> flashcards = flashcardRepository.findAll();
        assertThat(flashcards).hasSize(1);
        assertThat(flashcards.get(0).getFront().getText()).isEqualTo("What is Spring Boot?");

        // Verify deck flashcard count updated
        Deck updatedDeck = deckRepository.findById(deckId).orElseThrow();
        assertThat(updatedDeck.getFlashcardCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/flashcards - without authentication - returns 401")
    void createFlashcard_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/flashcards/{id} - with existing ID - returns flashcard")
    void getFlashcard_withExistingId_returns200() throws Exception {
        // Given
        Flashcard flashcard = Flashcard.builder()
            .deckId(deckId)
            .userId(userId)
            .front(Flashcard.CardContent.builder().text("Question").type(ContentType.TEXT_ONLY).build())
            .back(Flashcard.CardContent.builder().text("Answer").type(ContentType.TEXT_ONLY).build())
            .difficulty(DifficultyLevel.EASY)
            .build();
        flashcard = flashcardRepository.save(flashcard);

        // When/Then
        mockMvc.perform(get("/api/flashcards/" + flashcard.getId())
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(flashcard.getId()))
            .andExpect(jsonPath("$.front.text").value("Question"))
            .andExpect(jsonPath("$.difficulty").value("EASY"));
    }

    @Test
    @DisplayName("DELETE /api/flashcards/{id} - with existing ID - deletes flashcard")
    void deleteFlashcard_withExistingId_returns204() throws Exception {
        // Given
        Flashcard flashcard = createTestFlashcard();

        // When/Then
        mockMvc.perform(delete("/api/flashcards/" + flashcard.getId())
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isNoContent());

        // Verify deleted
        Optional<Flashcard> found = flashcardRepository.findById(flashcard.getId());
        assertThat(found).isEmpty();
    }

    private Flashcard createTestFlashcard() {
        Flashcard flashcard = Flashcard.builder()
            .deckId(deckId)
            .userId(userId)
            .front(Flashcard.CardContent.builder().text("Q").type(ContentType.TEXT_ONLY).build())
            .back(Flashcard.CardContent.builder().text("A").type(ContentType.TEXT_ONLY).build())
            .build();
        return flashcardRepository.save(flashcard);
    }
}
```

**Test Configuration (application-test.properties):**
```properties
# Embedded MongoDB for testing
spring.data.mongodb.database=flashcards-test
de.flapdoodle.mongodb.embedded.version=7.0.0

# Disable AI providers in tests
spring.ai.openai.api-key=test-key
spring.ai.anthropic.api-key=test-key
spring.ai.vertex.ai.gemini.project-id=test-project

# JWT test configuration
jwt.secret=test-secret-key-for-testing-purposes-only
jwt.expiration=3600000
```

## Cucumber BDD Testing

### Project Structure

```
src/test/
├── java/
│   └── com/flashcards/backend/flashcards/
│       └── bdd/
│           ├── CucumberSpringConfiguration.java
│           ├── RunCucumberTest.java
│           └── steps/
│               ├── AuthenticationSteps.java
│               ├── FlashcardSteps.java
│               ├── DeckSteps.java
│               ├── StudySessionSteps.java
│               └── AIGenerationSteps.java
└── resources/
    └── features/
        ├── authentication.feature
        ├── flashcard-management.feature
        ├── deck-management.feature
        ├── study-session.feature
        └── ai-flashcard-generation.feature
```

### Cucumber Configuration

```java
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://localhost";
    }

    @Before
    public void setPort() {
        RestAssured.port = port;
    }
}
```

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports.html, json:target/cucumber.json")
public class RunCucumberTest {
}
```

### Feature File Example: authentication.feature

```gherkin
Feature: User Authentication
  As a user
  I want to authenticate with the application
  So that I can access my flashcards securely

  Background:
    Given the application is running
    And the database is clean

  Scenario: Successful registration with email and password
    When user registers with email "john@example.com" and password "SecurePass123!"
    Then the registration should succeed
    And user "john@example.com" should exist in the database
    And user should have role "USER"

  Scenario: Login with valid credentials
    Given user "alice@example.com" exists with password "password123"
    When user logs in with email "alice@example.com" and password "password123"
    Then the login should succeed
    And a JWT token should be returned
    And the token should contain user email "alice@example.com"

  Scenario: Login with invalid credentials
    Given user "bob@example.com" exists with password "correct-password"
    When user logs in with email "bob@example.com" and password "wrong-password"
    Then the login should fail with status 401
    And no JWT token should be returned

  Scenario: TOTP setup and verification
    Given user "carol@example.com" is logged in
    When user requests TOTP setup
    Then a QR code should be returned
    And user's TOTP secret should be stored
    When user submits TOTP code from authenticator
    Then TOTP verification should succeed
    And user's account should be marked as TOTP-enabled

  Scenario: Access protected endpoint without authentication
    When user attempts to access "/api/decks" without token
    Then the request should fail with status 401

  Scenario: Access protected endpoint with expired token
    Given user "dave@example.com" has an expired JWT token
    When user attempts to access "/api/decks" with expired token
    Then the request should fail with status 401
```

### Step Definitions: AuthenticationSteps.java

```java
@Component
public class AuthenticationSteps {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Response response;
    private String jwtToken;
    private User testUser;

    @Given("the application is running")
    public void applicationIsRunning() {
        RestAssured.baseURI = "http://localhost:" + RestAssured.port;
    }

    @Given("the database is clean")
    public void databaseIsClean() {
        userRepository.deleteAll();
    }

    @When("user registers with email {string} and password {string}")
    public void userRegistersWithEmailAndPassword(String email, String password) {
        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "%s",
                "firstName": "Test",
                "lastName": "User"
            }
            """, email, password);

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/api/auth/register");
    }

    @Then("the registration should succeed")
    public void registrationShouldSucceed() {
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    @Then("user {string} should exist in the database")
    public void userShouldExistInDatabase(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo(email);
    }

    @Then("user should have role {string}")
    public void userShouldHaveRole(String roleName) {
        Optional<User> user = userRepository.findAll().stream().findFirst();
        assertThat(user).isPresent();
        assertThat(user.get().getRoles()).contains(Role.valueOf(roleName));
    }

    @Given("user {string} exists with password {string}")
    public void userExistsWithPassword(String email, String password) {
        testUser = User.builder()
            .email(email)
            .username(email.split("@")[0])
            .password(passwordEncoder.encode(password))
            .firstName("Test")
            .lastName("User")
            .roles(Set.of(Role.USER))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
        testUser = userRepository.save(testUser);
    }

    @When("user logs in with email {string} and password {string}")
    public void userLogsInWithEmailAndPassword(String email, String password) {
        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, email, password);

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/api/auth/login");

        if (response.getStatusCode() == 200) {
            jwtToken = response.jsonPath().getString("token");
        }
    }

    @Then("the login should succeed")
    public void loginShouldSucceed() {
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Then("a JWT token should be returned")
    public void jwtTokenShouldBeReturned() {
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).isNotEmpty();
    }

    @Then("the token should contain user email {string}")
    public void tokenShouldContainUserEmail(String email) {
        String emailFromToken = jwtUtil.extractEmail(jwtToken);
        assertThat(emailFromToken).isEqualTo(email);
    }

    @Then("the login should fail with status {int}")
    public void loginShouldFailWithStatus(int statusCode) {
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
    }

    @Then("no JWT token should be returned")
    public void noJwtTokenShouldBeReturned() {
        assertThat(jwtToken).isNull();
    }
}
```

### Feature File: ai-flashcard-generation.feature

```gherkin
Feature: AI-Powered Flashcard Generation
  As a user
  I want to generate flashcards from text using AI
  So that I can quickly create study materials

  Background:
    Given the application is running
    And user "student@example.com" is logged in
    And user has a deck named "Java Fundamentals"

  Scenario: Generate flashcards from educational text
    Given user provides the following text:
      """
      Dependency Injection is a design pattern in Spring Framework.
      It allows for loose coupling between components by injecting
      dependencies rather than creating them within the class.
      There are three types: constructor injection, setter injection,
      and field injection.
      """
    When user requests to generate 5 flashcards with model "GPT_4O_MINI"
    Then flashcard generation should succeed
    And 5 flashcards should be created
    And flashcards should be saved to deck "Java Fundamentals"
    And each flashcard should have front and back content
    And each flashcard should have difficulty level set

  Scenario: Generate flashcards with fallback when primary model fails
    Given the primary AI model "GPT_4O" is unavailable
    And fallback model "GPT_4O_MINI" is available
    When user requests to generate 3 flashcards
    Then the system should use fallback model
    And 3 flashcards should be created successfully

  Scenario: Fail flashcard generation with invalid input
    Given user provides empty text
    When user requests to generate 5 flashcards
    Then flashcard generation should fail with status 400
    And error message should indicate "Text is required"

  Scenario: Respect maximum flashcard limit
    When user requests to generate 25 flashcards
    Then flashcard generation should fail with status 400
    And error message should indicate "exceeds maximum allowed count of 20"

  Scenario: Generate flashcards with code examples
    Given user provides text with code examples:
      """
      In Java, the @Autowired annotation is used for dependency injection.
      Example: @Autowired private MyService service;
      """
    When user requests to generate 2 flashcards with code support
    Then flashcards should include code blocks
    And code blocks should have language "java" specified
```

## Test Data Management

### Test Fixtures and Builders

```java
public class TestDataBuilder {

    public static User.UserBuilder defaultUser() {
        return User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("encoded-password")
            .firstName("Test")
            .lastName("User")
            .roles(Set.of(Role.USER))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now());
    }

    public static Deck.DeckBuilder defaultDeck(String userId) {
        return Deck.builder()
            .title("Test Deck")
            .description("A test deck for unit tests")
            .category("Testing")
            .userId(userId)
            .isPublic(false)
            .tags(List.of("test", "java"))
            .flashcardCount(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now());
    }

    public static Flashcard.FlashcardBuilder defaultFlashcard(String deckId, String userId) {
        return Flashcard.builder()
            .deckId(deckId)
            .userId(userId)
            .front(Flashcard.CardContent.builder()
                .text("What is the question?")
                .type(ContentType.TEXT_ONLY)
                .build())
            .back(Flashcard.CardContent.builder()
                .text("This is the answer.")
                .type(ContentType.TEXT_ONLY)
                .build())
            .difficulty(DifficultyLevel.MEDIUM)
            .timesStudied(0)
            .timesCorrect(0)
            .timesIncorrect(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now());
    }
}
```

### Mock AI Responses

```java
@Component
public class MockAIResponses {

    public static String flashcardGenerationResponse() {
        return """
            [
                {
                    "front": {
                        "text": "What is dependency injection?",
                        "type": "TEXT_ONLY"
                    },
                    "back": {
                        "text": "A design pattern where dependencies are provided to a class rather than created by the class.",
                        "type": "TEXT_ONLY"
                    },
                    "difficulty": "MEDIUM"
                },
                {
                    "front": {
                        "text": "Name the three types of dependency injection in Spring.",
                        "type": "TEXT_ONLY"
                    },
                    "back": {
                        "text": "1. Constructor injection\\n2. Setter injection\\n3. Field injection",
                        "type": "TEXT_ONLY"
                    },
                    "difficulty": "HARD"
                }
            ]
            """;
    }

    public static String summaryResponse() {
        return """
            Dependency Injection (DI) is a fundamental design pattern in Spring Framework
            that promotes loose coupling and testability. By injecting dependencies rather
            than creating them internally, classes become more flexible and easier to test.
            """;
    }
}
```

## Coverage Targets

| Layer | Current | Target | Priority |
|-------|---------|--------|----------|
| Controllers | 0% | 80% | Critical |
| Services | <5% | 80% | Critical |
| DAOs | 0% | 80% | Critical |
| AI Strategies | 0% | 80% | Critical |
| Utils | 10% | 70% | High |
| Config | 0% | 50% | Medium |
| DTOs/Models | N/A | N/A | N/A (no logic) |

**Overall Target:** 80% line coverage

## CI/CD Integration

### Maven Configuration (pom.xml)

```xml
<build>
    <plugins>
        <!-- JaCoCo for code coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>jacoco-check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- Surefire for unit tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                </includes>
            </configuration>
        </plugin>

        <!-- Failsafe for integration tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.2.5</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <includes>
                    <include>**/*IT.java</include>
                    <include>**/*IntegrationTest.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### GitHub Actions Workflow

```yaml
name: Test Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mongodb:
        image: mongo:7.0
        ports:
          - 27017:27017
        env:
          MONGO_INITDB_ROOT_USERNAME: root
          MONGO_INITDB_ROOT_PASSWORD: password

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run unit tests
        run: mvn clean test

      - name: Run integration tests
        run: mvn verify -P integration-tests

      - name: Run Cucumber tests
        run: mvn verify -P cucumber-tests

      - name: Generate coverage report
        run: mvn jacoco:report

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          file: ./target/site/jacoco/jacoco.xml
          fail_ci_if_error: true

      - name: Check coverage threshold
        run: mvn jacoco:check

      - name: Publish test results
        if: always()
        uses: EnricoMi/publish-unit-test-result-action@v2
        with:
          files: |
            target/surefire-reports/**/*.xml
            target/failsafe-reports/**/*.xml
            target/cucumber-reports/*.json
```

## Test Execution Strategy

### Local Development

```bash
# Run all unit tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=FlashcardServiceTest

# Run integration tests
./mvnw verify -P integration-tests

# Run Cucumber tests
./mvnw verify -P cucumber-tests

# Run all tests with coverage
./mvnw clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### CI/CD Pipeline

1. **Pull Request:** Run all tests, enforce 80% coverage
2. **Merge to main:** Run full test suite + deploy to staging
3. **Nightly:** Run extended test suite including performance tests

## Implementation Timeline

### Week 1-2: Unit Tests
- Day 1-2: FlashcardService, DeckService
- Day 3-4: UserService, AuthService, TotpService
- Day 5-6: All DAO classes
- Day 7-10: All AI strategies

### Week 3: Integration Tests
- Day 11-12: FlashcardController, DeckController
- Day 13-14: UserController, AuthController
- Day 15: AIController, AIAudioController

### Week 4: Cucumber BDD
- Day 16-17: Authentication features
- Day 18-19: Flashcard and Deck management features
- Day 20: AI generation features
- Day 21: CI/CD setup and final coverage verification

## Related Documentation

- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md) - Current: <5% coverage, Target: 80%
- [AI Endpoints Code Improvements](./AI-Endpoints-Code-Improvements.md) - Unit tests are Issue #7
- [CLAUDE.md](../CLAUDE.md) - Project coding standards
