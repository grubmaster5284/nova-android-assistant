# Nova Android Assistant - Clean Architecture Restructuring Plan

## Overview
Restructure the Nova Android Assistant codebase from layer-based to feature-based organization while maintaining Clean Architecture principles. This will make the app easier to scale and manage as new features are added.

**Approach**: Incremental migration (feature-by-feature over multiple PRs)
**Scope**: Package restructuring only (no separate Gradle modules)
**Timeline**: ~2-3 weeks with 6 phases

---

## Current Structure (Layer-Based)
```
novaassistant/
├── data/          # All data sources and repositories
├── domain/        # All business logic (models, use cases, repository interfaces)
├── presentation/  # All UI (ViewModels, Composables, theme)
├── di/            # Dependency injection modules
├── config/        # App configuration
└── util/          # Utilities
```

**Problem**: As features grow, files are scattered across layers making it hard to:
- Understand feature boundaries
- Work on features independently
- Test features in isolation
- Onboard new developers

---

## Target Structure (Feature-Based) - Following Clean Architecture Rules

```
novaassistant/
├── MainActivity.kt
├── NovaApplication.kt
│
├── di/                               # Root-level DI modules
│   ├── AppModule.kt                 # Application-wide dependencies
│   ├── WakeWordModule.kt            # Wake word feature bindings
│   ├── SettingsModule.kt            # Settings feature bindings
│   ├── AudioModule.kt               # Audio feature bindings
│   └── AssistantModule.kt           # Assistant feature bindings
│
├── core/                             # Shared infrastructure
│   ├── config/                      # App configuration
│   ├── navigation/                  # App navigation graph
│   ├── theme/                       # Material theme (Color, Theme, Type)
│   ├── ui/                          # Shared UI components
│   │   ├── components/              # Reusable Composables
│   │   └── preview/                 # Preview utilities
│   └── util/                        # Shared utilities
│
└── features/                         # All features (Clean Architecture per feature)
    │
    ├── wakeword/                    # Wake Word Detection Feature
    │   ├── domain/                  # Pure Kotlin - NO Android/Hilt/HTTP imports
    │   │   ├── model/
    │   │   │   ├── WakeWordEvent.kt         # Immutable data class
    │   │   │   └── ServiceStatus.kt         # Immutable data class
    │   │   ├── repository/
    │   │   │   └── WakeWordRepository.kt    # Interface with suspend + Result<T,E>
    │   │   ├── WakeWordError.kt             # Sealed class for errors
    │   │   └── usecases/
    │   │       ├── StartWakeWordDetectionUseCase.kt
    │   │       ├── StopWakeWordDetectionUseCase.kt
    │   │       ├── ObserveWakeWordEventsUseCase.kt
    │   │       └── ObserveServiceStatusUseCase.kt
    │   │
    │   ├── data/
    │   │   ├── dto/                         # DTOs with toDomain() mapping
    │   │   │   └── (none currently - service uses domain models directly)
    │   │   ├── source/
    │   │   │   ├── service/                 # Android Service
    │   │   │   │   └── WakeWordService.kt
    │   │   │   └── local/                   # Local data sources if needed
    │   │   ├── repository/
    │   │   │   └── WakeWordRepositoryImpl.kt # Maps DTOs → Domain, errors → domain errors
    │   │   └── constants/
    │   │       └── WakeWordConstants.kt     # Endpoint/config constants
    │   │
    │   └── presentation/
    │       ├── components/
    │       │   ├── WakeWordFeedback.kt      # Stateless Composable
    │       │   └── StatusIndicator.kt       # Stateless Composable
    │       └── (No ViewModel - state managed by MainViewModel)
    │
    ├── settings/                    # Settings Management Feature
    │   ├── domain/                  # Pure Kotlin
    │   │   ├── model/
    │   │   │   └── Settings.kt              # Immutable data class with val
    │   │   ├── repository/
    │   │   │   └── SettingsRepository.kt    # Interface
    │   │   ├── SettingsError.kt             # Sealed class
    │   │   └── usecases/
    │   │       ├── GetSettingsUseCase.kt    # operator fun invoke()
    │   │       └── UpdateSettingsUseCase.kt
    │   │
    │   ├── data/
    │   │   ├── dto/
    │   │   │   └── (DataStore uses domain model directly)
    │   │   ├── source/
    │   │   │   └── local/
    │   │   │       └── SettingsDataStore.kt
    │   │   └── repository/
    │   │       └── SettingsRepositoryImpl.kt
    │   │
    │   └── presentation/
    │       ├── SettingsScreen.kt            # Composable with hiltViewModel()
    │       └── SettingsViewModel.kt         # @HiltViewModel with StateFlow<SettingsState>
    │
    ├── audio/                       # Audio Management Feature
    │   ├── domain/
    │   │   ├── model/
    │   │   │   └── AudioDevice.kt           # Immutable data class
    │   │   ├── AudioError.kt                # Sealed class
    │   │   └── manager/
    │   │       ├── AudioDeviceManager.kt    # Domain service/manager
    │   │       └── SoundPlayer.kt           # Domain service
    │   └── (No data layer - managers access Android APIs directly)
    │
    └── assistant/                   # Main Assistant Feature
        ├── domain/
        │   ├── model/
        │   │   └── AssistantState.kt        # Sealed class for state machine
        │   └── AssistantError.kt            # Sealed class
        │
        └── presentation/
            ├── main/
            │   ├── MainScreen.kt            # Composable observing ViewModel
            │   ├── MainViewModel.kt         # @HiltViewModel with StateFlow<MainState>
            │   ├── MainScreenRouter.kt
            │   └── components/
            │       ├── CircularActionButton.kt
            │       ├── ErrorOverlay.kt
            │       ├── ModernOrb.kt
            │       ├── StatusLabel.kt
            │       └── TopControlsPill.kt
            └── MainScreenImplementation.kt
```

**Key Architecture Principles** (from your `.claude/rules`):
1. **Domain Layer**: Pure Kotlin - NO Android, Hilt, or HTTP imports
2. **Data Layer**: Never expose DTOs to domain or UI - map in repository
3. **Presentation Layer**: UI only - no business logic, observe state via `collectAsStateWithLifecycle()`
4. **State Management**: `StateFlow<UiState>` with `DataState<T>` sealed class (Loading/Success/Failure)
5. **Repository Pattern**: Interfaces return `Result<T, E>` or sealed outcomes
6. **Use Cases**: Single responsibility with `operator fun invoke()`
7. **Error Handling**: Sealed classes for type-safe domain errors

---

## Feature Identification

| Feature | Responsibility | Key Files |
|---------|---------------|-----------|
| **Wake Word** | Porcupine integration, wake word detection service | `WakeWordService.kt` (532 lines), 4 use cases |
| **Settings** | User preferences (DataStore), sensitivity/feedback toggles | `SettingsDataStore.kt`, `SettingsScreen.kt` |
| **Audio** | Audio device routing (Bluetooth/headphones), sound feedback | `AudioDeviceManager.kt` (484 lines), `SoundPlayer.kt` |
| **Assistant** | Main UI, assistant state management, visual feedback | `MainViewModel.kt`, `MainScreen.kt` |

**Future Features** (easy to add):
- `features/conversation/` - Chat history, conversation management
- `features/voice/` - Speech-to-text, text-to-speech
- `features/llm/` - LLM integration (local or cloud)
- `features/actions/` - Action execution (timers, reminders, etc.)
- `features/onboarding/` - First-time setup flow

---

## Migration Phases (Incremental)

### Phase 1: Core Infrastructure Setup
**Goal**: Create shared core module and restructure DI (following Clean Architecture rules)
**Risk**: Low (additive changes only)

**Steps**:
1. Create `core/` package structure (config, navigation, theme, ui, util)
2. Move theme files → `core/theme/`
3. Move `AppConfig.kt` → `core/config/`
4. Keep `di/` at root level (per Clean Architecture rules)
5. Update `di/AppModule.kt` to provide core utilities
6. Move shared UI components → `core/ui/components/` (`PermissionHandler.kt`)
7. Move preview utilities → `core/ui/preview/`
8. Create `core/navigation/NavGraph.kt` (extract from MainActivity)

**Files to Move**:
- `presentation/theme/*.kt` (3 files) → `core/theme/`
- `config/AppConfig.kt` → `core/config/`
- `presentation/components/PermissionHandler.kt` → `core/ui/components/`
- `presentation/components/PreviewConfiguration.kt` → `core/ui/preview/`
- `presentation/components/PreviewParameterProviders.kt` → `core/ui/preview/`
- `di/AppModule.kt` → Keep at root, update imports only

**DI Changes**:
- **Keep** `di/` at root level (not inside `core/`)
- `di/AppModule.kt` provides `@ApplicationContext` and core singletons
- Feature modules will be added in later phases

**Testing**: App builds, launches, theme applies, navigation works

---

### Phase 2: Audio Feature Migration
**Goal**: Create audio feature module (foundation for wake word)
**Risk**: Low (utility classes with few dependents)

**Steps**:
1. Create `features/audio/domain/` structure
2. Create `features/audio/domain/model/AudioDevice.kt` (extract from AudioDeviceManager)
3. Create `features/audio/domain/AudioError.kt` (sealed class)
4. Move `AudioDeviceManager.kt` → `features/audio/domain/manager/`
5. Move `SoundPlayer.kt` → `features/audio/domain/manager/`
6. Create `di/AudioModule.kt` (at root level)
7. Update imports in moved files

**Files to Create/Move**:
- `features/audio/domain/model/AudioDevice.kt` (NEW - extract device info)
- `features/audio/domain/AudioError.kt` (NEW - sealed class)
- `util/AudioDeviceManager.kt` → `features/audio/domain/manager/`
- `util/SoundPlayer.kt` → `features/audio/domain/manager/`

**DI Module**:
- Create `di/AudioModule.kt` at root level
- Provides `AudioDeviceManager` and `SoundPlayer` as singletons

**Testing**: Build succeeds, audio device switching works, sounds play

---

### Phase 3: Settings Feature Migration
**Goal**: Migrate settings feature completely (following Clean Architecture rules)
**Risk**: Medium (cross-feature dependency with wake word service)

**Steps**:
1. Create `features/settings/` structure (domain/data/presentation)
2. Create domain layer:
   - `domain/model/Settings.kt` (immutable data class with val)
   - `domain/repository/SettingsRepository.kt` (interface with suspend + Result<T,E>)
   - `domain/SettingsError.kt` (sealed class)
   - `domain/usecases/` folder (plural)
3. Create data layer:
   - `data/source/local/` folder
   - `data/repository/` folder
4. Move files and update imports
5. Create `di/SettingsModule.kt` at root level
6. Optional: Create SettingsViewModel if needed

**Files to Move**:
- `domain/models/Settings.kt` → `features/settings/domain/model/Settings.kt`
- `domain/repository/SettingsRepository.kt` → `features/settings/domain/repository/SettingsRepository.kt`
- `domain/usecases/GetSettingsUseCase.kt` → `features/settings/domain/usecases/GetSettingsUseCase.kt`
- `domain/usecases/UpdateSettingsUseCase.kt` → `features/settings/domain/usecases/UpdateSettingsUseCase.kt`
- `data/local/SettingsDataStore.kt` → `features/settings/data/source/local/SettingsDataStore.kt`
- `data/repository/SettingsRepositoryImpl.kt` → `features/settings/data/repository/SettingsRepositoryImpl.kt`
- `presentation/settings/SettingsScreen.kt` → `features/settings/presentation/SettingsScreen.kt`

**Files to Create**:
- `features/settings/domain/SettingsError.kt` (NEW - sealed class for errors)
- `di/SettingsModule.kt` (at root level - binds SettingsRepository)

**DI Changes**:
- Create `di/SettingsModule.kt` with `@Binds` for repository
- Remove settings bindings from old `di/DataModule.kt`

**Testing**: Settings screen opens, preferences persist, changes reflect in UI

---

### Phase 4: Wake Word Feature Migration
**Goal**: Migrate wake word detection (highest risk phase - following Clean Architecture rules)
**Risk**: HIGH (foreground service with multiple cross-feature dependencies)

**Steps**:
1. Create `features/wakeword/` structure with proper data layer folders
2. Create domain layer:
   - `domain/model/` folder
   - `domain/repository/` folder
   - `domain/WakeWordError.kt` (sealed class)
   - `domain/usecases/` folder (plural)
3. Create data layer with Clean Architecture structure:
   - `data/source/service/` folder (Android Service)
   - `data/repository/` folder
   - `data/constants/` folder (for Porcupine config)
4. Move files and update all imports
5. **CRITICAL**: Update `WakeWordService.kt` imports:
   - `SettingsRepository` from `features.settings.domain.repository`
   - `AudioDeviceManager` from `features.audio.domain.manager`
   - `SoundPlayer` from `features.audio.domain.manager`
6. **CRITICAL**: Update `AndroidManifest.xml` service path:
   ```xml
   <service
       android:name=".features.wakeword.data.source.service.WakeWordService"
       ...
   />
   ```
7. Create `di/WakeWordModule.kt` at root level
8. Move presentation components

**Files to Move** (with corrected paths):

**Domain Layer** (Pure Kotlin - NO Android imports):
- `domain/models/WakeWordEvent.kt` → `features/wakeword/domain/model/WakeWordEvent.kt`
- `domain/models/ServiceStatus.kt` → `features/wakeword/domain/model/ServiceStatus.kt`
- `domain/repository/WakeWordRepository.kt` → `features/wakeword/domain/repository/WakeWordRepository.kt`
- `domain/usecases/StartWakeWordDetectionUseCase.kt` → `features/wakeword/domain/usecases/StartWakeWordDetectionUseCase.kt`
- `domain/usecases/StopWakeWordDetectionUseCase.kt` → `features/wakeword/domain/usecases/StopWakeWordDetectionUseCase.kt`
- `domain/usecases/ObserveWakeWordEventsUseCase.kt` → `features/wakeword/domain/usecases/ObserveWakeWordEventsUseCase.kt`
- `domain/usecases/ObserveServiceStatusUseCase.kt` → `features/wakeword/domain/usecases/ObserveServiceStatusUseCase.kt`

**Data Layer**:
- `data/service/WakeWordService.kt` → `features/wakeword/data/source/service/WakeWordService.kt`
- `data/repository/WakeWordRepositoryImpl.kt` → `features/wakeword/data/repository/WakeWordRepositoryImpl.kt`

**Presentation Layer**:
- `presentation/components/WakeWordFeedback.kt` → `features/wakeword/presentation/components/WakeWordFeedback.kt`
- `presentation/components/StatusIndicator.kt` → `features/wakeword/presentation/components/StatusIndicator.kt`

**Files to Create**:
- `features/wakeword/domain/WakeWordError.kt` (NEW - sealed class for errors)
- `features/wakeword/data/constants/WakeWordConstants.kt` (NEW - Porcupine config)
- `di/WakeWordModule.kt` (at root level - binds WakeWordRepository)

**Critical Files** (require careful import updates):
- `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/data/service/WakeWordService.kt`
- `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/AndroidManifest.xml`

**DI Changes**:
- Create `di/WakeWordModule.kt` with `@Binds` for repository
- Provide all use cases (or use `@Inject` constructor)
- Remove wake word bindings from old `di/DataModule.kt`

**Testing**:
- Service starts/stops correctly
- Wake word detection works
- Foreground notification appears
- Audio feedback plays (cross-feature dependency)
- Status indicators update
- Error broadcasts reach MainActivity
- Settings changes affect service behavior (cross-feature dependency)

---

### Phase 5: Assistant Feature Migration
**Goal**: Migrate main assistant UI and ViewModel
**Risk**: Medium (complex ViewModel with 6 use case dependencies)

**Steps**:
1. Create `features/assistant/` structure
2. Move `AssistantState.kt` model
3. Delete duplicate `domain/model/AssistantState.kt`
4. Move Main presentation (Screen, ViewModel, Router, components)
5. Update `MainViewModel.kt` imports for all cross-feature use cases
6. Update `MainActivity.kt` to import from new location

**Files to Move**:
- `domain/models/AssistantState.kt` → `features/assistant/domain/model/`
- `presentation/main/MainScreen.kt` → `features/assistant/presentation/main/`
- `presentation/main/MainViewModel.kt` → `features/assistant/presentation/main/`
- `presentation/MainScreenRouter.kt` → `features/assistant/presentation/main/`
- `presentation/MainScreenImplementation.kt` → `features/assistant/presentation/`
- `presentation/main/components/*.kt` (5 files) → `features/assistant/presentation/main/components/`

**Critical Files**:
- `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/presentation/main/MainViewModel.kt`
- `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/MainActivity.kt`

**Testing**: Full app integration test (permission → service → detection → UI feedback)

---

### Phase 6: Cleanup & Documentation
**Goal**: Remove old structure, optimize imports, document new architecture
**Risk**: Low (verification phase)

**Steps**:
1. Verify all old directories are empty
2. Delete empty directories: `data/`, `domain/`, `presentation/`, `di/`, `config/`, `util/`
3. Delete old `di/DataModule.kt`
4. Run "Optimize Imports" across entire project
5. Delete duplicate `domain/model/AssistantState.kt` if not done in Phase 5
6. Update README with new structure
7. Create architecture documentation

**Testing**: Full regression test of all features

---

## Dependency Injection Reorganization (Following Clean Architecture Rules)

### Current DI Structure
```kotlin
// di/AppModule.kt - Provides Context
// di/DataModule.kt - Binds all repositories
```

### New DI Structure (Feature-Based, Root-Level Modules)

All DI modules remain at **root level** (`di/` folder), NOT nested in features or core. This follows the Clean Architecture convention where DI wiring is separate from feature code.

#### `di/AppModule.kt` (Updated)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = AppConfig()
}
```

#### `di/WakeWordModule.kt` (NEW)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class WakeWordModule {

    @Binds
    @Singleton
    abstract fun bindWakeWordRepository(
        impl: WakeWordRepositoryImpl
    ): WakeWordRepository

    // Use cases can use @Inject constructor and be auto-provided by Hilt
    // Or explicitly provide them here if they need custom logic
    companion object {
        @Provides
        @Singleton
        fun provideStartWakeWordDetectionUseCase(
            repository: WakeWordRepository
        ): StartWakeWordDetectionUseCase = StartWakeWordDetectionUseCase(repository)

        @Provides
        @Singleton
        fun provideStopWakeWordDetectionUseCase(
            repository: WakeWordRepository
        ): StopWakeWordDetectionUseCase = StopWakeWordDetectionUseCase(repository)

        @Provides
        @Singleton
        fun provideObserveWakeWordEventsUseCase(
            repository: WakeWordRepository
        ): ObserveWakeWordEventsUseCase = ObserveWakeWordEventsUseCase(repository)

        @Provides
        @Singleton
        fun provideObserveServiceStatusUseCase(
            repository: WakeWordRepository
        ): ObserveServiceStatusUseCase = ObserveServiceStatusUseCase(repository)
    }
}
```

#### `di/SettingsModule.kt` (NEW)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSettingsDataStore(
            @ApplicationContext context: Context
        ): SettingsDataStore = SettingsDataStore(context)

        // Use cases use @Inject constructor
    }
}
```

#### `di/AudioModule.kt` (NEW)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioDeviceManager(
        @ApplicationContext context: Context
    ): AudioDeviceManager = AudioDeviceManager(context)

    // SoundPlayer is an object singleton - no need to provide via DI
}
```

#### `di/AssistantModule.kt` (NEW - if needed)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AssistantModule {
    // Minimal or empty - MainViewModel auto-injected via @HiltViewModel
    // No repository needed for assistant feature (uses use cases from other features)
}
```

### Migration Strategy

**Order of Operations**:
1. Create new feature modules (WakeWordModule, SettingsModule, AudioModule)
2. Verify app builds with both old and new modules coexisting
3. Delete old `di/DataModule.kt` ONLY after all features migrated
4. Each DI module is created in its respective phase (Phase 2-5)

### Clean Architecture DI Rules

1. **Location**: All DI modules at root `di/` folder (NOT in features or core)
2. **Naming**: `{Feature}Module.kt` (e.g., `WakeWordModule.kt`)
3. **Bindings**: Use `@Binds` for repository interfaces
4. **Providers**: Use `@Provides` for concrete classes or factories
5. **Use Cases**: Can use `@Inject constructor` (auto-provided by Hilt) or explicit `@Provides`
6. **Scope**: Use `@Singleton` for repositories and managers; ViewModels use `@HiltViewModel`

**Migration**: Create new feature modules BEFORE deleting `di/DataModule.kt`

---

## Implementing Clean Architecture Patterns

During migration, ensure all code follows these Clean Architecture patterns from your `.claude/rules`:

### 1. Domain Layer Rules (Pure Kotlin)

**Models**:
```kotlin
// Immutable data class with val
data class WakeWordEvent(
    val keyword: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

// Value class for IDs (optional but recommended)
@JvmInline
value class SettingsId(val value: String) {
    init {
        require(value.isNotBlank()) { "Settings ID cannot be empty" }
    }
}
```

**Repository Interfaces**:
```kotlin
// Use suspend + Result<T, E> or Flow
interface WakeWordRepository {
    suspend fun startDetection(): Result<Unit, WakeWordError>
    suspend fun stopDetection(): Result<Unit, WakeWordError>
    fun observeWakeWordEvents(): Flow<WakeWordEvent>
    fun observeServiceStatus(): Flow<ServiceStatus>
}
```

**Error Handling** (Sealed Classes):
```kotlin
sealed interface WakeWordError {
    data object ServiceNotAvailable : WakeWordError
    data object PermissionDenied : WakeWordError
    data object PorcupineInitFailed : WakeWordError
    data class Unknown(val message: String) : WakeWordError
}
```

**Use Cases** (operator fun invoke):
```kotlin
class StartWakeWordDetectionUseCase @Inject constructor(
    private val repository: WakeWordRepository,
) {
    suspend operator fun invoke(): Result<Unit, WakeWordError> =
        repository.startDetection()
}
```

### 2. Data Layer Rules

**DTOs with toDomain()**:
```kotlin
// Only if using DTOs (not needed if using domain models directly)
data class WakeWordEventDto(
    val keyword: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = 0L,
) {
    fun toDomain(): WakeWordEvent = WakeWordEvent(
        keyword = keyword,
        confidence = confidence,
        timestamp = timestamp,
    )
}
```

**Repository Implementation** (DTO → Domain, Error Mapping):
```kotlin
class WakeWordRepositoryImpl @Inject constructor(
    private val service: WakeWordService,
) : WakeWordRepository {

    override suspend fun startDetection(): Result<Unit, WakeWordError> =
        try {
            service.start()
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(WakeWordError.PermissionDenied)
        } catch (e: Exception) {
            Result.failure(WakeWordError.Unknown(e.message ?: "Unknown error"))
        }

    // ... other methods
}
```

### 3. Presentation Layer Rules

**State Management** (DataState sealed class):
```kotlin
data class MainState(
    val dataState: DataState<AssistantState> = DataState.Initial,
    val isProcessing: Boolean = false,
) {
    val isLoading: Boolean get() = dataState is DataState.Loading
    val hasError: Boolean get() = dataState is DataState.Failure
    val isSuccess: Boolean get() = dataState is DataState.Success
}

sealed class DataState<out T> {
    data object Initial : DataState<Nothing>()
    data object Loading : DataState<Nothing>()
    data class Success<T>(val value: T) : DataState<T>()
    data class Failure(val error: String) : DataState<Nothing>()
}
```

**ViewModel** (@HiltViewModel + StateFlow):
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val startWakeWordDetectionUseCase: StartWakeWordDetectionUseCase,
    private val observeWakeWordEventsUseCase: ObserveWakeWordEventsUseCase,
    // ... other use cases
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun startWakeWordDetection() {
        viewModelScope.launch {
            _state.update { it.copy(dataState = DataState.Loading) }
            startWakeWordDetectionUseCase()
                .onSuccess {
                    _state.update { it.copy(dataState = DataState.Success(AssistantState.Listening)) }
                }
                .onFailure { error ->
                    _state.update { it.copy(dataState = DataState.Failure(error.toString())) }
                }
        }
    }
}
```

**Screen** (Composable with hiltViewModel):
```kotlin
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainScreenContent(
        state = state,
        onStartDetection = { viewModel.startWakeWordDetection() },
    )
}

@Composable
private fun MainScreenContent(
    state: MainState,
    onStartDetection: () -> Unit,
) {
    when (val ds = state.dataState) {
        is DataState.Loading -> LoadingIndicator()
        is DataState.Failure -> ErrorContent(message = ds.error, onRetry = onStartDetection)
        is DataState.Success -> AssistantContent(state = ds.value)
        is DataState.Initial -> InitialContent(onStart = onStartDetection)
    }
}
```

### 4. Testing Rules

**Mirror `src/` in `test/`**:
```
src/test/java/com/ctrlbsketr/novaassistant/
  └── features/
      ├── wakeword/
      │   ├── domain/usecases/StartWakeWordDetectionUseCaseTest.kt
      │   └── data/repository/WakeWordRepositoryImplTest.kt
      └── settings/
          └── presentation/SettingsViewModelTest.kt
```

**Use Fakes** (not mocks) for repositories:
```kotlin
class FakeWakeWordRepository : WakeWordRepository {
    private var shouldSucceed = true

    fun setShouldSucceed(succeed: Boolean) { shouldSucceed = succeed }

    override suspend fun startDetection(): Result<Unit, WakeWordError> =
        if (shouldSucceed) Result.success(Unit)
        else Result.failure(WakeWordError.ServiceNotAvailable)
}
```

## Cross-Feature Dependencies

### Dependency Rules
1. Features can depend on **domain interfaces** from other features
2. Features **NEVER** depend on data implementations from other features
3. All cross-feature dependencies go through Hilt injection

### Key Dependencies
```
MainViewModel (assistant feature)
  ↓ depends on
  ├─ StartWakeWordDetectionUseCase (wakeword feature)
  ├─ StopWakeWordDetectionUseCase (wakeword feature)
  ├─ ObserveWakeWordEventsUseCase (wakeword feature)
  ├─ ObserveServiceStatusUseCase (wakeword feature)
  ├─ GetSettingsUseCase (settings feature)
  └─ UpdateSettingsUseCase (settings feature)

WakeWordService (wakeword feature)
  ↓ depends on
  ├─ SettingsRepository (settings feature domain interface)
  ├─ AudioDeviceManager (audio feature)
  └─ SoundPlayer (audio feature)
```

**Implementation**: Use `@Inject` constructor parameters with domain interfaces

---

## Adding New Features (Future)

### Template for New Feature (Following Clean Architecture Rules)

```
features/newfeature/
├── domain/                          # Pure Kotlin - NO Android/Hilt/HTTP imports
│   ├── model/
│   │   ├── FeatureModel.kt         # Immutable data class with val
│   │   └── FeatureId.kt            # Optional: value class for IDs
│   ├── repository/
│   │   └── FeatureRepository.kt    # Interface with suspend + Result<T,E>
│   ├── FeatureError.kt             # Sealed class/interface for errors
│   └── usecases/                   # Plural
│       ├── GetFeatureUseCase.kt    # operator fun invoke()
│       └── UpdateFeatureUseCase.kt
│
├── data/
│   ├── dto/
│   │   └── FeatureDto.kt           # DTOs with toDomain() mapping
│   ├── source/
│   │   ├── remote/
│   │   │   └── FeatureRemoteDataSource.kt
│   │   └── local/
│   │       └── FeatureLocalDataSource.kt
│   ├── repository/
│   │   └── FeatureRepositoryImpl.kt # Maps DTOs → Domain, errors → domain errors
│   └── constants/
│       └── FeatureApiConstants.kt   # Endpoints, paths (no secrets)
│
└── presentation/
    ├── FeatureScreen.kt            # Composable with hiltViewModel()
    ├── FeatureViewModel.kt         # @HiltViewModel with StateFlow<FeatureState>
    └── components/
        └── FeatureComponent.kt     # Stateless Composables
```

**Plus DI Module at Root**:
```
di/
└── FeatureModule.kt                # @Binds repository, @Provides use cases
```

### Steps to Add New Feature (Following Rules)

1. **Create domain layer** (Pure Kotlin):
   - Immutable models with `val`
   - Repository interface with `suspend` and `Result<T, E>`
   - Sealed class for errors (`FeatureError`)
   - Use cases with `operator fun invoke()`

2. **Create data layer**:
   - DTOs with `toDomain()` mapping
   - Data sources in `source/remote/` and `source/local/`
   - Repository impl that maps DTOs → domain and data errors → domain errors
   - API constants (no secrets)

3. **Create presentation layer**:
   - ViewModel with `@HiltViewModel` and `StateFlow<FeatureState>`
   - State class with `DataState<T>` sealed class
   - Screen with `hiltViewModel()` and `collectAsStateWithLifecycle()`
   - Stateless Composables in components/

4. **Create DI module** at root `di/`:
   - Use `@Binds` for repository interface
   - Use `@Provides` for use cases or let Hilt auto-provide via `@Inject constructor`

5. **Add navigation** in `core/navigation/NavGraph.kt`

6. **Write tests** mirroring `src/` structure in `test/`

### Example: Adding "Conversation History" Feature

```
features/conversation/
├── domain/                          # Pure Kotlin
│   ├── model/
│   │   ├── Conversation.kt         # data class with val
│   │   ├── Message.kt              # data class with val
│   │   └── ConversationId.kt       # @JvmInline value class
│   ├── repository/
│   │   └── ConversationRepository.kt # suspend + Result<T, ConversationError>
│   ├── ConversationError.kt        # sealed interface
│   └── usecases/
│       ├── GetConversationsUseCase.kt    # operator fun invoke()
│       ├── SaveMessageUseCase.kt
│       └── ClearHistoryUseCase.kt
│
├── data/
│   ├── dto/
│   │   ├── ConversationDto.kt      # with toDomain()
│   │   └── MessageDto.kt           # with toDomain()
│   ├── source/
│   │   └── local/
│   │       └── ConversationDatabase.kt # Room DB
│   └── repository/
│       └── ConversationRepositoryImpl.kt # Maps DTOs, errors
│
└── presentation/
    ├── ConversationListScreen.kt   # @Composable with hiltViewModel()
    ├── ConversationViewModel.kt    # @HiltViewModel, StateFlow<ConversationState>
    └── components/
        ├── MessageBubble.kt        # Stateless Composable
        └── ConversationCard.kt     # Stateless Composable
```

**Plus**:
```
di/ConversationModule.kt            # Root-level DI module
```

**State Management Example**:
```kotlin
data class ConversationState(
    val dataState: DataState<List<Conversation>> = DataState.Initial,
    val isRefreshing: Boolean = false,
)

sealed class DataState<out T> {
    data object Initial : DataState<Nothing>()
    data object Loading : DataState<Nothing>()
    data class Success<T>(val value: T) : DataState<T>()
    data class Failure(val error: String) : DataState<Nothing>()
}
```

---

## Testing Strategy

### Per-Phase Testing

#### Phase 1 (Core):
- [ ] App builds and launches
- [ ] Theme applies correctly
- [ ] Navigation works (Main ↔ Settings)
- [ ] Permissions dialog appears

#### Phase 2 (Audio):
- [ ] AudioDeviceManager initializes
- [ ] Sound feedback plays
- [ ] Bluetooth device switching works

#### Phase 3 (Settings):
- [ ] Settings screen opens
- [ ] Preferences persist in DataStore
- [ ] Settings changes reflect immediately

#### Phase 4 (Wake Word):
- [ ] Service starts/stops
- [ ] Wake word detection works
- [ ] Foreground notification appears
- [ ] Audio feedback on detection
- [ ] Error broadcasts received

#### Phase 5 (Assistant):
- [ ] Main screen displays correctly
- [ ] All UI components render
- [ ] ViewModel state updates
- [ ] Orb animations work

#### Phase 6 (Integration):
- [ ] End-to-end flow: launch → permission → start service → detect wake word → UI feedback
- [ ] Settings changes affect service behavior
- [ ] Navigation between all screens
- [ ] Service survives app backgrounding

### Automated Testing
- **Unit tests**: Test use cases and ViewModels with mocked repositories
- **Integration tests**: Test cross-feature dependencies with Hilt
- **UI tests**: Test user flows with Espresso/Compose Testing

---

## Git Strategy (Feature Branches)

```
main
  ├─ feature/restructure-1-core
  │   └─ PR #1: Core infrastructure setup
  ├─ feature/restructure-2-audio
  │   └─ PR #2: Audio feature migration
  ├─ feature/restructure-3-settings
  │   └─ PR #3: Settings feature migration
  ├─ feature/restructure-4-wakeword
  │   └─ PR #4: Wake word feature migration (HIGH RISK - thorough testing)
  ├─ feature/restructure-5-assistant
  │   └─ PR #5: Assistant feature migration
  └─ feature/restructure-6-cleanup
      └─ PR #6: Cleanup and documentation
```

**Commit Strategy**: Small, atomic commits within each PR for easy rollback

---

## Risk Mitigation

### High-Risk Areas

1. **WakeWordService Path Change (Phase 4)**
   - **Risk**: Service won't start if manifest path is wrong
   - **Mitigation**: Update manifest immediately, test on device
   - **Rollback**: Keep old service path in manifest during testing

2. **MainViewModel Dependencies (Phase 5)**
   - **Risk**: Missing use case imports break ViewModel
   - **Mitigation**: Update all 6 use case imports carefully, use IDE refactoring
   - **Rollback**: Revert commit, fix imports

3. **Hilt DI Conflicts**
   - **Risk**: Multiple modules provide same dependency
   - **Mitigation**: Delete old DI module immediately after creating new ones
   - **Rollback**: Git revert specific module

### Rollback Plan
- Each phase is a separate PR - can revert individual PRs
- Test thoroughly before merging each phase
- Keep feature flags if deploying to production during migration

---

## Benefits of This Structure

### Scalability
- **Add features easily**: New feature = new package under `features/`
- **Clear boundaries**: Each feature is self-contained with data/domain/presentation
- **Parallel development**: Multiple developers can work on different features without conflicts

### Maintainability
- **Reduced merge conflicts**: Changes stay within feature boundaries
- **Easier navigation**: All feature-related code in one place
- **Clear ownership**: Each feature can have a designated owner/team

### Testability
- **Feature isolation**: Test features independently
- **Mock dependencies**: Features depend on domain interfaces
- **Integration testing**: Test cross-feature interactions explicitly

### Future-Proofing
- **Multi-module ready**: Can easily convert to Gradle modules later
- **Dynamic features**: Potential for on-demand feature downloads
- **Modular architecture**: Aligns with modern Android best practices

---

## Verification Checklist (End-to-End)

After completing all phases, verify:

### Functional Testing
- [ ] App launches successfully
- [ ] Permission request works
- [ ] Wake word service starts/stops
- [ ] Wake word detection triggers UI feedback
- [ ] Audio feedback plays on detection
- [ ] Settings screen opens and saves changes
- [ ] Navigation between all screens works
- [ ] Service survives app backgrounding
- [ ] Bluetooth audio device switching works
- [ ] Error states display correctly

### Code Quality
- [ ] No compilation errors
- [ ] No unused imports
- [ ] All old layer-based directories deleted
- [ ] Hilt dependency graph builds correctly
- [ ] No circular dependencies between features
- [ ] Code follows package naming conventions

### Documentation
- [ ] README updated with new structure
- [ ] Architecture diagram created
- [ ] Feature addition guide documented
- [ ] PR descriptions include testing evidence

### Performance
- [ ] App startup time unchanged or improved
- [ ] No memory leaks (use LeakCanary)
- [ ] Service performance unchanged

---

## Critical Files Reference

These files require the most careful handling during migration:

1. **`WakeWordService.kt`** (532 lines)
   Path: `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/data/service/WakeWordService.kt`
   Risk: HIGH - Update imports for Settings, Audio, and update manifest path

2. **`MainViewModel.kt`**
   Path: `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/presentation/main/MainViewModel.kt`
   Risk: HIGH - Update 6 use case imports from different features

3. **`AndroidManifest.xml`**
   Path: `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/AndroidManifest.xml`
   Risk: HIGH - Must update service path or service won't start

4. **`MainActivity.kt`**
   Path: `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/MainActivity.kt`
   Risk: MEDIUM - Update imports for navigation and main screen

5. **`DataModule.kt`**
   Path: `/Users/satvikg/Downloads/Projects/Personal/Local_Assistant/nova_android_assistant/NovaAssistant/app/src/main/java/com/ctrlbsketr/novaassistant/di/DataModule.kt`
   Risk: MEDIUM - Must be replaced by feature DI modules correctly

---

## Summary

This restructuring transforms your app from a layer-based to a feature-based architecture, making it significantly easier to scale as you add new features (voice recognition, LLM integration, conversation history, actions, etc.).

The incremental migration approach (6 PRs over ~2-3 weeks) minimizes risk while maintaining a working app at each stage. The new structure follows Clean Architecture principles within each feature, providing clear boundaries and testability.

**Start with Phase 1 (Core)** - it's low-risk and sets the foundation for all subsequent phases.

---

## Clean Architecture Rules Compliance Checklist

During implementation, ensure ALL code follows these rules from `.claude/rules`:

### Domain Layer Rules ✓
- [ ] **Pure Kotlin**: NO Android, Hilt, or HTTP imports in domain layer
- [ ] **Immutable Models**: Use `data class` with `val` only
- [ ] **Repository Interfaces**: Use `suspend` functions and return `Result<T, E>` or Flow
- [ ] **Sealed Errors**: Define `{Feature}Error` as sealed class/interface
- [ ] **Use Cases**: Single responsibility with `operator fun invoke()`
- [ ] **Naming**: Models end in `Model`, repositories in `Repository`, use cases in `UseCase`
- [ ] **File Organization**: One main type per file in `domain/model/`, `domain/repository/`, `domain/usecases/`

### Data Layer Rules ✓
- [ ] **DTOs**: Create DTOs in `data/dto/` with `toDomain()` mapping
- [ ] **Data Sources**: Organize in `data/source/remote/` and `data/source/local/`
- [ ] **Repository Impl**: Map DTOs → Domain models and data errors → domain errors
- [ ] **Never Expose DTOs**: DTOs stay in data layer, never leak to domain or presentation
- [ ] **Constants**: API paths and configs in `data/constants/`, no secrets hardcoded
- [ ] **Naming**: DTOs end in `Dto`, data sources in `DataSource`, impls in `RepositoryImpl`

### Presentation Layer Rules ✓
- [ ] **ViewModel**: Use `@HiltViewModel` with `StateFlow<{Feature}State>`
- [ ] **State Management**: Use `DataState<T>` sealed class (Loading/Success/Failure/Initial)
- [ ] **Immutable State**: State is `data class` with `val`, update via `.update { }`
- [ ] **Screen**: Use `hiltViewModel()` and `collectAsStateWithLifecycle()`
- [ ] **Stateless Components**: Pass state and callbacks as parameters
- [ ] **No Business Logic**: Screens/Composables only render UI, no direct repository calls
- [ ] **Naming**: Screens end in `Screen`, ViewModels in `ViewModel`, state in `State`

### DI Rules ✓
- [ ] **Location**: All DI modules at root `di/` folder (NOT in features or core)
- [ ] **Feature Modules**: Each feature has `{Feature}Module.kt` at root level
- [ ] **Bindings**: Use `@Binds` for repository interfaces
- [ ] **Providers**: Use `@Provides` for concrete classes or factories
- [ ] **Scope**: `@Singleton` for repositories/managers, `@HiltViewModel` for ViewModels
- [ ] **Use Cases**: Can use `@Inject constructor` (auto-provided) or explicit `@Provides`

### Testing Rules ✓
- [ ] **Mirror Structure**: `test/` mirrors `src/` package structure
- [ ] **File Naming**: Test files end in `Test.kt` (e.g., `FeatureViewModelTest.kt`)
- [ ] **Fakes over Mocks**: Use fake implementations with in-memory state
- [ ] **Test States**: Cover Loading → Success, Loading → Failure transitions
- [ ] **Hilt Tests**: Use `@HiltAndroidTest` with `@TestInstallIn` for fakes

### Code Quality Rules ✓
- [ ] **Separation of Concerns**: Domain = pure, Data = I/O, Presentation = UI only
- [ ] **Error Handling**: Use sealed classes for type-safe error handling
- [ ] **Testability**: All dependencies injected, easy to test with fakes
- [ ] **Consistency**: Mirror structure across all features
- [ ] **Documentation**: Update README with new structure after migration

### Security Rules ✓
- [ ] **API Keys**: Never hardcode keys, use BuildConfig or local.properties
- [ ] **Mock in Tests**: Use fake/mock keys in tests, never real keys
- [ ] **CI/CD**: Use GitHub Secrets for real keys in CI pipelines

---

## Implementation Notes

**During Each Phase**:
1. Create folder structure first (domain/data/presentation)
2. Move/create files following naming conventions
3. Update imports to match new package structure
4. Create DI module at root `di/` folder
5. Verify tests still pass
6. Update AndroidManifest.xml if needed (services, permissions)
7. Run full regression test before merging PR

**Common Pitfalls to Avoid**:
- ❌ Android imports in domain layer
- ❌ DTOs leaking to presentation layer
- ❌ Business logic in ViewModels (use use cases)
- ❌ DI modules inside feature packages (keep at root)
- ❌ Mutable state in models (use `val`, not `var`)
- ❌ Hardcoded API keys or secrets

**Success Criteria**:
- ✅ All features follow feature/{domain,data,presentation} structure
- ✅ Domain layer has zero Android framework imports
- ✅ All repositories return `Result<T, E>` or Flow
- ✅ All ViewModels use `StateFlow` with immutable state
- ✅ All DI modules at root `di/` folder
- ✅ Tests mirror src/ structure
- ✅ App functionality unchanged after migration