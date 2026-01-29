# Nova Android Assistant - Architecture Documentation

## Overview

Nova follows **feature-based Clean Architecture** principles to ensure:
- **Scalability**: Easy to add new features
- **Maintainability**: Clear separation of concerns
- **Testability**: Domain logic isolated from framework dependencies
- **Independence**: Features can be developed and tested in isolation

## Architecture Principles

### Clean Architecture Layers

Each feature is organized into three layers:

1. **Domain Layer** (Pure Kotlin)
   - Business logic and domain models
   - Repository interfaces
   - Use cases
   - **NO Android framework dependencies**

2. **Data Layer**
   - Repository implementations
   - Data sources (local, remote, service)
   - DTOs with mapping to domain models
   - **Bridges domain and framework**

3. **Presentation Layer**
   - ViewModels
   - Composables (UI)
   - State management
   - **UI only - no business logic**

### Dependency Rule

Dependencies flow **inward**:
```
Presentation → Domain ← Data
```

- **Domain** has no dependencies on other layers
- **Data** implements domain interfaces
- **Presentation** depends on domain (use cases, models)

## Project Structure

```
novaassistant/
├── MainActivity.kt                     # App entry point
├── NovaApplication.kt                  # Application class
│
├── di/                                 # Dependency Injection (Root Level)
│   ├── AppModule.kt                   # Application-wide dependencies
│   ├── WakeWordModule.kt              # Wake word DI bindings
│   ├── SettingsModule.kt              # Settings DI bindings
│   └── AudioModule.kt                 # Audio DI bindings
│
├── core/                               # Shared Infrastructure
│   ├── config/                        # App configuration
│   │   └── AppConfig.kt
│   ├── navigation/                    # Navigation graph
│   │   └── NavGraph.kt
│   ├── theme/                         # Material Design theme
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── ui/                            # Shared UI components
│   │   ├── components/                # Reusable Composables
│   │   │   └── PermissionHandler.kt
│   │   └── preview/                   # Preview utilities
│   │       ├── PreviewConfiguration.kt
│   │       └── PreviewParameterProviders.kt
│   └── util/                          # Shared utilities
│       └── WakeWordLogger.kt
│
└── features/                           # All Features
    │
    ├── wakeword/                      # Wake Word Detection Feature
    │   ├── domain/                    # Pure Kotlin - NO Android imports
    │   │   ├── model/
    │   │   │   ├── WakeWordEvent.kt   # Immutable data class
    │   │   │   └── ServiceStatus.kt   # Immutable data class
    │   │   ├── repository/
    │   │   │   └── WakeWordRepository.kt  # Interface
    │   │   ├── WakeWordError.kt       # Sealed class for errors
    │   │   └── usecases/
    │   │       ├── StartWakeWordDetectionUseCase.kt
    │   │       ├── StopWakeWordDetectionUseCase.kt
    │   │       ├── ObserveWakeWordEventsUseCase.kt
    │   │       └── ObserveServiceStatusUseCase.kt
    │   │
    │   ├── data/
    │   │   ├── source/
    │   │   │   └── service/
    │   │   │       └── WakeWordService.kt  # Android Service
    │   │   ├── repository/
    │   │   │   └── WakeWordRepositoryImpl.kt
    │   │   └── constants/
    │   │       └── WakeWordConstants.kt
    │   │
    │   └── presentation/
    │       └── components/
    │           ├── WakeWordFeedback.kt  # Stateless Composable
    │           └── StatusIndicator.kt   # Stateless Composable
    │
    ├── settings/                      # Settings Management Feature
    │   ├── domain/
    │   │   ├── model/
    │   │   │   └── Settings.kt        # Immutable data class
    │   │   ├── repository/
    │   │   │   └── SettingsRepository.kt  # Interface
    │   │   ├── SettingsError.kt       # Sealed class
    │   │   └── usecases/
    │   │       ├── GetSettingsUseCase.kt
    │   │       └── UpdateSettingsUseCase.kt
    │   │
    │   ├── data/
    │   │   ├── source/
    │   │   │   └── local/
    │   │   │       └── SettingsDataStore.kt
    │   │   └── repository/
    │   │       └── SettingsRepositoryImpl.kt
    │   │
    │   └── presentation/
    │       ├── SettingsScreen.kt      # Composable
    │       └── SettingsViewModel.kt   # @HiltViewModel
    │
    ├── audio/                         # Audio Management Feature
    │   └── domain/
    │       ├── model/
    │       │   └── AudioDevice.kt     # Immutable data class
    │       ├── AudioError.kt          # Sealed class
    │       └── manager/
    │           ├── AudioDeviceManager.kt
    │           └── SoundPlayer.kt
    │
    └── assistant/                     # Main Assistant Feature
        ├── domain/
        │   └── model/
        │       └── AssistantState.kt  # Sealed class
        │
        └── presentation/
            └── main/
                ├── MainScreen.kt      # Composable
                ├── MainViewModel.kt   # @HiltViewModel
                ├── MainScreenRouter.kt
                └── components/
                    ├── CircularActionButton.kt
                    ├── ErrorOverlay.kt
                    ├── ModernOrb.kt
                    ├── StatusLabel.kt
                    └── TopControlsPill.kt
```

## Feature Deep Dive

### Wake Word Feature

**Responsibility**: Detect "Hey Nova" wake word using Porcupine

**Key Components**:
- `WakeWordService`: Android foreground service that runs Porcupine
- `WakeWordRepository`: Manages service lifecycle and events
- Use cases: Start/stop detection, observe events and status

**Dependencies**:
- Settings feature (sensitivity configuration)
- Audio feature (device routing, sound feedback)

**Data Flow**:
```
User → MainViewModel → StartWakeWordDetectionUseCase
                     → WakeWordRepository
                     → WakeWordService (Porcupine)
                     → Broadcasts WakeWordEvent
                     → Repository emits event
                     → ViewModel updates UI
```

### Settings Feature

**Responsibility**: Manage user preferences

**Key Components**:
- `SettingsDataStore`: Persists preferences using DataStore
- `SettingsRepository`: Provides settings access
- Use cases: Get/update settings

**Persistence**:
- Sensitivity level
- Audio feedback enabled/disabled

### Audio Feature

**Responsibility**: Audio device management and sound playback

**Key Components**:
- `AudioDeviceManager`: Detects and routes to Bluetooth/headphones
- `SoundPlayer`: Plays notification sounds

**Features**:
- Automatic Bluetooth device detection
- Audio routing priority (Bluetooth > Headphones > Speaker)
- Wake word detection sound feedback

### Assistant Feature

**Responsibility**: Main UI and user interaction

**Key Components**:
- `MainViewModel`: Central state management
- `MainScreen`: Main UI with orb visualization
- UI components: Orb, buttons, status labels

**State Management**:
```kotlin
data class MainState(
    val assistantState: AssistantState,
    val isServiceRunning: Boolean,
    val hasPermissions: Boolean
)

sealed class AssistantState {
    object Idle : AssistantState()
    object Listening : AssistantState()
    data class Detected(val keyword: String) : AssistantState()
}
```

## Cross-Feature Dependencies

Features communicate through **domain interfaces** only:

```
MainViewModel (assistant)
  ↓ depends on
  ├─ StartWakeWordDetectionUseCase (wakeword.domain)
  ├─ StopWakeWordDetectionUseCase (wakeword.domain)
  ├─ ObserveWakeWordEventsUseCase (wakeword.domain)
  ├─ GetSettingsUseCase (settings.domain)
  └─ UpdateSettingsUseCase (settings.domain)

WakeWordService (wakeword.data)
  ↓ depends on
  ├─ SettingsRepository (settings.domain)
  ├─ AudioDeviceManager (audio.domain)
  └─ SoundPlayer (audio.domain)
```

**Rules**:
- Features depend on **domain interfaces** from other features
- Features **NEVER** depend on data/presentation layers of other features
- All dependencies injected via Hilt

## Clean Architecture Rules

### Domain Layer ✓

**Pure Kotlin - NO Android imports**

```kotlin
// ✅ GOOD: Pure Kotlin model
data class WakeWordEvent(
    val keyword: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

// ✅ GOOD: Repository interface with Result
interface WakeWordRepository {
    suspend fun startDetection(): Result<Unit, WakeWordError>
    fun observeWakeWordEvents(): Flow<WakeWordEvent>
}

// ✅ GOOD: Sealed class for errors
sealed interface WakeWordError {
    data object ServiceNotAvailable : WakeWordError
    data object PermissionDenied : WakeWordError
    data class Unknown(val message: String) : WakeWordError
}

// ✅ GOOD: Use case with operator invoke
class StartWakeWordDetectionUseCase @Inject constructor(
    private val repository: WakeWordRepository
) {
    suspend operator fun invoke(): Result<Unit, WakeWordError> =
        repository.startDetection()
}

// ❌ BAD: Android import in domain
import android.content.Context  // Never in domain layer!
```

### Data Layer ✓

**Implements domain interfaces, manages data sources**

```kotlin
// ✅ GOOD: Repository implementation
class WakeWordRepositoryImpl @Inject constructor(
    private val service: WakeWordService
) : WakeWordRepository {
    override suspend fun startDetection(): Result<Unit, WakeWordError> =
        try {
            service.start()
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(WakeWordError.PermissionDenied)
        } catch (e: Exception) {
            Result.failure(WakeWordError.Unknown(e.message ?: ""))
        }
}

// ✅ GOOD: DTOs with toDomain() mapping (if using DTOs)
data class WakeWordEventDto(
    val keyword: String,
    val confidence: Float
) {
    fun toDomain(): WakeWordEvent = WakeWordEvent(
        keyword = keyword,
        confidence = confidence
    )
}
```

### Presentation Layer ✓

**UI only - observes state, delegates to use cases**

```kotlin
// ✅ GOOD: ViewModel with StateFlow
@HiltViewModel
class MainViewModel @Inject constructor(
    private val startWakeWordDetectionUseCase: StartWakeWordDetectionUseCase,
    private val observeWakeWordEventsUseCase: ObserveWakeWordEventsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun startDetection() {
        viewModelScope.launch {
            startWakeWordDetectionUseCase()
                .onSuccess { /* update state */ }
                .onFailure { error -> /* handle error */ }
        }
    }
}

// ✅ GOOD: Stateless Composable
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainScreenContent(
        state = state,
        onStartDetection = { viewModel.startDetection() }
    )
}
```

### Dependency Injection ✓

**All DI modules at root `di/` folder**

```kotlin
// ✅ GOOD: Feature DI module at root level
@Module
@InstallIn(SingletonComponent::class)
abstract class WakeWordModule {
    @Binds
    @Singleton
    abstract fun bindWakeWordRepository(
        impl: WakeWordRepositoryImpl
    ): WakeWordRepository
}
```

## State Management

### DataState Pattern

```kotlin
sealed class DataState<out T> {
    data object Initial : DataState<Nothing>()
    data object Loading : DataState<Nothing>()
    data class Success<T>(val value: T) : DataState<T>()
    data class Failure(val error: String) : DataState<Nothing>()
}

data class MainState(
    val dataState: DataState<AssistantState> = DataState.Initial,
    val isProcessing: Boolean = false
)
```

**Usage in Composables**:
```kotlin
when (val ds = state.dataState) {
    is DataState.Loading -> LoadingIndicator()
    is DataState.Failure -> ErrorContent(ds.error)
    is DataState.Success -> AssistantContent(ds.value)
    is DataState.Initial -> InitialContent()
}
```

## Testing Strategy

### Test Structure

Mirror `src/` in `test/`:
```
src/test/java/com/ctrlbsketr/novaassistant/
  └── features/
      ├── wakeword/
      │   ├── domain/usecases/StartWakeWordDetectionUseCaseTest.kt
      │   └── data/repository/WakeWordRepositoryImplTest.kt
      └── settings/
          └── presentation/SettingsViewModelTest.kt
```

### Use Fakes, Not Mocks

```kotlin
class FakeWakeWordRepository : WakeWordRepository {
    private val events = MutableSharedFlow<WakeWordEvent>()
    private var shouldSucceed = true

    fun setShouldSucceed(succeed: Boolean) {
        shouldSucceed = succeed
    }

    override suspend fun startDetection(): Result<Unit, WakeWordError> =
        if (shouldSucceed) Result.success(Unit)
        else Result.failure(WakeWordError.ServiceNotAvailable)

    override fun observeWakeWordEvents(): Flow<WakeWordEvent> = events
}
```

### ViewModel Testing

```kotlin
@Test
fun `when startDetection succeeds, state updates to listening`() = runTest {
    val fakeRepo = FakeWakeWordRepository()
    val useCase = StartWakeWordDetectionUseCase(fakeRepo)
    val viewModel = MainViewModel(useCase, ...)

    viewModel.startDetection()

    assertEquals(AssistantState.Listening, viewModel.state.value.assistantState)
}
```

## Adding New Features

### Template Structure

```
features/newfeature/
├── domain/
│   ├── model/
│   │   └── FeatureModel.kt         # Immutable data class
│   ├── repository/
│   │   └── FeatureRepository.kt    # Interface
│   ├── FeatureError.kt             # Sealed class
│   └── usecases/
│       └── GetFeatureUseCase.kt    # operator fun invoke()
│
├── data/
│   ├── dto/
│   │   └── FeatureDto.kt           # with toDomain()
│   ├── source/
│   │   └── local/remote/
│   └── repository/
│       └── FeatureRepositoryImpl.kt
│
└── presentation/
    ├── FeatureScreen.kt
    ├── FeatureViewModel.kt
    └── components/
```

**Plus DI module**:
```
di/FeatureModule.kt
```

### Steps

1. Create feature package structure
2. Define domain models and repository interface
3. Implement data layer (repository, data sources)
4. Create presentation layer (ViewModel, screens)
5. Add DI module at root level
6. Write tests mirroring src/ structure

## Best Practices

### DO ✓

- Keep domain layer pure Kotlin
- Use immutable data classes (`val` only)
- Return `Result<T, E>` from repositories
- Use sealed classes for errors
- Inject dependencies via constructor
- Use `StateFlow` for ViewModel state
- Keep Composables stateless
- Write tests with fakes

### DON'T ✗

- Add Android imports to domain layer
- Expose DTOs to domain or presentation
- Put business logic in ViewModels
- Use mutable state (`var`) in models
- Hardcode dependencies
- Put DI modules inside features
- Mock when you can fake
- Skip tests

## Performance Considerations

- Wake word service runs on background thread
- Audio processing isolated from UI
- StateFlow ensures efficient UI updates
- Lazy initialization of heavy dependencies

## Security

- No API keys in code (use `local.properties`)
- Audio data processed locally
- No network requests (offline-first)

## Future Enhancements

Potential features to add:

- `features/conversation/`: Chat history management
- `features/voice/`: Speech-to-text, text-to-speech
- `features/llm/`: Local or cloud LLM integration
- `features/actions/`: Timers, reminders, smart home
- `features/onboarding/`: First-time setup flow

Each follows the same Clean Architecture template.

## Resources

- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
