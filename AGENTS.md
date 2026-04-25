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