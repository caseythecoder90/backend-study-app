# GitHub Professional Workflow Guide

This guide will help you use GitHub like a professional software team, including issues, pull requests, project boards, and best practices.

## Table of Contents

1. [Creating and Managing Issues](#creating-issues)
2. [Setting Up Project Boards](#project-boards)
3. [Pull Request Workflow](#pull-requests)
4. [Branch Strategy](#branch-strategy)
5. [Code Review Best Practices](#code-review)
6. [GitHub Actions & Automation](#automation)

---

## Creating Issues

### Step 1: Navigate to Issues

1. Go to your repository: `https://github.com/caseythecoder90/backend-study-app`
2. Click the **"Issues"** tab at the top
3. Click **"New issue"** (green button on the right)

### Step 2: Create Your First Issue

**Example: Issue #1 - Implement Rate Limiting**

**Title:**
```
[CRITICAL] Implement Redis-Based Rate Limiting for All Endpoints
```

**Description:** (Copy from `docs/GitHub-Issues-Template.md`)
```markdown
## Description
Currently, the application has no rate limiting, making it vulnerable to:
- Brute force attacks on authentication endpoints
- API abuse and cost overruns (AI endpoints cost $0.50+ per request)
- DDoS attacks

## Acceptance Criteria
- [ ] Add Bucket4j and Redis dependencies to pom.xml
- [ ] Create `RateLimitProperties` configuration class
- [ ] Create `RateLimitService` with token bucket implementation
- [ ] Create `@RateLimited` annotation
- [ ] Implement `RateLimitAspect` for intercepting annotated methods
- [ ] Configure rate limits in application.yml
- [ ] Add rate limit exceeded error handling (429 status)
- [ ] Write unit tests for RateLimitService
- [ ] Write integration tests with embedded Redis
- [ ] Update API documentation

## Related Documentation
- `docs/Rate-Limiting-Redis-Design.md`
- `docs/Security-Measures-Review.md`

## Estimated Effort
2-3 days
```

**Right Sidebar Configuration:**

1. **Assignees:** Assign to yourself
2. **Labels:** Click "Labels" and create/select:
   - `critical` (red) - for MVP blockers
   - `security` (purple) - for security-related issues
   - `backend` (blue) - for backend work
   - `mvp-blocker` (red) - for MVP requirements
3. **Projects:** (We'll set up next)
4. **Milestone:** Create "MVP Release" milestone (we'll do this next)

### Step 3: Create Labels (One-Time Setup)

**To create labels:**
1. Go to Issues tab → Click "Labels"
2. Click "New label"

**Essential Labels to Create:**

**Priority Labels:**
- `critical` - Red (#d73a4a) - MVP blockers
- `high-priority` - Orange (#e99695) - Important features
- `medium-priority` - Yellow (#fbca04) - Nice to have
- `low-priority` - Green (#0e8a16) - Future enhancements

**Type Labels:**
- `bug` - Red (#d73a4a)
- `feature` - Blue (#0075ca)
- `enhancement` - Purple (#a2eeef)
- `refactoring` - Yellow (#fbca04)
- `documentation` - Light blue (#0075ca)
- `testing` - Green (#0e8a16)

**Component Labels:**
- `backend` - Dark blue (#0052cc)
- `frontend` - Light blue (#1d76db)
- `database` - Purple (#5319e7)
- `ai` - Pink (#d876e3)
- `security` - Red (#b60205)
- `authentication` - Orange (#e99695)

**Status Labels:**
- `mvp-blocker` - Red (#b60205)
- `in-progress` - Yellow (#fbca04)
- `blocked` - Red (#d73a4a)
- `needs-review` - Orange (#e99695)
- `ready-for-qa` - Green (#0e8a16)

### Step 4: Create Milestones

1. Go to Issues → Click "Milestones"
2. Click "New milestone"

**Milestone 1: MVP Release**
- **Title:** MVP Release
- **Due date:** (Choose a date 6-8 weeks from now)
- **Description:**
  ```
  Minimum Viable Product with all critical features:
  - Rate limiting and security hardening
  - 80% test coverage
  - Study sessions with spaced repetition
  - All AI endpoints production-ready
  ```

**Milestone 2: Post-MVP Enhancements**
- **Title:** Post-MVP Enhancements
- **Due date:** (2-3 months after MVP)
- **Description:**
  ```
  Nice-to-have features and improvements:
  - Analytics dashboard
  - Daily reminders
  - Content moderation
  - Advanced monitoring
  ```

### Step 5: Bulk Create Issues from Template

**Quick Method:** Use GitHub CLI (if you have it installed)

```bash
# Install GitHub CLI (if not already installed)
# macOS:
brew install gh

# Login to GitHub
gh auth login

# Create issue from template
gh issue create --title "[CRITICAL] Implement Rate Limiting" \
  --body-file docs/GitHub-Issues-Template.md \
  --label "critical,security,backend,mvp-blocker" \
  --milestone "MVP Release"
```

**Manual Method:** Copy each issue from `docs/GitHub-Issues-Template.md` and paste into GitHub's issue creation form.

---

## Project Boards

Project boards help you visualize and organize your work.

### Step 1: Create a Project Board

1. Go to your repository
2. Click "Projects" tab
3. Click "New project"
4. Choose **"Board"** template
5. Name it: **"MVP Sprint"**

### Step 2: Set Up Columns

Default columns are usually: "Todo", "In Progress", "Done"

**Recommended Columns for MVP:**

1. **📋 Backlog** - All issues not yet started
2. **🎯 Ready** - Issues ready to work on (dependencies met)
3. **🚧 In Progress** - Currently being worked on
4. **👀 In Review** - Pull request open, awaiting review
5. **✅ Done** - Completed and merged

**To add/rename columns:**
- Click "+" to add column
- Click column name to rename
- Drag columns to reorder

### Step 3: Add Issues to Board

1. Click "Add item" in a column
2. Search for your issues by number or title
3. Drag issues between columns as work progresses

### Step 4: Automate Board (Optional)

1. Click "⋯" on a column → "Manage automation"
2. **Todo Column:**
   - Auto-add: New issues with milestone "MVP Release"
3. **In Progress Column:**
   - Auto-move: When issue is assigned
   - Auto-move: When pull request is opened
4. **Done Column:**
   - Auto-move: When issue is closed
   - Auto-move: When pull request is merged

### Step 5: Create Filtered Views

**View 1: Critical Path**
- Filter: `is:open label:mvp-blocker`
- Sort by: Priority

**View 2: My Issues**
- Filter: `is:open assignee:@me`

**View 3: Blocked Items**
- Filter: `is:open label:blocked`

---

## Pull Request Workflow

Pull requests (PRs) are how you merge code changes into the main branch.

### Branch Strategy

**Main Branch:** `main` (production-ready code)
**Development Branch:** `develop` (integration branch)
**Feature Branches:** `feature/issue-number-description`
**Bugfix Branches:** `bugfix/issue-number-description`
**Hotfix Branches:** `hotfix/description`

### Step-by-Step: Create Your First Pull Request

#### 1. Create a Feature Branch

```bash
# Make sure you're on main and up to date
git checkout main
git pull origin main

# Create a feature branch for issue #1 (rate limiting)
git checkout -b feature/1-implement-rate-limiting
```

#### 2. Work on Your Feature

```bash
# Make your code changes
# Add files
git add .

# Commit with reference to issue number
git commit -m "feat: implement rate limiting with Redis and Bucket4j

- Add Bucket4j and Redis dependencies
- Create RateLimitProperties and RateLimitService
- Implement @RateLimited annotation and aspect
- Configure rate limits for all endpoints
- Add unit and integration tests

Closes #1"
```

**Commit Message Best Practices:**
- Use conventional commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`
- First line: Brief summary (50 chars max)
- Body: Detailed explanation (wrap at 72 chars)
- Footer: Reference issue with `Closes #1` or `Fixes #1`

#### 3. Push Your Branch

```bash
git push origin feature/1-implement-rate-limiting
```

#### 4. Create Pull Request on GitHub

**After pushing, GitHub will show a banner:**
- Click **"Compare & pull request"**

**OR manually:**
1. Go to repository → "Pull requests" tab
2. Click "New pull request"
3. Base branch: `main`
4. Compare branch: `feature/1-implement-rate-limiting`
5. Click "Create pull request"

**Pull Request Template:**

**Title:**
```
feat: Implement rate limiting with Redis and Bucket4j (#1)
```

**Description:**
```markdown
## Changes
Implements Redis-based rate limiting for all endpoints to prevent abuse and control costs.

## What's Included
- ✅ Bucket4j and Redis integration
- ✅ RateLimitService with token bucket algorithm
- ✅ @RateLimited annotation for easy application
- ✅ Configured limits for all endpoint categories
- ✅ Rate limit exceeded error handling (429 responses)
- ✅ Unit tests (80% coverage)
- ✅ Integration tests with embedded Redis

## Testing
```bash
# Run tests
./mvnw test

# Run integration tests
./mvnw verify
```

## API Changes
All rate-limited endpoints now return these headers:
- `X-RateLimit-Limit`: Total requests allowed
- `X-RateLimit-Remaining`: Remaining requests
- `Retry-After`: Seconds until reset (when limit exceeded)

## Rate Limits Applied
- Auth login: 5 req / 5 min per IP
- AI flashcard generation: 10 req / min per user
- Audio TTS: 10 req / min per user
- (See docs/Rate-Limiting-Redis-Design.md for full list)

## Checklist
- [x] Code follows CLAUDE.md standards
- [x] Tests written and passing
- [x] Documentation updated
- [x] No breaking changes
- [x] Ready for review

## Related Issues
Closes #1

## Screenshots/Demo
(If applicable, add screenshots or GIF demos)
```

**Right Sidebar:**
1. **Reviewers:** Request review from team members
2. **Assignees:** Assign to yourself
3. **Labels:** Same as the issue (`critical`, `security`, etc.)
4. **Projects:** Link to "MVP Sprint" board
5. **Milestone:** "MVP Release"
6. **Linked issues:** GitHub auto-detects "Closes #1"

#### 5. Wait for Review

**Reviewer Checklist:**
- Code follows standards
- Tests are comprehensive
- No security vulnerabilities
- Documentation is updated
- CI/CD passes

#### 6. Address Feedback

If reviewer requests changes:

```bash
# Make changes
git add .
git commit -m "refactor: address PR review feedback

- Extract magic numbers to constants
- Add edge case tests
- Improve error messages"

git push origin feature/1-implement-rate-limiting
```

GitHub automatically updates the PR with new commits.

#### 7. Merge Pull Request

Once approved:

1. Click "Squash and merge" (recommended for clean history)
2. Edit commit message if needed
3. Click "Confirm squash and merge"
4. Delete the feature branch (GitHub prompts you)

**Result:** Issue #1 automatically closes, project board moves to "Done"

---

## Code Review Best Practices

### As a Reviewer

**DO:**
- ✅ Be constructive and specific
- ✅ Ask questions rather than make demands
- ✅ Praise good code
- ✅ Test the changes locally if possible
- ✅ Check for security issues

**Example Good Review:**
```
Great work on implementing rate limiting! A few suggestions:

**Security:**
- Line 45: Consider hashing the rate limit key to prevent user enumeration
- Line 78: Should we log when rate limits are exceeded for monitoring?

**Code Quality:**
- Line 62: This could be extracted to a constant `MAX_RETRIES = 3`

**Question:**
- How does this handle distributed systems with multiple instances?

Overall looks solid! Just those minor points and we're good to merge 🚀
```

**DON'T:**
- ❌ Be vague ("This is bad")
- ❌ Be rude or dismissive
- ❌ Nitpick trivial formatting (use linters instead)
- ❌ Request changes without explanation

### As an Author

**DO:**
- ✅ Respond to all comments
- ✅ Mark conversations as resolved
- ✅ Push commits to address feedback
- ✅ Thank reviewers

**Example Response:**
```
Thanks for the review!

**Security:**
✅ Added hashing for rate limit keys (commit abc123)
✅ Added logging for rate limit exceeded events

**Code Quality:**
✅ Extracted to MAX_RETRIES constant

**Distributed Systems:**
That's a great point! Redis naturally handles this since all instances
share the same Redis cluster. Added a note in the documentation.
```

---

## GitHub Actions & Automation

### Pre-Commit Hooks (Local)

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Run tests before commit

echo "Running tests before commit..."
./mvnw test

if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Commit aborted."
    exit 1
fi

echo "✅ Tests passed. Proceeding with commit."
```

Make executable:
```bash
chmod +x .git/hooks/pre-commit
```

### GitHub Actions Workflow

Create `.github/workflows/pr-checks.yml`:

```yaml
name: Pull Request Checks

on:
  pull_request:
    branches: [ main, develop ]

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

      - name: Run tests
        run: mvn clean test

      - name: Run integration tests
        run: mvn verify

      - name: Check code coverage
        run: mvn jacoco:check

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          file: ./target/site/jacoco/jacoco.xml
          fail_ci_if_error: true

      - name: Comment PR with coverage
        uses: codecov/codecov-action@v4
        with:
          token: ${{ secrets.CODECOV_TOKEN }}

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run OWASP Dependency Check
        run: mvn org.owasp:dependency-check-maven:check

      - name: Run Snyk security scan
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}

  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Check code style
        run: mvn checkstyle:check
```

### Branch Protection Rules

1. Go to Settings → Branches
2. Click "Add rule"
3. Branch name pattern: `main`
4. Enable:
   - ✅ Require a pull request before merging
   - ✅ Require approvals (1 reviewer minimum)
   - ✅ Dismiss stale approvals when new commits are pushed
   - ✅ Require status checks to pass before merging
     - Select: `test`, `security-scan`, `lint`
   - ✅ Require branches to be up to date before merging
   - ✅ Require conversation resolution before merging
   - ✅ Do not allow bypassing the above settings (even for admins)

---

## Professional Touches

### 1. Add Issue Templates

Create `.github/ISSUE_TEMPLATE/bug_report.yml`:

```yaml
name: Bug Report
description: File a bug report
title: "[BUG]: "
labels: ["bug", "needs-triage"]
body:
  - type: markdown
    attributes:
      value: |
        Thanks for taking the time to fill out this bug report!

  - type: textarea
    id: what-happened
    attributes:
      label: What happened?
      description: Also tell us, what did you expect to happen?
      placeholder: Tell us what you see!
    validations:
      required: true

  - type: textarea
    id: reproduce
    attributes:
      label: Steps to Reproduce
      description: How can we reproduce this?
      placeholder: |
        1. Go to '...'
        2. Click on '....'
        3. Scroll down to '....'
        4. See error
    validations:
      required: true

  - type: dropdown
    id: version
    attributes:
      label: Version
      description: What version are you running?
      options:
        - 1.0.0 (Latest)
        - 0.9.0
        - Other
    validations:
      required: true
```

### 2. Add Pull Request Template

Create `.github/PULL_REQUEST_TEMPLATE.md`:

```markdown
## Description
<!-- Describe your changes in detail -->

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## How Has This Been Tested?
<!-- Describe the tests you ran to verify your changes -->

## Checklist
- [ ] My code follows the style guidelines (CLAUDE.md)
- [ ] I have performed a self-review of my own code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published

## Related Issues
Closes #(issue number)
```

### 3. Add README Badges

Add to top of `README.md`:

```markdown
# Flashcards Backend

![Build Status](https://github.com/caseythecoder90/backend-study-app/workflows/CI/badge.svg)
![Coverage](https://codecov.io/gh/caseythecoder90/backend-study-app/branch/main/graph/badge.svg)
![Security](https://snyk.io/test/github/caseythecoder90/backend-study-app/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)
```

### 4. Create CONTRIBUTING.md

```markdown
# Contributing to Flashcards Backend

Thank you for your interest in contributing! This document provides guidelines for contributions.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/backend-study-app.git`
3. Create a branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Run tests: `./mvnw test`
6. Commit: `git commit -m "feat: your feature"`
7. Push: `git push origin feature/your-feature-name`
8. Open a Pull Request

## Code Style

Follow the guidelines in [CLAUDE.md](./CLAUDE.md).

## Commit Messages

Use conventional commits:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation only
- `test:` Adding tests
- `refactor:` Code change that neither fixes a bug nor adds a feature

## Questions?

Open an issue with the `question` label.
```

---

## Quick Reference: Your First Week

### Monday: Setup
- [ ] Create labels
- [ ] Create milestones
- [ ] Create project board
- [ ] Set up branch protection

### Tuesday-Wednesday: Create Issues
- [ ] Create 15 critical issues from template
- [ ] Add to project board
- [ ] Assign priorities

### Thursday-Friday: First Feature
- [ ] Pick Issue #1 (Rate Limiting)
- [ ] Create feature branch
- [ ] Implement
- [ ] Write tests
- [ ] Create pull request

### Next Week: Team Workflow
- [ ] Review PR
- [ ] Merge when approved
- [ ] Move to next issue
- [ ] Repeat!

---

## Helpful GitHub Keyboard Shortcuts

While viewing issues/PRs:
- `c` - Create new issue
- `g` `i` - Go to issues
- `g` `p` - Go to pull requests
- `g` `b` - Go to projects
- `/` - Focus search bar
- `?` - Show all shortcuts

---

## Resources

- [GitHub Docs - Issues](https://docs.github.com/en/issues)
- [GitHub Docs - Pull Requests](https://docs.github.com/en/pull-requests)
- [GitHub Docs - Projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)

---

**Remember:** Being organized with issues and PRs makes you look professional, helps you track progress, and makes collaboration easier. Start small, build the habit, and you'll quickly become proficient!

Good luck! 🚀