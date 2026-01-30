# Voice Recognition Engine Abstraction Plan

## Executive Summary

Design an abstraction layer that allows hot-swapping between voice recognition engines (Vosk → Edge Impulse) without affecting the domain layer or UI. The plan follows your existing repository pattern and service architecture, ensuring minimal changes when switching engines.

---

## Architecture Overview

### Current Pattern (Wake Word Detection)
```
UI Layer (MainScreen)
    ↓
ViewModel (MainViewModel)
    ↓
Use Cases (Observe/Start/Stop)
    ↓
Repository Interface (WakeWordRepository) ← Domain boundary
    ↓
Repository Implementation (WakeWordRepositoryImpl)
    ↓
Service (WakeWordService) ← Engine isolated here (Porcupine)
```

### Proposed Pattern (Speech Recognition)
```
UI Layer (MainScreen)
    ↓
ViewModel (MainViewModel)
    ↓
Use Cases (Start/Stop/ObserveSpeechRecognition)
    ↓
Repository Interface (SpeechRecognitionRepository) ← Domain boundary
    ↓
Repository Implementation (SpeechRecognitionRepositoryImpl)
    ↓
Service (SpeechRecognitionService)
    ↓
Engine Adapter Interface (SpeechRecognitionEngine) ← Swap point
    ↓                                  ↓
VoskEngine                    EdgeImpulseEngine
(Vosk SDK)                    (TensorFlow Lite)
```

---

## Core Abstraction Strategy

### Layer 1: Domain Models (Engine-Agnostic)

**Purpose**: Define data structures shared across all engines

**New Models Needed**:
1. `SpeechRecognitionEvent` - Transcription results
2. `RecognitionStatus` - Recording state (idle, listening, processing, error)
3. `RecognitionError` - Error types and recovery hints
4. `AudioBuffer` - Raw audio data wrapper (if needed for multi-stage processing)

**Location**: `domain/models/`

**Key Design**:
- Immutable data classes
- No Android/engine dependencies
- Validation in init blocks
- Utility functions for common operations

---

### Layer 2: Repository Interface (Domain Contract)

**Purpose**: Define operations independent of engine implementation

**Interface**: `SpeechRecognitionRepository`

**Methods**:
```kotlin
interface SpeechRecognitionRepository {
    // Lifecycle
    suspend fun startRecognition(): Result<Unit>
    suspend fun stopRecognition(): Result<Unit>

    // State observation
    fun observeRecognitionEvents(): Flow<SpeechRecognitionEvent>
    fun observeRecognitionStatus(): Flow<RecognitionStatus>

    // Status queries
    suspend fun isRecognizing(): Boolean
    suspend fun getStatus(): RecognitionStatus
}
```

**Location**: `domain/repository/SpeechRecognitionRepository.kt`

**Key Design**:
- No engine-specific types exposed
- Result types for error handling
- StateFlow for reactive state
- Suspend functions for one-shot operations

---

### Layer 3: Repository Implementation (Bridge Layer)

**Purpose**: Delegate to service, transform service events to domain models

**Class**: `SpeechRecognitionRepositoryImpl`

**Responsibilities**:
- Forward start/stop calls to SpeechRecognitionService
- Transform service StateFlows to domain flows
- Wrap exceptions in Result types
- No engine knowledge (delegates to service)

**Location**: `data/repository/SpeechRecognitionRepositoryImpl.kt`

**Key Design**:
- @Singleton scope
- Constructor inject Context
- Thin adapter (no business logic)

---

### Layer 4: Service Layer (Android Component)

**Purpose**: Manage foreground service, audio capture, engine lifecycle

**Class**: `SpeechRecognitionService`

**Responsibilities**:
- Foreground service lifecycle
- Audio recording via AudioRecord
- Initialize/destroy engine adapter
- Transform engine callbacks to domain events
- Emit events via companion StateFlows (like WakeWordService)

**Location**: `data/service/SpeechRecognitionService.kt`

**Key Design**:
- Companion object with StateFlows (singleton pattern)
- Static start/stop methods
- Inject engine via Hilt (bound to interface)
- Handle all Android-specific concerns (permissions, notifications, audio)

---

### Layer 5: Engine Adapter Interface (Swap Boundary)

**Purpose**: Abstract engine-specific initialization, processing, and cleanup

**Interface**: `SpeechRecognitionEngine`

**Methods**:
```kotlin
interface SpeechRecognitionEngine {
    // Lifecycle
    suspend fun initialize(context: Context, config: EngineConfig): Result<Unit>
    suspend fun start(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun release(): Result<Unit>

    // Audio processing
    suspend fun processAudioFrame(audioData: ShortArray, sampleRate: Int): Result<Unit>

    // Event callbacks
    fun setEventCallback(callback: (SpeechRecognitionEvent) -> Unit)
    fun setStatusCallback(callback: (RecognitionStatus) -> Unit)

    // Capabilities
    fun getRequiredSampleRate(): Int
    fun getEngineInfo(): EngineInfo
}
```

**Supporting Types**:
```kotlin
data class EngineConfig(
    val modelPath: String? = null,
    val sensitivity: Float = 0.5f,
    val language: String = "en-US",
    val maxAudioLengthMs: Long = 5000,
    val engineSpecificParams: Map<String, Any> = emptyMap()
)

data class EngineInfo(
    val name: String,
    val version: String,
    val modelSize: Long,
    val supportsStreaming: Boolean
)
```

**Location**: `data/engine/SpeechRecognitionEngine.kt`

**Key Design**:
- Callback-based (engine calls back to service)
- Handles audio frame processing
- Configuration via data class (supports engine-specific params)
- Result types for error handling

---

### Layer 6: Concrete Engine Implementations

#### Implementation 1: Vosk

**Class**: `VoskSpeechRecognitionEngine`

**Location**: `data/engine/vosk/VoskSpeechRecognitionEngine.kt`

**Dependencies**:
- Vosk SDK (via Gradle)
- Model file in assets/

**Key Design**:
- Initialize Vosk Model and Recognizer in `initialize()`
- Process audio frames via `recognizer.acceptWaveForm()`
- Parse JSON results to SpeechRecognitionEvent
- Handle partial vs. final results
- Clean up Vosk resources in `release()`

---

#### Implementation 2: Edge Impulse

**Class**: `EdgeImpulseSpeechRecognitionEngine`

**Location**: `data/engine/edgeimpulse/EdgeImpulseSpeechRecognitionEngine.kt`

**Dependencies**:
- Edge Impulse SDK (TensorFlow Lite + custom runtime)
- .tflite model file in assets/
- Label mapping file

**Key Design**:
- Initialize TFLite Interpreter in `initialize()`
- Buffer audio frames until full window collected
- Run inference on audio window
- Map output tensor to recognized command/text
- Handle confidence thresholds
- Clean up TFLite resources in `release()`

---

## Dependency Injection Setup

### Hilt Module Configuration

**Module**: `SpeechRecognitionModule`

**Location**: `di/SpeechRecognitionModule.kt`

**Bindings**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechRecognitionModule {

    @Binds
    @Singleton
    abstract fun bindSpeechRecognitionRepository(
        impl: SpeechRecognitionRepositoryImpl
    ): SpeechRecognitionRepository

    @Binds
    @Singleton
    abstract fun bindSpeechRecognitionEngine(
        impl: VoskSpeechRecognitionEngine  // ← Swap point
    ): SpeechRecognitionEngine
}
```

**Key Design**:
- Repository binding never changes
- Engine binding is the ONLY line to change when swapping
- Both implementations available in codebase
- Switch by changing one line: `VoskSpeechRecognitionEngine` → `EdgeImpulseSpeechRecognitionEngine`

---

## Hot-Swap Procedure

### Step-by-Step Swap Process

**To switch from Vosk to Edge Impulse:**

1. **Add dependency** (if not already present)
   - Update `app/build.gradle.kts` with Edge Impulse SDK

2. **Add model assets**
   - Place `.tflite` model in `assets/models/`
   - Add label mapping file if needed

3. **Update DI binding** (ONE line change)
   - File: `di/SpeechRecognitionModule.kt`
   - Change: `VoskSpeechRecognitionEngine` → `EdgeImpulseSpeechRecognitionEngine`

4. **Rebuild and test**
   - Clean build
   - Verify initialization
   - Test recognition accuracy

**No other code changes required** - domain layer, repository, UI remain unchanged.

---

## Use Case Layer

### New Use Cases Needed

**StartSpeechRecognitionUseCase**
```kotlin
class StartSpeechRecognitionUseCase @Inject constructor(
    private val speechRecognitionRepository: SpeechRecognitionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            if (speechRecognitionRepository.isRecognizing()) {
                return Result.success(Unit)
            }
            speechRecognitionRepository.startRecognition()
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Failed to start: ${e.message}"))
        }
    }
}
```

**StopSpeechRecognitionUseCase**
```kotlin
class StopSpeechRecognitionUseCase @Inject constructor(
    private val speechRecognitionRepository: SpeechRecognitionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return speechRecognitionRepository.stopRecognition()
    }
}
```

**ObserveSpeechEventsUseCase**
```kotlin
class ObserveSpeechEventsUseCase @Inject constructor(
    private val speechRecognitionRepository: SpeechRecognitionRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<SpeechRecognitionEvent> {
        return combine(
            speechRecognitionRepository.observeRecognitionEvents(),
            settingsRepository.observeSettings()
        ) { event, settings ->
            // Apply domain logic (e.g., confidence filtering)
            if (event.confidence >= settings.recognitionConfidenceThreshold) {
                event
            } else {
                null
            }
        }.filterNotNull()
    }
}
```

**Location**: `domain/usecases/`

---

## Integration with Existing Architecture

### ViewModel Integration

**MainViewModel** will need:
- Inject new use cases
- Observe speech events
- Combine with existing state

**Pattern** (following existing WakeWordEvent pattern):
```kotlin
val speechEvents: StateFlow<SpeechRecognitionEvent?> = observeSpeechEvents()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
```

### Coordination with Wake Word Service

**Flow**:
1. WakeWordService detects "Hey Nova"
2. ViewModel observes WakeWordEvent
3. ViewModel triggers StartSpeechRecognitionUseCase
4. SpeechRecognitionService starts listening
5. User speaks command
6. Engine processes audio → SpeechRecognitionEvent
7. ViewModel observes event → triggers action

**Key Design**:
- Two independent services
- Coordinated via ViewModel
- No direct service-to-service communication
- Clean separation of concerns

---

## Configuration Management

### Settings Extension

**Add to Settings data class**:
```kotlin
data class Settings(
    // Existing...
    val isServiceEnabled: Boolean,
    val wakeSensitivity: Float,

    // New for speech recognition
    val selectedEngine: SpeechEngine = SpeechEngine.VOSK,
    val recognitionConfidenceThreshold: Float = 0.6f,
    val maxRecordingDurationMs: Long = 5000,
    val autoStopOnSilence: Boolean = true
)

enum class SpeechEngine {
    VOSK,
    EDGE_IMPULSE
}
```

**Advanced Option**: Use settings to select engine at runtime
- Store engine selection in DataStore
- Use Hilt qualifiers (@Named) to provide different engines
- Factory pattern to instantiate based on settings
- **Trade-off**: More complexity, but no recompile needed

**Recommendation for POC**: Use compile-time swap (DI binding change)
- Simpler implementation
- Easier to debug
- Can add runtime selection in Phase 2 if validated

---

## Testing Strategy

### Unit Testing

**Repository Tests**:
- Mock SpeechRecognitionService StateFlows
- Verify Result types on errors
- Test flow transformations

**Use Case Tests**:
- Mock repository
- Verify business logic (confidence filtering, etc.)
- Test flow composition

**Engine Tests**:
- Mock audio frames
- Verify event callbacks
- Test initialization/cleanup

### Integration Testing

**Engine Swap Test**:
1. Record 10 test utterances
2. Run with Vosk, measure accuracy/latency
3. Swap to Edge Impulse (change DI binding)
4. Run same test set, measure accuracy/latency
5. Compare results

**Acceptance Criteria**:
- No code changes except DI binding
- Both engines produce SpeechRecognitionEvent
- UI behavior identical
- Different accuracy/latency profiles OK (expected)

---

## File Structure

```
NovaAssistant/app/src/main/java/com/novaassistant/

domain/
├── models/
│   ├── SpeechRecognitionEvent.kt          [NEW]
│   ├── RecognitionStatus.kt               [NEW]
│   ├── RecognitionError.kt                [NEW]
│   └── (existing models)
├── repository/
│   ├── SpeechRecognitionRepository.kt     [NEW]
│   └── (existing repositories)
└── usecases/
    ├── StartSpeechRecognitionUseCase.kt   [NEW]
    ├── StopSpeechRecognitionUseCase.kt    [NEW]
    ├── ObserveSpeechEventsUseCase.kt      [NEW]
    └── (existing use cases)

data/
├── service/
│   ├── SpeechRecognitionService.kt        [NEW]
│   └── WakeWordService.kt                 [EXISTING]
├── repository/
│   ├── SpeechRecognitionRepositoryImpl.kt [NEW]
│   └── (existing implementations)
└── engine/
    ├── SpeechRecognitionEngine.kt         [NEW - Interface]
    ├── EngineConfig.kt                    [NEW]
    ├── EngineInfo.kt                      [NEW]
    ├── vosk/
    │   └── VoskSpeechRecognitionEngine.kt [NEW]
    └── edgeimpulse/
        └── EdgeImpulseSpeechRecognitionEngine.kt [NEW]

di/
├── SpeechRecognitionModule.kt             [NEW]
└── (existing modules)

presentation/
└── main/
    └── MainViewModel.kt                    [MODIFY - Add speech use cases]
```

---

## Critical Design Decisions

### Decision 1: Where to place engine abstraction?

**Chosen**: Between Service and Engine implementation

**Rationale**:
- Service handles Android concerns (audio, foreground, notifications)
- Engine handles only recognition logic
- Enables testing engines without Android dependencies
- Follows your existing pattern (Service isolates Porcupine)

### Decision 2: Callback vs. Flow in engine interface?

**Chosen**: Callbacks from engine → Service transforms to StateFlow

**Rationale**:
- Engines (especially native SDKs) use callbacks natively
- Service layer is already StateFlow-based (following WakeWordService)
- Avoids forcing engine implementations to use coroutines/Flow
- Clean adapter boundary

### Decision 3: Audio capture in Service or Engine?

**Chosen**: Service handles audio capture, passes frames to engine

**Rationale**:
- Audio capture is Android-specific (AudioRecord, permissions)
- Engines should be pure processors (audio in → events out)
- Enables engine unit tests with synthetic audio
- Reuses AudioDeviceManager from WakeWordService

### Decision 4: Hot-swap via DI or runtime selection?

**Chosen**: DI binding change for POC, runtime option for Phase 2

**Rationale**:
- DI swap: Simple, compile-time safe, easier to debug
- Runtime swap: Requires factory pattern, more complexity
- POC goal is validation, not production features
- Can add runtime selection after validation succeeds

### Decision 5: One repo interface or one per engine?

**Chosen**: Single SpeechRecognitionRepository interface

**Rationale**:
- Engines provide same capability (audio → text)
- UI shouldn't care about engine choice
- Swapping should be transparent to domain layer
- Follows Liskov Substitution Principle

---

## Implementation Phases

### Phase 1: Foundation (Week 3)
- Define domain models
- Create repository interface
- Create engine interface
- Set up DI module

### Phase 2: Vosk Implementation (Week 3-4)
- Implement VoskSpeechRecognitionEngine
- Implement SpeechRecognitionService
- Implement repository
- Create use cases
- Integrate with MainViewModel

### Phase 3: Testing & Validation (Week 4)
- Test Vosk accuracy/latency
- Validate battery impact
- Test integration with wake word flow

### Phase 4: Edge Impulse Prep (Post-POC)
- Implement EdgeImpulseSpeechRecognitionEngine
- Collect training data
- Train model
- Test accuracy

### Phase 5: Hot-Swap Validation (Post-POC)
- Change DI binding
- Run same test suite
- Compare metrics
- Document findings

---

## Success Criteria

### Abstraction Quality Metrics

1. **Zero domain layer changes** when swapping engines
2. **One-line DI change** to swap engines
3. **Identical StateFlow types** from both engines
4. **No engine-specific types** in repository/use cases
5. **Independent testing** of each engine implementation

### Functional Requirements

1. Both engines emit `SpeechRecognitionEvent` with same structure
2. Both engines report `RecognitionStatus` lifecycle
3. Error handling works identically
4. Audio device management works with both engines
5. Battery drain comparable between engines

---

## Risk Mitigation

### Risk 1: Engine APIs too different to abstract

**Mitigation**:
- Engine adapter handles impedance mismatch
- Service provides unified audio input pipeline
- Callbacks allow async differences

### Risk 2: Performance overhead from abstraction

**Mitigation**:
- Abstraction is thin (no heavy processing)
- Direct function calls, minimal indirection
- Profile both engines to verify

### Risk 3: Audio format incompatibility

**Mitigation**:
- Engine exposes required sample rate via `getRequiredSampleRate()`
- Service resamples if needed
- AudioRecord configured per engine requirements

### Risk 4: Model size differences impact UX

**Mitigation**:
- Document model sizes clearly
- Loading screens if needed
- Lazy load models (not at app startup)

---

## Future Enhancements (Phase 2)

1. **Runtime engine selection** via Settings UI
2. **Engine capability discovery** (streaming vs. batch, languages, etc.)
3. **Hybrid mode**: Vosk for general commands + Edge Impulse for specific keywords
4. **A/B testing framework** to compare engines on real users
5. **Fallback engine**: Try Edge Impulse first, fall back to Vosk if low confidence
6. **Model updates**: Download new Edge Impulse models without app update

---

## Summary

This abstraction strategy:
- ✅ Follows your existing architectural patterns (Repository, Use Cases, DI)
- ✅ Isolates engine-specific code in adapter implementations
- ✅ Enables hot-swapping with one DI binding change
- ✅ Maintains domain layer purity (no engine dependencies)
- ✅ Supports independent testing of engines
- ✅ Scales to Phase 2 enhancements
- ✅ Minimizes risk by reusing proven patterns

**Key Insight**: The abstraction point is at the engine adapter interface, not the repository. The repository remains engine-agnostic, delegating to the service, which uses DI to get the configured engine implementation.
