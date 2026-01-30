# Nova Assistant Setup Guide

**Company:** Control, Backspace & Enter

## Prerequisites

- Android Studio (latest version)
- Android SDK 34+ (Android 14+)
- Kotlin 1.9+
- Gradle 8.0+

## Initial Setup

### 1. Clone and Open Project

```bash
git clone <repository-url>
cd nova_android_assistant/NovaAssistant
```

Open the project in Android Studio.

### 2. Configure Environment Variables

The app uses a `.env` file for sensitive configuration like API keys.

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` file** and add your API keys:
   ```bash
   # Porcupine Wake Word Detection
   # Get your free access key from: https://console.picovoice.ai/
   PORCUPINE_ACCESS_KEY=your_access_key_here
   ```

3. **Get Porcupine Access Key:**
   - Visit https://console.picovoice.ai/
   - Sign up for a free account
   - Create a new project
   - Copy your access key
   - Paste it into `.env` file

### 3. Build the Project

The build system will automatically:
- Read the `.env` file
- Inject values into `BuildConfig`
- Make them available through `AppConfig` class

```bash
./gradlew build
```

## Configuration Architecture

### How It Works

1. **`.env` file** (gitignored) → Contains your actual API keys
2. **`build.gradle.kts`** → Reads `.env` and injects into `BuildConfig`
3. **`AppConfig` class** → Provides type-safe access to configuration
4. **Dependency Injection** → `AppConfig` is injected via Hilt

### Accessing Configuration

In your code, inject `AppConfig`:

```kotlin
@Inject
lateinit var appConfig: AppConfig

// Use it
val accessKey = appConfig.porcupineAccessKey
```

### Adding New Configuration Values

1. **Add to `.env.example`:**
   ```bash
   NEW_API_KEY=
   ```

2. **Update `build.gradle.kts`:**
   ```kotlin
   buildConfigField(
       "String",
       "NEW_API_KEY",
       "\"${envProperties.getProperty("NEW_API_KEY", "")}\""
   )
   ```

3. **Add to `AppConfig.kt`:**
   ```kotlin
   val newApiKey: String
       get() = BuildConfig.NEW_API_KEY
   ```

## Security Best Practices

✅ **DO:**
- Keep `.env` file gitignored (already configured)
- Use `.env.example` as a template (committed to git)
- Store sensitive keys in `.env`, not in code
- Rotate keys regularly
- Use different keys for development and production

❌ **DON'T:**
- Commit `.env` file to git
- Hardcode API keys in source code
- Share `.env` file publicly
- Use production keys in development

## Troubleshooting

### Build Fails: "PORCUPINE_ACCESS_KEY not found"

**Solution:** Make sure you've created `.env` file from `.env.example` and added your access key.

### Access Key Not Working

1. Verify the key is correct in `.env` file
2. Check that the key is valid at https://console.picovoice.ai/
3. Ensure no extra spaces or quotes around the key in `.env`
4. Rebuild the project: `./gradlew clean build`

### Configuration Not Updating

After changing `.env` file:
1. Sync Gradle files in Android Studio
2. Clean and rebuild: `./gradlew clean build`

## Project Structure

```
NovaAssistant/
├── .env                    # Your actual API keys (gitignored)
├── .env.example            # Template file (committed)
├── app/
│   ├── build.gradle.kts    # Reads .env and injects to BuildConfig
│   └── src/main/java/
│       └── com/novaassistant/
│           ├── config/
│           │   └── AppConfig.kt    # Configuration abstraction
│           └── ...
└── ...
```

## Next Steps

After setup:
1. Build and run the app
2. Grant microphone permission when prompted
3. Test wake word detection ("Hey Nova")
4. Check logs: `adb logcat -s NovaWakeWord`

See `WAKE_WORD_DEBUGGING.md` for detailed debugging information.

