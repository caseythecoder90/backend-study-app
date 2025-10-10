# Issue: Code Review, Cleanup, and Initial Testing

Use this as a template for creating a new GitHub issue.

---

## Title
```
[HIGH] Code Review, Cleanup, and Initial Testing Pass
```

## Description

Before implementing major new features, we need to review the existing codebase, fix immediate issues, and establish a baseline of tests for current functionality. This will ensure we have a solid foundation to build upon.

### Current State
- Test coverage: <5%
- Some duplicate code and inconsistencies
- Audio features recently added but not fully tested
- AI endpoints working but need validation improvements
- No systematic code review performed

### Goals
1. Review all existing code for quality and consistency
2. Fix immediate issues and code smells
3. Add basic tests for existing functionality
4. Document current state and create baseline
5. Identify quick wins and technical debt

## Acceptance Criteria

### Code Review
- [ ] Review all controller classes for consistency with CLAUDE.md standards
- [ ] Review service layer for proper error handling
- [ ] Review DAO layer for proper exception wrapping
- [ ] Check all DTOs have proper validation annotations
- [ ] Verify utility method usage (StringUtils, Objects, etc.)
- [ ] Check for proper use of constants vs magic values
- [ ] Review import statements (no wildcards, proper organization)

### Immediate Fixes
- [ ] Fix any TODO or FIXME comments
- [ ] Remove unused imports and dead code
- [ ] Standardize error messages in ErrorMessages.java
- [ ] Ensure all public methods have JavaDoc
- [ ] Fix any obvious bugs or code smells
- [ ] Standardize logging patterns across classes

### Testing Baseline
- [ ] Add basic unit tests for critical services:
  - [ ] FlashcardService (CRUD operations)
  - [ ] DeckService (CRUD operations)
  - [ ] AuthService (login, registration)
  - [ ] TotpService (setup, verification)
- [ ] Add integration tests for critical endpoints:
  - [ ] POST /api/auth/login
  - [ ] POST /api/flashcards
  - [ ] POST /api/ai/generate-flashcards
- [ ] Run existing tests and ensure all pass
- [ ] Configure JaCoCo and measure current coverage
- [ ] Document test execution in README

### Audio Feature Review (Recently Added)
- [ ] Test AIAudioController endpoints manually with Postman
- [ ] Verify AIAudioService orchestration logic
- [ ] Test TextToSpeechStrategy with real OpenAI API call
- [ ] Test SpeechToTextStrategy with sample audio file
- [ ] Add error handling for audio file validation
- [ ] Add basic unit tests for audio strategies
- [ ] Document audio endpoints in Postman collection

### AI Endpoint Improvements
- [ ] Review AIController for consistency
- [ ] Verify AIExecutionService fallback logic works
- [ ] Test all AI strategies with mock responses
- [ ] Add input validation to all AI request DTOs
- [ ] Verify error codes are specific (not generic)
- [ ] Add basic logging for AI operations

### Documentation
- [ ] Create Postman collection for all endpoints
- [ ] Document environment variables in .env.example
- [ ] Update README with current feature status
- [ ] Document known issues and limitations
- [ ] Create changelog for recent changes

### Code Cleanup Checklist
- [ ] Remove commented-out code
- [ ] Consolidate duplicate validation logic
- [ ] Extract magic numbers to constants
- [ ] Ensure consistent naming conventions
- [ ] Remove debug System.out.println (use logging)
- [ ] Organize package structure if needed

## Testing Plan

### Manual Testing Checklist

**Authentication:**
- [ ] Register new user with email/password
- [ ] Login with valid credentials
- [ ] Login with invalid credentials (should fail)
- [ ] Setup TOTP 2FA
- [ ] Verify TOTP code
- [ ] Verify TOTP with wrong code (should fail)

**Flashcard Management:**
- [ ] Create a deck
- [ ] Create a flashcard in deck
- [ ] Get all flashcards in deck
- [ ] Update a flashcard
- [ ] Delete a flashcard

**AI Features:**
- [ ] Generate flashcards from text (GPT-4o-mini)
- [ ] Generate flashcards and verify fallback if primary fails
- [ ] Summarize text content
- [ ] Generate flashcards from image (if implemented)

**Audio Features:**
- [ ] Convert text to speech (full recitation)
- [ ] Convert text to speech (summary)
- [ ] Convert text to speech (streaming endpoint)
- [ ] Convert speech to text (transcription only)
- [ ] Convert speech to text with summary generation
- [ ] Convert speech to text with flashcard generation

### Automated Test Goals
- [ ] Achieve at least 40% overall coverage (baseline)
- [ ] Critical services: minimum 60% coverage
- [ ] All tests pass in CI/CD (if configured)

## Issues Found (Update as you discover)

### Critical Issues
- (Add any critical bugs found during review)

### Code Smells
- (Add any code quality issues)

### Technical Debt
- (Add items that need refactoring but aren't blocking)

## Related Documentation
- `docs/Testing-Strategy.md` - Testing approach
- `docs/AI-Endpoints-Code-Improvements.md` - Known AI issues
- `CLAUDE.md` - Coding standards
- `docs/Sequence-Diagrams-Audio-Features.md` - Audio flows

## Estimated Effort
3-5 days (1 week)

## Priority
High (P1) - Should be done before major feature work

## Labels
`high-priority`, `refactoring`, `testing`, `code-quality`, `backend`

## Milestone
MVP Release (Foundation work)

## Notes

This is preparatory work to ensure we have a solid foundation before implementing:
- Rate limiting (#1)
- Security hardening (#2-5)
- Comprehensive testing (#6-8)
- Study sessions (#10)

**Benefits of doing this first:**
- Catch bugs early before they compound
- Establish testing patterns to follow
- Create baseline metrics for improvement
- Identify areas that need refactoring
- Document current functionality

**Success Criteria:**
- All existing tests pass
- Basic test coverage established (40%+)
- No critical bugs identified
- Code follows CLAUDE.md standards consistently
- Postman collection created for API testing
- Clear picture of current state and next steps

## Branch Strategy
Create branch: `chore/code-review-and-cleanup`

## Checklist for PR
- [ ] All existing code reviewed and documented
- [ ] Critical fixes applied
- [ ] Basic tests added and passing
- [ ] JaCoCo coverage report generated
- [ ] Postman collection exported
- [ ] README updated with current status
- [ ] No breaking changes introduced