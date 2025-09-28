# System Architecture and Design Diagrams

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Application<br/>React/Next.js]
        MOBILE[Mobile Application<br/>React Native]
        CLI[CLI Tool<br/>Node.js]
    end

    subgraph "API Gateway Layer"
        GATEWAY[Spring Cloud Gateway<br/>- Rate Limiting<br/>- Authentication<br/>- Load Balancing]
    end

    subgraph "Application Services"
        AUTH_SVC[Authentication Service<br/>- JWT Management<br/>- OAuth2<br/>- TOTP]

        CHAT_SVC[Chat Service<br/>- Session Management<br/>- Context Handling<br/>- Conversation Flow]

        AI_SVC[AI Orchestration Service<br/>- Model Selection<br/>- Function Calling<br/>- Response Processing]

        CONTENT_SVC[Content Service<br/>- Flashcard CRUD<br/>- Deck Management<br/>- Import/Export]

        STUDY_SVC[Study Service<br/>- Study Sessions<br/>- Progress Tracking<br/>- Spaced Repetition]

        VISUAL_SVC[Visual Generation Service<br/>- Diagram Creation<br/>- Mind Maps<br/>- Image Processing]
    end

    subgraph "AI Provider Layer"
        OPENAI[OpenAI API<br/>GPT-4/GPT-4V]
        CLAUDE[Anthropic API<br/>Claude 3]
        GEMINI[Google Vertex AI<br/>Gemini]
    end

    subgraph "Data Layer"
        MONGO[(MongoDB<br/>- Users<br/>- Flashcards<br/>- Decks<br/>- Sessions)]
        REDIS[(Redis<br/>- Session Cache<br/>- Rate Limiting<br/>- AI Response Cache)]
        S3[S3 Storage<br/>- Images<br/>- Documents<br/>- Audio Files]
    end

    subgraph "Infrastructure"
        KAFKA[Kafka<br/>Event Streaming]
        ELASTIC[Elasticsearch<br/>Search & Analytics]
        GRAFANA[Grafana<br/>Monitoring]
    end

    WEB --> GATEWAY
    MOBILE --> GATEWAY
    CLI --> GATEWAY

    GATEWAY --> AUTH_SVC
    GATEWAY --> CHAT_SVC
    GATEWAY --> CONTENT_SVC
    GATEWAY --> STUDY_SVC

    CHAT_SVC --> AI_SVC
    CONTENT_SVC --> AI_SVC
    STUDY_SVC --> AI_SVC

    AI_SVC --> OPENAI
    AI_SVC --> CLAUDE
    AI_SVC --> GEMINI
    AI_SVC --> VISUAL_SVC

    AUTH_SVC --> MONGO
    CHAT_SVC --> REDIS
    CONTENT_SVC --> MONGO
    STUDY_SVC --> MONGO
    VISUAL_SVC --> S3

    AI_SVC --> KAFKA
    STUDY_SVC --> KAFKA
    KAFKA --> ELASTIC
    ELASTIC --> GRAFANA
```

## 2. UML Class Diagram - Core Domain Model

```mermaid
classDiagram
    class User {
        -String id
        -String email
        -String username
        -String passwordHash
        -Role role
        -UserPreferences preferences
        -LearningProfile learningProfile
        -Date createdAt
        -Date updatedAt
        +register()
        +authenticate()
        +updateProfile()
        +getLearningStats()
    }

    class Deck {
        -String id
        -String userId
        -String title
        -String description
        -List~Tag~ tags
        -DeckSettings settings
        -Date createdAt
        -Date lastStudied
        +addFlashcard()
        +removeFlashcard()
        +share()
        +clone()
        +export()
    }

    class Flashcard {
        -String id
        -String deckId
        -String front
        -String back
        -ContentType contentType
        -DifficultyLevel difficulty
        -List~String~ mediaUrls
        -FlashcardMetadata metadata
        -Date createdAt
        +edit()
        +addMedia()
        +updateDifficulty()
        +getStudyStats()
    }

    class StudySession {
        -String id
        -String userId
        -String deckId
        -SessionType type
        -Date startTime
        -Date endTime
        -List~StudyResult~ results
        -SessionMetrics metrics
        +start()
        +submitAnswer()
        +pause()
        +complete()
        +getProgress()
    }

    class ChatSession {
        -String id
        -String userId
        -List~Message~ messages
        -ConversationContext context
        -Date startTime
        -Date lastActivity
        +sendMessage()
        +processResponse()
        +updateContext()
        +exportTranscript()
    }

    class AIRequest {
        -String id
        -String userId
        -RequestType type
        -String content
        -ModelType model
        -List~Function~ functions
        -RequestOptions options
        +validate()
        +execute()
        +retry()
    }

    class AIResponse {
        -String requestId
        -String content
        -List~FunctionCall~ functionCalls
        -ResponseMetadata metadata
        -TokenUsage tokenUsage
        +parse()
        +cache()
        +format()
    }

    class LearningProfile {
        -String userId
        -LearningStyle style
        -List~String~ strengths
        -List~String~ weaknesses
        -StudyPreferences preferences
        -PerformanceMetrics metrics
        +analyze()
        +updateMetrics()
        +getRecommendations()
    }

    User "1" --> "*" Deck : owns
    User "1" --> "*" StudySession : participates
    User "1" --> "*" ChatSession : initiates
    User "1" --> "1" LearningProfile : has

    Deck "1" --> "*" Flashcard : contains

    StudySession "*" --> "1" Deck : uses
    StudySession "1" --> "*" Flashcard : reviews

    ChatSession "1" --> "*" AIRequest : generates
    AIRequest "1" --> "1" AIResponse : produces

    AIRequest --> User : initiatedBy
    StudySession --> LearningProfile : updates
```

## 3. UML Class Diagram - AI Services Architecture

```mermaid
classDiagram
    class AIOrchestrationService {
        -ModelSelectorService modelSelector
        -FunctionRegistry functionRegistry
        -PromptBuilder promptBuilder
        -ResponseProcessor responseProcessor
        +processRequest(AIRequest): AIResponse
        +selectModel(request): ChatModel
        +executeFunctions(calls): List~Result~
        +handleFallback(error): AIResponse
    }

    class FunctionRegistry {
        -Map~String,AIFunction~ functions
        +register(function: AIFunction)
        +getFunction(name: String): AIFunction
        +getAvailableFunctions(): List~FunctionDef~
        +validateParameters(name, params): Boolean
    }

    class AIFunction {
        <<interface>>
        +getName(): String
        +getDefinition(): FunctionDefinition
        +execute(params: Map): Object
        +validate(params: Map): Boolean
    }

    class GenerateFlashcardsFunction {
        -FlashcardService flashcardService
        -PromptTemplate template
        +execute(params): List~Flashcard~
        +parseResponse(response): List~Flashcard~
    }

    class AnalyzeImageFunction {
        -VisionService visionService
        -ImageProcessor processor
        +execute(params): ImageAnalysis
        +extractConcepts(analysis): List~Concept~
    }

    class CreateDiagramFunction {
        -MermaidService mermaidService
        -DiagramGenerator generator
        +execute(params): Diagram
        +generateMermaidCode(type, data): String
    }

    class ModelSelectorService {
        -List~AIProvider~ providers
        -ModelRegistry registry
        +selectModel(request): ChatModel
        +isAvailable(model): Boolean
        +getFallbackChain(primary): List~Model~
        +validateModel(model, request): Boolean
    }

    class ChatModel {
        <<interface>>
        +call(prompt: Prompt): ChatResponse
        +stream(prompt: Prompt): Stream~ChatResponse~
        +getModelName(): String
        +getProvider(): Provider
    }

    class OpenAIChatModel {
        -OpenAIClient client
        -ModelConfig config
        +call(prompt): ChatResponse
        +countTokens(text): Integer
    }

    class ClaudeChatModel {
        -AnthropicClient client
        -ModelConfig config
        +call(prompt): ChatResponse
        +handleFunctionCalls(response): List
    }

    class GeminiChatModel {
        -VertexAIClient client
        -ModelConfig config
        +call(prompt): ChatResponse
        +processMultiModal(content): Response
    }

    AIOrchestrationService --> FunctionRegistry : uses
    AIOrchestrationService --> ModelSelectorService : uses

    FunctionRegistry --> AIFunction : manages

    AIFunction <|-- GenerateFlashcardsFunction : implements
    AIFunction <|-- AnalyzeImageFunction : implements
    AIFunction <|-- CreateDiagramFunction : implements

    ModelSelectorService --> ChatModel : selects

    ChatModel <|-- OpenAIChatModel : implements
    ChatModel <|-- ClaudeChatModel : implements
    ChatModel <|-- GeminiChatModel : implements
```

## 4. Component Interaction Diagram

```mermaid
graph LR
    subgraph "Frontend Components"
        UI[User Interface]
        CHAT_UI[Chat Interface]
        STUDY_UI[Study Interface]
        DECK_UI[Deck Manager]
    end

    subgraph "Backend Controllers"
        CHAT_CTRL[ChatController]
        AI_CTRL[AIController]
        CONTENT_CTRL[ContentController]
        STUDY_CTRL[StudyController]
    end

    subgraph "Service Layer"
        CHAT_SVC[ChatSessionService]
        AI_ORCH[AIOrchestrationService]
        FUNC_REG[FunctionRegistry]
        MODEL_SEL[ModelSelectorService]
    end

    subgraph "AI Functions"
        F1[GenerateFlashcards]
        F2[AnalyzeImage]
        F3[CreateDiagram]
        F4[ProvideExplanation]
        F5[StartStudySession]
    end

    subgraph "External Services"
        OPENAI_API[OpenAI API]
        CLAUDE_API[Claude API]
        GEMINI_API[Gemini API]
    end

    UI --> CHAT_CTRL
    CHAT_UI --> CHAT_CTRL
    STUDY_UI --> STUDY_CTRL
    DECK_UI --> CONTENT_CTRL

    CHAT_CTRL --> CHAT_SVC
    AI_CTRL --> AI_ORCH

    CHAT_SVC --> AI_ORCH
    AI_ORCH --> FUNC_REG
    AI_ORCH --> MODEL_SEL

    FUNC_REG --> F1
    FUNC_REG --> F2
    FUNC_REG --> F3
    FUNC_REG --> F4
    FUNC_REG --> F5

    MODEL_SEL --> OPENAI_API
    MODEL_SEL --> CLAUDE_API
    MODEL_SEL --> GEMINI_API
```

## 5. Data Flow Diagram

```mermaid
graph TB
    subgraph "User Input"
        TEXT[Text Input]
        IMAGE[Image Upload]
        VOICE[Voice Input]
    end

    subgraph "Input Processing"
        NLP[NLP Processing]
        OCR[OCR Service]
        STT[Speech-to-Text]
    end

    subgraph "AI Processing"
        INTENT[Intent Recognition]
        FUNC_SELECT[Function Selection]
        MODEL_ROUTE[Model Routing]
        EXEC[Function Execution]
    end

    subgraph "Content Generation"
        FLASH_GEN[Flashcard Generation]
        VISUAL_GEN[Visual Generation]
        AUDIO_GEN[Audio Generation]
    end

    subgraph "Output"
        CARDS[Flashcards]
        DIAGRAMS[Diagrams]
        RESPONSES[Chat Responses]
        AUDIO_OUT[Audio Output]
    end

    subgraph "Storage"
        DB[(Database)]
        CACHE[(Cache)]
        FILES[(File Storage)]
    end

    TEXT --> NLP
    IMAGE --> OCR
    VOICE --> STT

    NLP --> INTENT
    OCR --> INTENT
    STT --> INTENT

    INTENT --> FUNC_SELECT
    FUNC_SELECT --> MODEL_ROUTE
    MODEL_ROUTE --> EXEC

    EXEC --> FLASH_GEN
    EXEC --> VISUAL_GEN
    EXEC --> AUDIO_GEN

    FLASH_GEN --> CARDS
    VISUAL_GEN --> DIAGRAMS
    AUDIO_GEN --> AUDIO_OUT

    CARDS --> DB
    DIAGRAMS --> FILES
    RESPONSES --> CACHE

    FLASH_GEN --> RESPONSES
    VISUAL_GEN --> RESPONSES
```

## 6. Deployment Architecture

```mermaid
graph TB
    subgraph "Production Environment - Railway"
        subgraph "Application Tier"
            APP1[Spring Boot App<br/>Instance 1]
            APP2[Spring Boot App<br/>Instance 2]
            LB[Load Balancer]
        end

        subgraph "Caching Tier"
            REDIS_PROD[Redis Cloud<br/>- Session Cache<br/>- Rate Limiting]
        end
    end

    subgraph "Data Tier - MongoDB Atlas"
        MONGO_PRIMARY[(Primary)]
        MONGO_SEC1[(Secondary 1)]
        MONGO_SEC2[(Secondary 2)]
    end

    subgraph "Storage - AWS S3"
        S3_PROD[S3 Bucket<br/>- Images<br/>- Documents<br/>- Exports]
    end

    subgraph "AI Providers"
        OPENAI_PROD[OpenAI API]
        CLAUDE_PROD[Claude API]
        GEMINI_PROD[Gemini API]
    end

    subgraph "Monitoring"
        GRAFANA_CLOUD[Grafana Cloud]
        SENTRY[Sentry<br/>Error Tracking]
    end

    subgraph "CI/CD"
        GITHUB[GitHub Repository]
        ACTIONS[GitHub Actions]
        RAILWAY_DEPLOY[Railway Deployment]
    end

    LB --> APP1
    LB --> APP2

    APP1 --> REDIS_PROD
    APP2 --> REDIS_PROD

    APP1 --> MONGO_PRIMARY
    APP2 --> MONGO_PRIMARY

    MONGO_PRIMARY --> MONGO_SEC1
    MONGO_PRIMARY --> MONGO_SEC2

    APP1 --> S3_PROD
    APP2 --> S3_PROD

    APP1 --> OPENAI_PROD
    APP1 --> CLAUDE_PROD
    APP1 --> GEMINI_PROD

    APP2 --> OPENAI_PROD
    APP2 --> CLAUDE_PROD
    APP2 --> GEMINI_PROD

    APP1 --> GRAFANA_CLOUD
    APP2 --> GRAFANA_CLOUD

    APP1 --> SENTRY
    APP2 --> SENTRY

    GITHUB --> ACTIONS
    ACTIONS --> RAILWAY_DEPLOY
    RAILWAY_DEPLOY --> APP1
    RAILWAY_DEPLOY --> APP2
```

## 7. Security Architecture

```mermaid
graph TB
    subgraph "Security Layers"
        subgraph "Network Security"
            WAF[Web Application Firewall]
            SSL[SSL/TLS Encryption]
            DDOS[DDoS Protection]
        end

        subgraph "Application Security"
            AUTH[Authentication<br/>- JWT<br/>- OAuth2<br/>- TOTP]
            AUTHZ[Authorization<br/>- RBAC<br/>- Resource Permissions]
            RATE[Rate Limiting<br/>- Per User<br/>- Per Endpoint]
        end

        subgraph "Data Security"
            ENCRYPT[Encryption<br/>- At Rest<br/>- In Transit]
            VAULT[Secrets Management<br/>- API Keys<br/>- Credentials]
            AUDIT[Audit Logging]
        end

        subgraph "AI Security"
            CONTENT_FILTER[Content Filtering<br/>- Input Validation<br/>- Output Sanitization]
            PROMPT_INJECT[Prompt Injection<br/>Prevention]
            PII[PII Detection<br/>& Redaction]
        end
    end

    USER[User Request] --> WAF
    WAF --> SSL
    SSL --> AUTH
    AUTH --> AUTHZ
    AUTHZ --> RATE
    RATE --> CONTENT_FILTER
    CONTENT_FILTER --> PROMPT_INJECT
    PROMPT_INJECT --> APP[Application]
    APP --> PII
    PII --> ENCRYPT
    ENCRYPT --> VAULT
    APP --> AUDIT
```

## 8. Event-Driven Architecture

```mermaid
graph LR
    subgraph "Event Producers"
        USER_SVC[User Service]
        AI_SVC[AI Service]
        STUDY_SVC[Study Service]
        CONTENT_SVC[Content Service]
    end

    subgraph "Event Bus"
        KAFKA[Apache Kafka]
        subgraph "Topics"
            T1[user.events]
            T2[ai.generation]
            T3[study.progress]
            T4[content.updates]
        end
    end

    subgraph "Event Consumers"
        ANALYTICS[Analytics Service]
        NOTIFICATION[Notification Service]
        RECOMMENDATION[Recommendation Engine]
        EXPORT[Export Service]
    end

    USER_SVC --> T1
    AI_SVC --> T2
    STUDY_SVC --> T3
    CONTENT_SVC --> T4

    T1 --> ANALYTICS
    T1 --> NOTIFICATION

    T2 --> ANALYTICS
    T2 --> RECOMMENDATION

    T3 --> ANALYTICS
    T3 --> RECOMMENDATION
    T3 --> NOTIFICATION

    T4 --> NOTIFICATION
    T4 --> EXPORT
```

## 9. Monitoring and Observability Stack

```mermaid
graph TB
    subgraph "Application"
        APP[Spring Boot Application]
        ACTUATOR[Spring Actuator<br/>- Health Checks<br/>- Metrics<br/>- Info]
        MICROMETER[Micrometer<br/>- Custom Metrics<br/>- Timers<br/>- Counters]
    end

    subgraph "Data Collection"
        PROM[Prometheus<br/>- Metrics Scraping<br/>- Time Series DB]
        LOKI[Loki<br/>- Log Aggregation]
        TEMPO[Tempo<br/>- Distributed Tracing]
    end

    subgraph "Visualization"
        GRAFANA[Grafana<br/>- Dashboards<br/>- Alerts<br/>- Reports]
    end

    subgraph "Alerting"
        ALERT_MGR[Alert Manager]
        PAGERDUTY[PagerDuty]
        SLACK[Slack Notifications]
    end

    APP --> ACTUATOR
    APP --> MICROMETER

    ACTUATOR --> PROM
    MICROMETER --> PROM

    APP --> LOKI
    APP --> TEMPO

    PROM --> GRAFANA
    LOKI --> GRAFANA
    TEMPO --> GRAFANA

    GRAFANA --> ALERT_MGR
    ALERT_MGR --> PAGERDUTY
    ALERT_MGR --> SLACK
```

## 10. Scalability Strategy

```mermaid
graph TB
    subgraph "Horizontal Scaling"
        LB[Load Balancer]
        APP1[App Instance 1]
        APP2[App Instance 2]
        APP3[App Instance 3]
        APPN[App Instance N]
    end

    subgraph "Data Partitioning"
        SHARD1[MongoDB Shard 1]
        SHARD2[MongoDB Shard 2]
        SHARDN[MongoDB Shard N]
    end

    subgraph "Caching Strategy"
        L1[L1 Cache<br/>Application]
        L2[L2 Cache<br/>Redis]
        CDN[CDN<br/>Static Assets]
    end

    subgraph "Async Processing"
        QUEUE[Message Queue]
        WORKER1[Worker 1]
        WORKER2[Worker 2]
        WORKERN[Worker N]
    end

    LB --> APP1
    LB --> APP2
    LB --> APP3
    LB --> APPN

    APP1 --> L1
    L1 --> L2
    L2 --> SHARD1

    APP2 --> QUEUE
    QUEUE --> WORKER1
    QUEUE --> WORKER2
    QUEUE --> WORKERN

    CDN --> LB
```