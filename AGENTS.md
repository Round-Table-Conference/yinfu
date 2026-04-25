# AGENTS.md

## Project Structure

- **yinfu-backend/**: Spring Boot 2.7.18, Java 11, MyBatis-Plus, MySQL
- **YinFuAndroid/**: Android Kotlin app (Gradle 8.2.0, Kotlin 1.9.20)

## Backend Commands

```bash
# Run backend
cd yinfu-backend && mvn spring-boot:run

# Build JAR
cd yinfu-backend && mvn clean package -DskipTests
```

- **Port**: 8083
- **Database**: MySQL `yinfu_app` (localhost:3306)
- **Entry point**: `com.yinfu.app.YinfuApplication`

## Key Architecture

- **API Response format**: `Result<T>` with `{code, message, data}` - see `common/Result.java`
- **MyBatis-Plus**: Soft delete enabled (`deleted` field, 1=deleted, 0=active)
- **JWT**: Configured in `application.yml` with 24hr expiration
- **AI Service**: SiliconFlow API (DeepSeek-V3.1 model)

## API Endpoints

All endpoints currently permit all requests (SecurityConfig.java:24-25):

| Prefix | Description |
|--------|-------------|
| `/api/auth/*` | Login, register |
| `/api/user/*` | User profile |
| `/api/sos/*` | SOS emergency |
| `/api/medication/*` | Medication reminders |
| `/api/health/*` | Health records |
| `/api/emergency-contact/*` | Emergency contacts |
| `/api/ai/*` | AI diagnosis & chat |

## Android Build

```bash
cd YinFuAndroid
# Requires local.properties with sdk.dir
./gradlew assembleRelease
```

## Notes

- No tests in repository
- Database credentials hardcoded in `application.yml` (dev only)
- MyBatis mappers in `src/main/resources/mapper/*.xml` if any custom SQL

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.