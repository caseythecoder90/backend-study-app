# User Journey Sequence Diagrams

## 1. Natural Language Flashcard Generation Journey

```mermaid
sequenceDiagram
    participant User
    participant ChatUI as Chat Interface
    participant ChatController
    participant ChatService
    participant AIOrchestrator
    participant FunctionRegistry
    participant FlashcardFunction
    participant ModelSelector
    participant OpenAI
    participant FlashcardService
    participant MongoDB
    participant Redis

    User->>ChatUI: "Create flashcards about photosynthesis"
    ChatUI->>ChatController: POST /api/chat/message
    ChatController->>ChatService: processMessage(request)

    ChatService->>Redis: getSessionContext(sessionId)
    Redis-->>ChatService: context data

    ChatService->>AIOrchestrator: processRequest(message, context)
    AIOrchestrator->>FunctionRegistry: getAvailableFunctions()
    FunctionRegistry-->>AIOrchestrator: [generate_flashcards, explain_concept, ...]

    AIOrchestrator->>ModelSelector: selectModel(request)
    ModelSelector-->>AIOrchestrator: OpenAI GPT-4

    AIOrchestrator->>OpenAI: chat.completion(prompt + functions)
    OpenAI-->>AIOrchestrator: response + function_call(generate_flashcards)

    AIOrchestrator->>FlashcardFunction: execute(topic="photosynthesis")
    FlashcardFunction->>OpenAI: generate flashcard content
    OpenAI-->>FlashcardFunction: flashcard data

    FlashcardFunction->>FlashcardService: createFlashcards(data)
    FlashcardService->>MongoDB: save flashcards
    MongoDB-->>FlashcardService: saved IDs

    FlashcardService-->>FlashcardFunction: created flashcards
    FlashcardFunction-->>AIOrchestrator: function result

    AIOrchestrator->>OpenAI: completion with function result
    OpenAI-->>AIOrchestrator: formatted response

    AIOrchestrator-->>ChatService: AI response
    ChatService->>Redis: updateSessionContext()
    ChatService-->>ChatController: response
    ChatController-->>ChatUI: formatted response + flashcards
    ChatUI-->>User: "I've created 10 flashcards about photosynthesis..."
```

## 2. Image Analysis and Flashcard Generation Journey

```mermaid
sequenceDiagram
    participant User
    participant WebUI
    participant UploadController
    participant ImageService
    participant VisionAI
    participant AIOrchestrator
    participant AnalyzeImageFunction
    participant S3
    participant FlashcardService
    participant MongoDB
    participant NotificationService

    User->>WebUI: Uploads image of handwritten notes
    WebUI->>UploadController: POST /api/upload/image (multipart)
    UploadController->>ImageService: processImage(file)

    ImageService->>S3: uploadImage(file)
    S3-->>ImageService: imageUrl

    ImageService->>VisionAI: analyzeImage(imageUrl)
    VisionAI->>AIOrchestrator: processVisionRequest(image)

    AIOrchestrator->>AnalyzeImageFunction: execute(imageUrl)
    AnalyzeImageFunction->>VisionAI: extractText + identifyConcepts

    VisionAI-->>AnalyzeImageFunction: {text: "...", concepts: [...], diagrams: [...]}

    AnalyzeImageFunction->>FlashcardService: generateFromImageAnalysis(analysis)
    FlashcardService->>MongoDB: saveFlashcards(cards)
    MongoDB-->>FlashcardService: saved

    FlashcardService-->>AnalyzeImageFunction: created flashcards
    AnalyzeImageFunction-->>AIOrchestrator: analysis result

    AIOrchestrator-->>ImageService: processing complete
    ImageService->>NotificationService: notifyUser(userId, result)

    ImageService-->>UploadController: response
    UploadController-->>WebUI: {status: "success", flashcards: [...]}
    WebUI-->>User: Display extracted content and generated flashcards
```

## 3. Adaptive Study Session Journey

```mermaid
sequenceDiagram
    participant User
    participant StudyUI
    participant StudyController
    participant StudyService
    participant LearningAnalytics
    participant AIOrchestrator
    participant AdaptiveFunction
    participant SpacedRepetition
    participant MongoDB
    participant Redis

    User->>StudyUI: "Start adaptive study session"
    StudyUI->>StudyController: POST /api/study/start
    StudyController->>StudyService: createAdaptiveSession(userId, deckId)

    StudyService->>MongoDB: getUserProfile(userId)
    MongoDB-->>StudyService: learningProfile

    StudyService->>LearningAnalytics: analyzeLearningPattern(userId)
    LearningAnalytics->>MongoDB: getHistoricalData()
    MongoDB-->>LearningAnalytics: performance data
    LearningAnalytics-->>StudyService: {weakAreas: [...], optimalDifficulty: ...}

    StudyService->>SpacedRepetition: getNextCards(userId, deckId)
    SpacedRepetition->>MongoDB: queryDueCards()
    MongoDB-->>SpacedRepetition: flashcards

    StudyService->>AIOrchestrator: generateAdaptiveContent(profile, cards)
    AIOrchestrator->>AdaptiveFunction: execute(learningStyle, difficulty)
    AdaptiveFunction-->>AIOrchestrator: adapted content

    StudyService->>Redis: cacheSession(sessionId, data)
    StudyService-->>StudyController: sessionData
    StudyController-->>StudyUI: {session: {...}, firstCard: {...}}

    loop Study Interaction
        StudyUI-->>User: Present flashcard
        User->>StudyUI: Submit answer
        StudyUI->>StudyController: POST /api/study/answer
        StudyController->>StudyService: processAnswer(sessionId, answer)

        StudyService->>SpacedRepetition: updateProgress(cardId, result)
        StudyService->>LearningAnalytics: trackPerformance(metrics)

        alt Incorrect Answer
            StudyService->>AIOrchestrator: generateExplanation(card, answer)
            AIOrchestrator-->>StudyService: explanation
            StudyService-->>StudyController: {correct: false, explanation: "..."}
        else Correct Answer
            StudyService->>SpacedRepetition: calculateNextReview(cardId)
            StudyService-->>StudyController: {correct: true, nextCard: {...}}
        end

        StudyController-->>StudyUI: response
        StudyUI-->>User: Feedback and next card
    end

    User->>StudyUI: End session
    StudyUI->>StudyController: POST /api/study/complete
    StudyController->>StudyService: completeSession(sessionId)
    StudyService->>MongoDB: saveSessionResults()
    StudyService->>LearningAnalytics: updateLearningProfile()
    StudyService-->>StudyController: sessionSummary
    StudyController-->>StudyUI: summary and recommendations
    StudyUI-->>User: Display progress and next steps
```

## 4. Visual Diagram Generation Journey

```mermaid
sequenceDiagram
    participant User
    participant ChatUI
    participant ChatController
    participant AIOrchestrator
    participant CreateDiagramFunction
    participant MermaidService
    participant ImageGenService
    participant S3
    participant MongoDB

    User->>ChatUI: "Create a mind map for the water cycle"
    ChatUI->>ChatController: POST /api/chat/message
    ChatController->>AIOrchestrator: processRequest(message)

    AIOrchestrator->>CreateDiagramFunction: execute(type="mindmap", topic="water cycle")

    CreateDiagramFunction->>MermaidService: generateMindMap(topic)
    MermaidService->>AIOrchestrator: requestStructure(topic)
    AIOrchestrator-->>MermaidService: hierarchical structure

    MermaidService-->>CreateDiagramFunction: mermaidCode

    alt Generate Image
        CreateDiagramFunction->>ImageGenService: renderMermaid(code)
        ImageGenService-->>CreateDiagramFunction: imageBuffer
        CreateDiagramFunction->>S3: uploadImage(buffer)
        S3-->>CreateDiagramFunction: imageUrl
    end

    CreateDiagramFunction->>MongoDB: saveDiagram(metadata)
    MongoDB-->>CreateDiagramFunction: diagramId

    CreateDiagramFunction-->>AIOrchestrator: {diagramId, mermaidCode, imageUrl}
    AIOrchestrator-->>ChatController: response with diagram
    ChatController-->>ChatUI: formatted response
    ChatUI-->>User: Display mind map with interactive view
```

## 5. Multi-Modal Study Material Creation Journey

```mermaid
sequenceDiagram
    participant User
    participant WebUI
    participant ContentController
    participant MultiModalService
    participant PDFParser
    participant VisionAI
    participant AudioService
    participant AIOrchestrator
    participant ContentSynthesizer
    participant FlashcardService
    participant MongoDB

    User->>WebUI: Upload PDF, images, and audio lecture
    WebUI->>ContentController: POST /api/content/multi-modal
    ContentController->>MultiModalService: processMultiModal(files)

    par Process PDF
        MultiModalService->>PDFParser: extractText(pdf)
        PDFParser-->>MultiModalService: text content
    and Process Images
        MultiModalService->>VisionAI: analyzeImages(images)
        VisionAI-->>MultiModalService: visual concepts
    and Process Audio
        MultiModalService->>AudioService: transcribe(audio)
        AudioService-->>MultiModalService: transcript
    end

    MultiModalService->>ContentSynthesizer: synthesizeContent(all_content)
    ContentSynthesizer->>AIOrchestrator: identifyKeyConcepts(combined)
    AIOrchestrator-->>ContentSynthesizer: {concepts: [...], relationships: [...]}

    ContentSynthesizer->>AIOrchestrator: generateComprehensiveFlashcards()
    AIOrchestrator->>FlashcardService: createWithCrossReferences(data)

    FlashcardService->>MongoDB: saveFlashcardsWithMetadata()
    MongoDB-->>FlashcardService: saved

    FlashcardService-->>ContentSynthesizer: created flashcards
    ContentSynthesizer-->>MultiModalService: synthesis complete

    MultiModalService->>MongoDB: saveLearningPath(flashcards, sources)
    MultiModalService-->>ContentController: response
    ContentController-->>WebUI: {flashcards: [...], studyPath: {...}}
    WebUI-->>User: Display organized study materials
```

## 6. Collaborative Study Group Journey

```mermaid
sequenceDiagram
    participant User1
    participant User2
    participant GroupUI
    participant GroupController
    participant GroupService
    participant AIOrchestrator
    participant FacilitateFunction
    participant WebSocket
    participant MongoDB
    participant Redis

    User1->>GroupUI: Create study group for "Calculus"
    GroupUI->>GroupController: POST /api/groups/create
    GroupController->>GroupService: createGroup(userId, topic)
    GroupService->>MongoDB: saveGroup(groupData)
    GroupService->>WebSocket: createRoom(groupId)
    GroupService-->>GroupController: groupId

    User2->>GroupUI: Join group via invite link
    GroupUI->>GroupController: POST /api/groups/join
    GroupController->>GroupService: addMember(groupId, userId)
    GroupService->>WebSocket: joinRoom(userId, groupId)

    par Notify User1
        WebSocket-->>User1: "User2 joined the group"
    and Notify User2
        WebSocket-->>User2: "Welcome to the group"
    end

    User1->>GroupUI: "Let's review derivatives"
    GroupUI->>WebSocket: broadcastMessage(message)
    WebSocket-->>User2: Display message

    GroupService->>AIOrchestrator: facilitateDiscussion(topic, context)
    AIOrchestrator->>FacilitateFunction: execute(generateQuestion)
    FacilitateFunction-->>AIOrchestrator: "Can someone explain the chain rule?"

    AIOrchestrator-->>GroupService: AI facilitator message
    GroupService->>WebSocket: broadcast(aiMessage)

    par Notify All Users
        WebSocket-->>User1: AI question
        WebSocket-->>User2: AI question
    end

    User2->>GroupUI: Submits answer
    GroupService->>AIOrchestrator: evaluateAnswer(answer)
    AIOrchestrator-->>GroupService: feedback

    GroupService->>Redis: updateGroupProgress()
    GroupService->>MongoDB: saveInteraction()

    GroupService->>WebSocket: broadcastFeedback()
    WebSocket-->>User1: Show User2's answer and feedback
    WebSocket-->>User2: Show feedback

    GroupService->>AIOrchestrator: generateGroupChallenge()
    AIOrchestrator-->>GroupService: collaborative problem
    GroupService->>WebSocket: broadcastChallenge()

    loop Collaborative Problem Solving
        User1->>GroupUI: Contributes solution part
        User2->>GroupUI: Adds to solution
        GroupService->>AIOrchestrator: evaluateProgress()
        AIOrchestrator-->>GroupService: hints or confirmation
        GroupService->>WebSocket: broadcastUpdate()
    end
```

## 7. Voice-Activated Study Journey

```mermaid
sequenceDiagram
    participant User
    participant MobileApp
    participant VoiceController
    participant SpeechService
    participant AIOrchestrator
    participant StudyService
    participant TTSService
    participant AudioStream

    User->>MobileApp: Tap microphone icon
    MobileApp->>VoiceController: startVoiceSession()
    VoiceController->>AudioStream: openStream()

    User->>MobileApp: "Start studying my Spanish deck"
    MobileApp->>SpeechService: processAudio(stream)
    SpeechService-->>VoiceController: "Start studying my Spanish deck"

    VoiceController->>AIOrchestrator: processVoiceCommand(text)
    AIOrchestrator->>StudyService: startVoiceStudy(userId, "Spanish")
    StudyService-->>AIOrchestrator: firstCard

    AIOrchestrator->>TTSService: generateSpeech(cardFront)
    TTSService-->>AudioStream: audioBuffer
    AudioStream-->>MobileApp: Play: "What is 'hello' in Spanish?"

    User->>MobileApp: "Hola"
    MobileApp->>SpeechService: processAudio(response)
    SpeechService-->>VoiceController: "Hola"

    VoiceController->>StudyService: checkAnswer("Hola")
    StudyService-->>VoiceController: {correct: true}

    VoiceController->>TTSService: generateFeedback("Correct!")
    TTSService-->>AudioStream: audioBuffer
    AudioStream-->>MobileApp: Play: "Correct! Next question..."

    loop Voice Study Loop
        StudyService->>VoiceController: nextCard
        VoiceController->>TTSService: generateSpeech(question)
        AudioStream-->>User: Audio question
        User->>MobileApp: Voice answer
        MobileApp->>SpeechService: processAnswer()
        VoiceController->>StudyService: evaluate()
        StudyService-->>VoiceController: result
        VoiceController->>TTSService: feedback
        AudioStream-->>User: Audio feedback
    end

    User->>MobileApp: "Stop studying"
    VoiceController->>StudyService: endSession()
    StudyService-->>VoiceController: summary
    VoiceController->>TTSService: generateSummary()
    AudioStream-->>User: "Great session! You got 8 out of 10 correct."
```

## 8. AI-Powered Content Recommendation Journey

```mermaid
sequenceDiagram
    participant User
    participant DashboardUI
    participant RecommendationController
    participant RecommendationEngine
    participant LearningAnalytics
    participant AIOrchestrator
    participant ContentService
    participant MongoDB

    User->>DashboardUI: Opens dashboard
    DashboardUI->>RecommendationController: GET /api/recommendations
    RecommendationController->>RecommendationEngine: generateRecommendations(userId)

    RecommendationEngine->>LearningAnalytics: getUserProfile(userId)
    LearningAnalytics->>MongoDB: queryUserData()
    MongoDB-->>LearningAnalytics: {studyHistory, performance, preferences}
    LearningAnalytics-->>RecommendationEngine: learningProfile

    RecommendationEngine->>AIOrchestrator: analyzeGaps(profile)
    AIOrchestrator-->>RecommendationEngine: {weakAreas: [...], suggestions: [...]}

    RecommendationEngine->>ContentService: findRelatedContent(suggestions)
    ContentService->>MongoDB: searchPublicDecks(criteria)
    MongoDB-->>ContentService: matchingDecks

    RecommendationEngine->>AIOrchestrator: personalizeRecommendations(decks, profile)
    AIOrchestrator-->>RecommendationEngine: rankedRecommendations

    RecommendationEngine-->>RecommendationController: recommendations
    RecommendationController-->>DashboardUI: {
        daily: [...],
        weekly: [...],
        topics: [...],
        challenges: [...]
    }

    DashboardUI-->>User: Display personalized recommendations

    User->>DashboardUI: Clicks on recommended deck
    DashboardUI->>ContentService: cloneDeck(deckId, userId)
    ContentService->>AIOrchestrator: adaptToUserLevel(deck, profile)
    AIOrchestrator-->>ContentService: adaptedDeck
    ContentService->>MongoDB: saveUserDeck()
    ContentService-->>DashboardUI: deckAdded
    DashboardUI-->>User: "Deck added and adapted to your level"
```

## 9. Exam Preparation Journey

```mermaid
sequenceDiagram
    participant User
    participant ExamUI
    participant ExamController
    participant ExamService
    participant AIOrchestrator
    participant QuizFunction
    participant TimerService
    participant GradingService
    participant MongoDB

    User->>ExamUI: "Prepare me for SAT Math"
    ExamUI->>ExamController: POST /api/exam/prepare
    ExamController->>ExamService: createExamPrep(userId, "SAT Math")

    ExamService->>AIOrchestrator: generateStudyPlan(exam, timeframe)
    AIOrchestrator-->>ExamService: {
        plan: [...],
        milestones: [...],
        practiceExams: [...]
    }

    ExamService->>MongoDB: saveStudyPlan()
    ExamService-->>ExamController: studyPlan
    ExamController-->>ExamUI: Display plan

    User->>ExamUI: "Start practice exam"
    ExamUI->>ExamController: POST /api/exam/start-practice
    ExamController->>ExamService: generatePracticeExam(userId, examType)

    ExamService->>AIOrchestrator: createExam(specifications)
    AIOrchestrator->>QuizFunction: generateQuestions(count=50, type="SAT")
    QuizFunction-->>AIOrchestrator: questions

    ExamService->>TimerService: startTimer(duration=180)
    ExamService->>MongoDB: saveExamSession()
    ExamService-->>ExamController: {exam: {...}, timeLimit: 180}

    loop Exam Taking
        ExamUI-->>User: Display question
        User->>ExamUI: Submit answer
        ExamUI->>ExamController: POST /api/exam/answer
        ExamController->>ExamService: recordAnswer(questionId, answer)
        ExamService->>MongoDB: saveAnswer()
        ExamService->>TimerService: checkTime()

        alt Time Warning
            TimerService-->>ExamService: 5 minutes remaining
            ExamService-->>ExamUI: timeWarning
            ExamUI-->>User: "5 minutes remaining"
        end
    end

    User->>ExamUI: Submit exam
    ExamUI->>ExamController: POST /api/exam/submit
    ExamController->>GradingService: gradeExam(examId)

    GradingService->>AIOrchestrator: evaluateAnswers(questions, answers)
    AIOrchestrator-->>GradingService: {
        score: 85,
        correct: [...],
        incorrect: [...],
        explanations: [...]
    }

    GradingService->>ExamService: generateReport(results)
    ExamService->>MongoDB: saveResults()
    ExamService-->>ExamController: examReport
    ExamController-->>ExamUI: Display results with explanations
    ExamUI-->>User: Score, analysis, and improvement areas
```

## 10. Learning Path Creation Journey

```mermaid
sequenceDiagram
    participant User
    participant PathUI
    participant PathController
    participant PathService
    participant AIOrchestrator
    participant CurriculumFunction
    participant ContentService
    participant ProgressTracker
    participant MongoDB

    User->>PathUI: "I want to learn machine learning from scratch"
    PathUI->>PathController: POST /api/path/create
    PathController->>PathService: generateLearningPath(userId, goal)

    PathService->>AIOrchestrator: analyzeLearningGoal(goal)
    AIOrchestrator->>CurriculumFunction: execute(topic="ML", level="beginner")

    CurriculumFunction->>AIOrchestrator: assessPrerequisites()
    AIOrchestrator-->>CurriculumFunction: [math, statistics, programming]

    CurriculumFunction->>ContentService: findOrCreateContent(prerequisites)
    ContentService-->>CurriculumFunction: prerequisiteDecks

    CurriculumFunction->>AIOrchestrator: generateCurriculum()
    AIOrchestrator-->>CurriculumFunction: {
        phases: [
            {name: "Foundations", duration: "2 weeks", topics: [...]},
            {name: "Core Concepts", duration: "4 weeks", topics: [...]},
            {name: "Advanced", duration: "4 weeks", topics: [...]}
        ]
    }

    PathService->>ContentService: createDecksForPath(curriculum)
    ContentService->>MongoDB: savePathDecks()

    PathService->>ProgressTracker: initializeTracking(userId, pathId)
    ProgressTracker->>MongoDB: createProgressRecord()

    PathService-->>PathController: learningPath
    PathController-->>PathUI: {
        path: {...},
        estimatedDuration: "10 weeks",
        milestones: [...],
        firstDeck: {...}
    }

    PathUI-->>User: Display interactive learning path

    loop Daily Learning
        User->>PathUI: Continue learning
        PathUI->>ProgressTracker: getCurrentPosition(userId, pathId)
        ProgressTracker-->>PathUI: currentDeck, progress

        User->>PathUI: Complete study session
        PathUI->>ProgressTracker: updateProgress()

        alt Milestone Reached
            ProgressTracker->>AIOrchestrator: generateMilestoneAssessment()
            AIOrchestrator-->>ProgressTracker: assessment
            ProgressTracker-->>PathUI: Show milestone quiz
        end
    end
```

## Journey Analytics and Metrics

### Key Performance Indicators per Journey

| Journey | Success Metrics | Tracking Points |
|---------|----------------|-----------------|
| Natural Language Generation | - Time to generate < 3s<br>- User satisfaction > 90%<br>- Flashcard quality score > 4/5 | - Request initiation<br>- AI processing<br>- Content delivery |
| Image Analysis | - OCR accuracy > 95%<br>- Concept extraction rate > 80%<br>- Processing time < 5s | - Upload start<br>- Analysis complete<br>- Flashcard generation |
| Adaptive Study | - Retention improvement > 30%<br>- Engagement rate > 70%<br>- Session completion > 80% | - Session start<br>- Each interaction<br>- Session end |
| Visual Generation | - Generation success rate > 95%<br>- User interaction rate > 60%<br>- Share rate > 20% | - Request<br>- Generation<br>- User interaction |
| Multi-Modal | - Content synthesis accuracy > 85%<br>- Time saved > 50%<br>- Cross-reference quality > 90% | - Upload<br>- Processing<br>- Synthesis complete |
| Collaborative | - Group engagement > 75%<br>- Peer learning effectiveness > 40%<br>- Return rate > 60% | - Group creation<br>- Member interactions<br>- Session completion |
| Voice Study | - Recognition accuracy > 95%<br>- Hands-free completion > 90%<br>- User preference > 70% | - Voice initiation<br>- Each Q&A<br>- Session end |
| Recommendations | - Click-through rate > 30%<br>- Adoption rate > 20%<br>- Relevance score > 4/5 | - Recommendation display<br>- User interaction<br>- Content adoption |
| Exam Prep | - Score improvement > 20%<br>- Practice completion > 85%<br>- Confidence increase > 40% | - Plan creation<br>- Practice sessions<br>- Actual exam results |
| Learning Path | - Path completion > 60%<br>- Milestone achievement > 70%<br>- Knowledge retention > 80% | - Path creation<br>- Daily progress<br>- Milestone completion |

## Error Handling and Recovery Flows

### Common Error Scenarios

```mermaid
sequenceDiagram
    participant User
    participant System
    participant ErrorHandler
    participant FallbackService
    participant NotificationService

    User->>System: Request AI generation
    System->>ErrorHandler: AI Provider Timeout

    ErrorHandler->>FallbackService: Try alternate provider

    alt Fallback Success
        FallbackService-->>System: Result from alternate
        System-->>User: Success with delay notice
    else All Providers Failed
        FallbackService-->>ErrorHandler: All failed
        ErrorHandler->>NotificationService: Queue for retry
        ErrorHandler-->>System: Graceful degradation
        System-->>User: "Generated content will be ready soon"

        Note over NotificationService: Retry in background
        NotificationService->>System: Retry successful
        System->>User: Push notification with results
    end
```