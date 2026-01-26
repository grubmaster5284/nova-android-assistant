# Configuration Management

**Company:** Control, Backspace & Enter

## Overview

The Nova Assistant app uses a secure, abstracted configuration system for managing API keys and sensitive information. All configuration values are loaded from a `.env` file at build time and accessed through a type-safe `AppConfig` class.

## Architecture

```
┌─────────────┐
│  .env file  │  (gitignored, contains actual keys)
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ build.gradle.kts │  (reads .env, injects to BuildConfig)
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│   BuildConfig    │  (generated at build time)
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│   AppConfig      │  (abstraction layer, injected via Hilt)
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│  Application     │  (uses AppConfig for configuration)
└──────────────────┘
```

## Files

### 1. `.env.example`
Template file showing required configuration keys. **Committed to git.**

### 2. `.env`
Your actual configuration file with real API keys. **Gitignored, never committed.**

### 3. `app/build.gradle.kts`
Reads `.env` file and injects values into `BuildConfig` at build time.

### 4. `app/src/main/java/.../config/AppConfig.kt`
Type-safe abstraction for accessing configuration values. Injected via Hilt.

## Usage

### Setting Up Configuration

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` and add your keys:**
   ```bash
   PORCUPINE_ACCESS_KEY=your_key_here
   ```

3. **Rebuild the project:**
   ```bash
   ./gradlew clean build
   ```

### Using Configuration in Code

```kotlin
@AndroidEntryPoint
class MyService : Service() {
    
    @Inject
    lateinit var appConfig: AppConfig
    
    fun initialize() {
        val accessKey = appConfig.porcupineAccessKey
        // Use access key...
    }
}
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
       get() {
           val key = BuildConfig.NEW_API_KEY
           if (key.isBlank()) {
               throw IllegalStateException("NEW_API_KEY not configured")
           }
           return key
       }
   ```

## Benefits

✅ **Security**: API keys never committed to git  
✅ **Abstraction**: Easy to swap configuration sources  
✅ **Type Safety**: Compile-time validation  
✅ **Testability**: Easy to mock `AppConfig` in tests  
✅ **Flexibility**: Can switch to remote config, encrypted storage, etc.  
✅ **Developer Experience**: Clear error messages when keys missing  

## Security Notes

- `.env` file is gitignored
- Never commit `.env` to version control
- Use different keys for development and production
- Rotate keys regularly
- Consider using Android Keystore for production apps

## Troubleshooting

**Build fails with "PORCUPINE_ACCESS_KEY not found"**
- Ensure `.env` file exists in project root
- Check that key is set (no empty value)
- Rebuild: `./gradlew clean build`

**Configuration not updating**
- Sync Gradle files in Android Studio
- Clean and rebuild project

**Access key not working**
- Verify key is correct in `.env`
- Check for extra spaces or quotes
- Ensure key is valid at provider's console

