# Study Sessions Sequence Diagrams

This document contains sequence diagrams for the Study Session system including spaced repetition, progress tracking, and learning analytics.

**⚠️ IMPLEMENTATION STATUS:** Study Sessions are planned for MVP but NOT YET IMPLEMENTED (0% complete)

## 1. Starting a New Study Session

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant StudySessionController
    participant StudySessionService
    participant StudySessionDao
    participant FlashcardService
    participant FlashcardDao
    participant SpacedRepetitionService
    participant MongoDB

    User->>Frontend: Click "Study Now" on deck
    Frontend->>StudySessionController: POST /api/study-sessions/start<br/>{userId, deckId, sessionType: "REVIEW"}

    StudySessionController->>StudySessionService: startStudySession(request)

    StudySessionService->>FlashcardService: getFlashcardsByDeckId(deckId)
    FlashcardService->>FlashcardDao: findByDeckId(deckId)
    FlashcardDao->>MongoDB: Query flashcards collection
    MongoDB-->>FlashcardDao: Return flashcards (e.g., 50 cards)
    FlashcardDao-->>FlashcardService: Return List<FlashcardDto>
    FlashcardService-->>StudySessionService: Return flashcards

    StudySessionService->>SpacedRepetitionService: selectCardsForReview(flashcards, sessionType)

    SpacedRepetitionService->>SpacedRepetitionService: For each card:<br/>- Check lastReviewed date<br/>- Check repetitionLevel<br/>- Calculate next review date (SM-2)<br/>- Filter cards due today

    SpacedRepetitionService-->>StudySessionService: Return 20 cards due for review

    StudySessionService->>StudySessionService: Create StudySession document:<br/>- sessionId (UUID)<br/>- userId, deckId<br/>- sessionType: REVIEW<br/>- startTime: now<br/>- cardResults: [] (empty)<br/>- status: IN_PROGRESS

    StudySessionService->>StudySessionDao: save(studySession)
    StudySessionDao->>MongoDB: Insert study_sessions document
    MongoDB-->>StudySessionDao: Return saved session
    StudySessionDao-->>StudySessionService: Return StudySessionDto

    StudySessionService-->>StudySessionController: Return session with first card

    StudySessionController-->>Frontend: 200 OK<br/>{sessionId, totalCards: 20,<br/>currentCardIndex: 0, flashcard: {...}}

    Frontend->>User: Show flashcard #1 (front side)<br/>Progress: 0/20
```

## 2. Submitting a Card Response (Spaced Repetition)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant StudySessionController
    participant StudySessionService
    participant StudySessionDao
    participant SpacedRepetitionService
    participant FlashcardDao
    participant MongoDB

    User->>Frontend: Rate card difficulty:<br/>"Again" (1), "Hard" (2),<br/>"Good" (3), "Easy" (4)
    Frontend->>StudySessionController: POST /api/study-sessions/{sessionId}/submit-card<br/>{flashcardId, rating, responseTimeMs}

    StudySessionController->>StudySessionService: submitCardResponse(sessionId, request)

    StudySessionService->>StudySessionDao: findById(sessionId)
    StudySessionDao->>MongoDB: Query study_sessions
    MongoDB-->>StudySessionDao: Return session
    StudySessionDao-->>StudySessionService: Return StudySessionDto

    StudySessionService->>SpacedRepetitionService: calculateNextReview(flashcardId, rating)

    SpacedRepetitionService->>FlashcardDao: findById(flashcardId)
    FlashcardDao->>MongoDB: Query flashcards
    MongoDB-->>FlashcardDao: Return flashcard
    FlashcardDao-->>SpacedRepetitionService: Return flashcard with learning data

    SpacedRepetitionService->>SpacedRepetitionService: Apply SM-2 Algorithm:<br/>1. Get current easinessFactor (EF)<br/>2. Get current interval<br/>3. Update based on rating

    alt Rating: Again (1) - Failed
        SpacedRepetitionService->>SpacedRepetitionService: EF = max(1.3, EF - 0.2)<br/>interval = 0 (review today)<br/>repetitionLevel = 0
    else Rating: Hard (2) - Difficult
        SpacedRepetitionService->>SpacedRepetitionService: EF = max(1.3, EF - 0.15)<br/>interval = interval * 1.2<br/>repetitionLevel stays same
    else Rating: Good (3) - Normal
        SpacedRepetitionService->>SpacedRepetitionService: If repetitionLevel = 0: interval = 1 day<br/>If repetitionLevel = 1: interval = 6 days<br/>Else: interval = interval * EF<br/>repetitionLevel++
    else Rating: Easy (4) - Perfect
        SpacedRepetitionService->>SpacedRepetitionService: EF = EF + 0.15<br/>interval = interval * EF * 1.3<br/>repetitionLevel++
    end

    SpacedRepetitionService->>SpacedRepetitionService: Calculate nextReviewDate:<br/>nextReviewDate = today + interval

    SpacedRepetitionService->>FlashcardDao: updateLearningData(flashcardId, {<br/>easinessFactor, interval,<br/>repetitionLevel, lastReviewed, nextReviewDate})
    FlashcardDao->>MongoDB: Update flashcard
    MongoDB-->>FlashcardDao: Confirmation

    SpacedRepetitionService-->>StudySessionService: Return CardResult<br/>{flashcardId, rating, responseTimeMs,<br/>nextReviewDate, wasCorrect}

    StudySessionService->>StudySessionService: Add CardResult to session.cardResults[]<br/>currentCardIndex++

    StudySessionService->>StudySessionDao: update(session)
    StudySessionDao->>MongoDB: Update study_sessions (add card result)
    MongoDB-->>StudySessionDao: Confirmation

    alt More cards remaining
        StudySessionService->>StudySessionService: Get next card

        StudySessionService-->>StudySessionController: Return next card

        StudySessionController-->>Frontend: 200 OK<br/>{currentCardIndex: 1, totalCards: 20,<br/>nextFlashcard: {...}}

        Frontend->>User: Show next flashcard<br/>Progress: 1/20
    else Session complete
        StudySessionService->>StudySessionService: Set session.status = COMPLETED<br/>Set session.endTime = now<br/>Calculate session stats

        StudySessionService->>StudySessionDao: update(session)
        StudySessionDao->>MongoDB: Update session

        StudySessionService-->>StudySessionController: Session complete

        StudySessionController-->>Frontend: 200 OK<br/>{sessionComplete: true, stats: {...}}

        Frontend->>User: Show session summary<br/>(accuracy, time, cards mastered)
    end
```

## 3. Completing a Study Session (Summary)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant StudySessionController
    participant StudySessionService
    participant StudySessionDao
    participant AnalyticsService
    participant MongoDB

    Note over User: User completes last card in session

    Frontend->>StudySessionController: GET /api/study-sessions/{sessionId}/summary

    StudySessionController->>StudySessionService: getSessionSummary(sessionId)

    StudySessionService->>StudySessionDao: findById(sessionId)
    StudySessionDao->>MongoDB: Query study_sessions
    MongoDB-->>StudySessionDao: Return complete session
    StudySessionDao-->>StudySessionService: Return StudySessionDto

    StudySessionService->>StudySessionService: Calculate statistics:<br/>- Total cards reviewed: 20<br/>- Correct (Good/Easy): 15<br/>- Incorrect (Again/Hard): 5<br/>- Accuracy: 75%

    StudySessionService->>StudySessionService: Calculate time statistics:<br/>- Total time: 15 minutes<br/>- Average time per card: 45 seconds<br/>- Fastest card: 12 seconds<br/>- Slowest card: 120 seconds

    StudySessionService->>StudySessionService: Categorize cards:<br/>- Mastered (Easy): 8 cards<br/>- Learning (Good): 7 cards<br/>- Struggling (Again/Hard): 5 cards

    StudySessionService->>StudySessionService: Calculate next review schedule:<br/>- Cards due tomorrow: 5<br/>- Cards due in 3 days: 7<br/>- Cards due in 7+ days: 8

    StudySessionService->>AnalyticsService: recordSessionMetrics(session)
    AnalyticsService->>MongoDB: Update user analytics document
    MongoDB-->>AnalyticsService: Confirmation

    StudySessionService-->>StudySessionController: Return SessionSummaryDto

    StudySessionController-->>Frontend: 200 OK<br/>{accuracy: 75%, totalTime: "15m",<br/>cardsMastered: 8, nextReviewDate: "2024-01-16",<br/>cardsDueTomorrow: 5, streak: 7}

    Frontend->>User: Display session summary:<br/>- Achievement badge (75% accuracy)<br/>- Progress chart<br/>- Study streak updated<br/>- Next review reminder
```

## 4. Resume Interrupted Study Session

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant StudySessionController
    participant StudySessionService
    participant StudySessionDao
    participant MongoDB

    User->>Frontend: Return to app after interruption
    Frontend->>StudySessionController: GET /api/study-sessions/active?userId={userId}

    StudySessionController->>StudySessionService: getActiveSession(userId)

    StudySessionService->>StudySessionDao: findByUserIdAndStatus(userId, "IN_PROGRESS")
    StudySessionDao->>MongoDB: Query study_sessions<br/>filter: {userId, status: "IN_PROGRESS"}

    alt Active session found
        MongoDB-->>StudySessionDao: Return session (started 2 hours ago)
        StudySessionDao-->>StudySessionService: Return StudySessionDto

        StudySessionService->>StudySessionService: Check session timeout:<br/>If startTime > 24 hours ago:<br/>  Mark as ABANDONED<br/>Else:<br/>  Resume session

        alt Session not timed out
            StudySessionService-->>StudySessionController: Return active session

            StudySessionController-->>Frontend: 200 OK<br/>{sessionId, currentCardIndex: 12,<br/>totalCards: 20, flashcard: {...}}

            Frontend->>User: Show modal:<br/>"Resume your study session?<br/>Progress: 12/20 cards"

            User->>Frontend: Click "Resume"
            Frontend->>Frontend: Navigate to study view<br/>with current card

        else Session timed out
            StudySessionService->>StudySessionDao: update(session, status: "ABANDONED")
            StudySessionDao->>MongoDB: Update session
            MongoDB-->>StudySessionDao: Confirmation

            StudySessionService-->>StudySessionController: No active session

            StudySessionController-->>Frontend: 200 OK<br/>{activeSession: null}

            Frontend->>User: Show "Start New Session" button
        end

    else No active session
        MongoDB-->>StudySessionDao: null
        StudySessionDao-->>StudySessionService: null
        StudySessionService-->>StudySessionController: No active session

        StudySessionController-->>Frontend: 200 OK<br/>{activeSession: null}

        Frontend->>User: Show "Start New Session" button
    end
```

## 5. Learning Analytics Dashboard

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AnalyticsController
    participant AnalyticsService
    participant StudySessionDao
    participant FlashcardDao
    participant MongoDB

    User->>Frontend: Navigate to "My Progress" page
    Frontend->>AnalyticsController: GET /api/analytics/dashboard?userId={userId}&period=30

    AnalyticsController->>AnalyticsService: getDashboard(userId, period)

    par Fetch study sessions
        AnalyticsService->>StudySessionDao: findByUserIdAndDateRange(userId, last30Days)
        StudySessionDao->>MongoDB: Query study_sessions
        MongoDB-->>StudySessionDao: Return 15 completed sessions
        StudySessionDao-->>AnalyticsService: Return sessions
    and Fetch flashcards stats
        AnalyticsService->>FlashcardDao: getUserFlashcardStats(userId)
        FlashcardDao->>MongoDB: Aggregate flashcards by deck
        MongoDB-->>FlashcardDao: Return stats
        FlashcardDao-->>AnalyticsService: Return stats
    end

    AnalyticsService->>AnalyticsService: Calculate overall metrics:<br/>- Total study time: 7.5 hours<br/>- Total cards reviewed: 450<br/>- Average accuracy: 78%<br/>- Current streak: 12 days<br/>- Longest streak: 28 days

    AnalyticsService->>AnalyticsService: Calculate daily stats (last 30 days):<br/>- Chart data: [{date, cardsReviewed, accuracy}]<br/>- Identify patterns (best time of day)

    AnalyticsService->>AnalyticsService: Calculate deck performance:<br/>- Per deck: {deckName, cardCount,<br/>  masteredCards, accuracy, lastStudied}

    AnalyticsService->>AnalyticsService: Calculate retention rate:<br/>- Cards reviewed multiple times<br/>- Success rate on repeated cards<br/>- Forgetting curve data

    AnalyticsService->>AnalyticsService: Generate insights:<br/>- "Your accuracy improved 15% this week!"<br/>- "You learn best in the morning"<br/>- "Review 'Spring Boot' deck - 5 cards due"

    AnalyticsService-->>AnalyticsController: Return DashboardDto

    AnalyticsController-->>Frontend: 200 OK<br/>{studyStreak: 12, totalCards: 450,<br/>accuracy: 78%, timeSpent: "7.5h",<br/>chartData: [...], deckPerformance: [...],<br/>insights: [...], upcomingReviews: [...]}

    Frontend->>User: Display dashboard:<br/>- Study streak badge<br/>- Progress charts<br/>- Deck performance table<br/>- AI-generated insights<br/>- Next review schedule
```

## 6. Daily Review Reminder System

```mermaid
sequenceDiagram
    participant CronJob as Scheduled Job<br/>(Daily 9 AM)
    participant ReminderService
    participant StudySessionDao
    participant FlashcardDao
    participant NotificationService
    participant EmailService
    participant PushNotificationService
    participant MongoDB

    CronJob->>ReminderService: @Scheduled(cron = "0 0 9 * * *")<br/>sendDailyReminders()

    ReminderService->>FlashcardDao: getUsersWithDueCards()
    FlashcardDao->>MongoDB: Aggregate query:<br/>Find users with cards where<br/>nextReviewDate ≤ today

    MongoDB-->>FlashcardDao: Return users with due card counts
    FlashcardDao-->>ReminderService: Return List<UserDueCardsDto>

    loop For each user with due cards
        ReminderService->>StudySessionDao: getLastStudyDate(userId)
        StudySessionDao->>MongoDB: Query latest session
        MongoDB-->>StudySessionDao: Return last session date
        StudySessionDao-->>ReminderService: Return date

        ReminderService->>ReminderService: Calculate message:<br/>- Due cards count: 15<br/>- Last studied: 2 days ago<br/>- Streak at risk: Yes (day 13)

        alt User has email notifications enabled
            ReminderService->>EmailService: sendReviewReminder(userEmail, {<br/>dueCards: 15, lastStudied: "2 days ago"})
            EmailService-->>ReminderService: Email sent
        end

        alt User has push notifications enabled
            ReminderService->>PushNotificationService: sendPushNotification(userId, message)
            PushNotificationService-->>ReminderService: Notification sent
        end

        ReminderService->>NotificationService: createInAppNotification(userId, {<br/>type: "REVIEW_REMINDER",<br/>message: "15 cards due for review!"})
        NotificationService->>MongoDB: Insert notification document
        MongoDB-->>NotificationService: Confirmation

    end

    ReminderService->>ReminderService: Log reminder batch completion<br/>Total users notified: 247
```

## Implementation Status

| Feature | Status | Priority | Estimated Effort |
|---------|--------|----------|------------------|
| Start Study Session | ❌ Not Implemented | Critical | 2-3 days |
| Submit Card Response | ❌ Not Implemented | Critical | 2-3 days |
| SM-2 Algorithm | ❌ Not Implemented | Critical | 2-3 days |
| Session Summary | ❌ Not Implemented | High | 1-2 days |
| Resume Session | ❌ Not Implemented | High | 1 day |
| Learning Analytics | ❌ Not Implemented | Medium | 3-4 days |
| Daily Reminders | ❌ Not Implemented | Low | 1-2 days |
| **Total Effort** | - | - | **12-18 days** |

## Database Schema for Study Sessions

### StudySession Document

```json
{
  "_id": "session-uuid",
  "userId": "user-id",
  "deckId": "deck-id",
  "sessionType": "REVIEW", // REVIEW, NEW_CARDS, MIXED
  "status": "IN_PROGRESS", // IN_PROGRESS, COMPLETED, ABANDONED
  "startTime": "2024-01-15T10:00:00Z",
  "endTime": "2024-01-15T10:15:00Z",
  "cardResults": [
    {
      "flashcardId": "card-id-1",
      "rating": 3, // 1=Again, 2=Hard, 3=Good, 4=Easy
      "responseTimeMs": 4500,
      "wasCorrect": true,
      "timestamp": "2024-01-15T10:01:30Z",
      "nextReviewDate": "2024-01-22T00:00:00Z"
    }
  ],
  "totalCards": 20,
  "currentCardIndex": 15,
  "accuracyRate": 0.75,
  "totalTimeMs": 900000, // 15 minutes
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:15:00Z"
}
```

### Flashcard Learning Data (Embedded in Flashcard)

```json
{
  "_id": "flashcard-id",
  "deckId": "deck-id",
  "front": "What is dependency injection?",
  "back": "A design pattern...",
  "learningData": {
    "easinessFactor": 2.5, // SM-2 algorithm (1.3 to 2.5+)
    "interval": 7, // Days until next review
    "repetitionLevel": 3, // Number of successful reviews
    "lastReviewed": "2024-01-15T10:05:00Z",
    "nextReviewDate": "2024-01-22T00:00:00Z",
    "totalReviews": 5,
    "successfulReviews": 4,
    "averageResponseTimeMs": 5000,
    "masteryLevel": "LEARNING" // NEW, LEARNING, YOUNG, MATURE, MASTERED
  }
}
```

### UserAnalytics Document

```json
{
  "_id": "user-id",
  "userId": "user-id",
  "totalStudyTimeMs": 27000000, // 7.5 hours
  "totalCardsReviewed": 450,
  "totalSessionsCompleted": 15,
  "currentStreak": 12, // Days
  "longestStreak": 28,
  "lastStudyDate": "2024-01-15",
  "averageAccuracy": 0.78,
  "cardsMastered": 120,
  "cardsLearning": 80,
  "cardsNew": 50,
  "deckStats": [
    {
      "deckId": "deck-id",
      "deckName": "Spring Boot",
      "totalCards": 50,
      "masteredCards": 30,
      "accuracy": 0.82,
      "lastStudied": "2024-01-15",
      "totalStudyTimeMs": 5400000
    }
  ],
  "dailyActivity": [
    {
      "date": "2024-01-15",
      "cardsReviewed": 20,
      "accuracy": 0.75,
      "studyTimeMs": 900000,
      "sessionsCompleted": 1
    }
  ],
  "createdAt": "2023-12-01T00:00:00Z",
  "updatedAt": "2024-01-15T10:15:00Z"
}
```

## SM-2 (SuperMemo 2) Algorithm Details

The SM-2 algorithm optimizes review intervals based on recall difficulty:

### Algorithm Parameters

- **EF (Easiness Factor)**: 1.3 to 2.5+ (default: 2.5)
- **Interval**: Days until next review
- **Repetition Level**: Count of successful reviews

### Rating Scale

| Rating | Label | Meaning | Impact |
|--------|-------|---------|--------|
| 1 | Again | Complete failure | EF -= 0.2, interval = 0, reset level |
| 2 | Hard | Difficult but recalled | EF -= 0.15, interval *= 1.2 |
| 3 | Good | Recalled with effort | interval = level-based calculation |
| 4 | Easy | Perfect recall | EF += 0.15, interval *= EF * 1.3 |

### Interval Calculation

```java
if (rating < 3) {
    // Failed or struggled - review soon
    interval = (rating == 1) ? 0 : Math.ceil(interval * 1.2);
    if (rating == 1) repetitionLevel = 0;
} else {
    // Successful review
    if (repetitionLevel == 0) interval = 1; // 1 day
    else if (repetitionLevel == 1) interval = 6; // 6 days
    else interval = Math.ceil(previousInterval * easinessFactor);

    repetitionLevel++;
}

// Adjust EF
easinessFactor += (0.1 - (5 - rating) * (0.08 + (5 - rating) * 0.02));
easinessFactor = Math.max(1.3, easinessFactor);
```

## Testing Strategy

### Unit Tests Needed:

1. **SpacedRepetitionService**
   - SM-2 algorithm calculations
   - Card selection logic
   - Next review date calculation

2. **StudySessionService**
   - Session creation
   - Card response submission
   - Session completion
   - Timeout handling

3. **AnalyticsService**
   - Metric calculations
   - Chart data generation
   - Insight generation

### Integration Tests Needed:

1. Complete study session flow (start → submit cards → complete)
2. Session interruption and resume
3. Cross-deck analytics
4. Concurrent session handling

### Cucumber Scenarios:

```gherkin
Feature: Study Session with Spaced Repetition

  Scenario: User completes a study session with varying difficulty
    Given user "john@example.com" has a deck "Java Basics" with 20 flashcards
    And all flashcards are due for review today
    When user starts a study session for deck "Java Basics"
    And user rates card 1 as "Good"
    And user rates card 2 as "Easy"
    And user rates card 3 as "Again"
    And user completes all 20 cards
    Then session summary shows accuracy of 90%
    And card 1 should be scheduled for review in 1 day
    And card 2 should be scheduled for review in 6 days
    And card 3 should be scheduled for review today
    And user's study streak should increase by 1
```

## Related Documentation

- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md) - Study Sessions marked as 0% complete
- [Database ERD](./Database-ERD.md) (to be created) - Full schema documentation
- [Testing Strategy](./Testing-Strategy.md) (to be created)
- [AI Operations](./Sequence-Diagrams-AI-Operations.md)