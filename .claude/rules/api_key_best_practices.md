## ✅ Best Practices for API Keys in Test Environments

### 🔒 1. **Never Hardcode API Keys in Code (Even for Testing)**

❌ Bad:

```kotlin
const val API_KEY = "my-secret-api-key" // Avoid this!
```

✅ Instead, **load them dynamically** from BuildConfig, local.properties, or mock configs.

---

### 📁 2. **Use `local.properties` or BuildConfig**

**Option A – BuildConfig (recommended for non-secrets):**

In `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com\"")
    }
}
// For secrets: use local.properties and never commit real values
```

**Option B – `local.properties` (for secrets, not committed):**

```
# local.properties (add to .gitignore if it contains secrets)
API_KEY=your-api-key-here
```

Read in code:

```kotlin
// In a module or Application, load from local.properties or use BuildConfig
val props = Properties().apply {
    rootProject.file("local.properties").inputStream().use { load(it) }
}
val apiKey = props.getProperty("API_KEY", "")
```

👉 **Do not commit** `local.properties` if it contains sensitive data. Add to `.gitignore` when needed.

---

### 🧪 3. **Mock API Keys in Unit/Instrumentation Tests**

Use fake or mock implementations that **don’t require real keys**.

```kotlin
class FakeApiService(private val apiKey: String = "test-api-key") : ApiService {
    override suspend fun fetchData(): Result<String> = Result.success("mock response")
}
```

Pass dummy keys in tests:

```kotlin
val mockService = ApiRepository(apiKey = "test-api-key")
```

### 🔧 4. **Inject API Keys via Constructor or Dependency Injection**

This allows you to:

* Swap real vs. mock keys easily
* Keep test files clean and controlled

```kotlin
class ApiRepository @Inject constructor(
    @Named("ApiKey") private val apiKey: String
) { ... }
```

In tests:

```kotlin
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ApiModule::class])
object FakeApiModule {
    @Provides
    @Named("ApiKey")
    fun provideApiKey(): String = "mock-test-key"
}
```

---

### 🤫 5. **Use GitHub Actions Secrets (For CI Tests)**

If your CI (e.g., GitHub Actions) needs a real API key for integration testing:

* Store it in **GitHub Secrets**
* Inject it into the build (e.g., Gradle project properties or env vars)

```yaml
env:
  API_KEY: ${{ secrets.TEST_API_KEY }}
```

---

### 📌 Summary of Best Practices

| Practice                           | Why It’s Good                        |
| ---------------------------------- | ------------------------------------ |
| ❌ Don't hardcode in source code    | Avoid security leaks                 |
| ✅ Use BuildConfig / local.properties | Easy to manage multiple environments |
| ✅ Use mock services with fake keys | Safe, fast, reliable tests            |
| ✅ Inject keys via constructor/DI   | Makes testing and switching easier   |
| ✅ Use secrets in CI/CD             | Protect real keys in pipelines       |

---
