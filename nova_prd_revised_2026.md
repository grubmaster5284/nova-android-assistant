# Product Requirements Document (PRD)

## Project: Nova — Offline Voice Assistant (Experimental POC)

**Version:** 0.2 (Revised January 2026)
**Owner:** Ricky
**Last Updated:** January 3, 2026

---

# 1. Executive Summary

Nova is an experimental **offline-first voice assistant** for Android that validates whether hands-free device interaction can be practical, reliable, and privacy-preserving. This PRD reflects a **realistic, scalable approach**: build a minimal proof-of-concept to validate core assumptions, then expand only if validated.

## The Core Hypothesis

**"Can an offline voice assistant feel faster and more natural than unlocking your phone and tapping?"**

If yes → build more features
If no → learned valuable lessons, minimal time invested

## The Strategy

- **Phase 1 (POC)**: Bare minimum to validate the concept (6 weeks)
- **Phase 2-4**: Feature expansion only if POC proves viable
- **Native Android**: Right foundation for performance-critical app
- **Clean Architecture**: Easy to scale or abandon

---

# 2. Goals & Non-Goals

## 2.1 POC Goals (Phase 1)

**Primary Goal**: Validate that offline wake word → voice command → device action feels **responsive, reliable, and useful** in real-world conditions.

**Technical Validation:**
- Prove wake word detection works with acceptable battery drain
- Prove offline STT captures short commands accurately
- Prove end-to-end latency feels instantaneous (<1.5s)
- Prove one useful action: making phone calls hands-free

**Practical Validation:**
- Would I actually use this instead of touching my phone?
- Does it work in realistic environments, not just silent rooms?
- Does it work on first try, or need multiple attempts?

## 2.2 POC Non-Goals (Deferred to Later Phases)

- No LLM-based natural language understanding
- No conversation memory or context
- No ambiguity resolution (exact matches only)
- No multi-turn dialogue
- No messaging, alarms, reminders, navigation
- No Android Auto integration
- No cloud LLM integration
- No advanced error recovery
- No performance optimization beyond basics

## 2.3 Long-Term Vision (Post-POC)

If POC validates core assumptions, Nova will evolve into a **full multimodal personal agent** with:
- Conversation memory and personalization
- LLM-powered natural language understanding
- Complete device control (messaging, alarms, timers, reminders)
- Online capabilities (web search, cloud LLM integration)
- Automotive integration (Android Auto)
- Smart context awareness

---

# 3. Target Users & Use Cases

## 3.1 Primary User (POC)

A privacy-conscious user who wants **hands-free phone calling** when:
- Cooking (hands dirty/busy)
- Driving (eyes on road)
- Exercising (phone in pocket)
- Working (hands on keyboard)

## 3.2 Core Use Case (POC)

**User:** "Hey Nova"
**System:** [Beep/visual indicator]
**User:** "Call Mom"
**System:** [Shows contact on screen] "Call Sarah Johnson?"
**User:** "Yes" (voice confirmation - hands-free)
**System:** [Places call] "Calling Sarah Johnson"

## 3.3 Success Criteria (POC)

### Technical KPIs
- Wake word detection: >85% accuracy in quiet environments
- Battery drain: <3% per hour in passive listening mode
- End-to-end latency: <1.5 seconds (wake word → response)
- STT accuracy: >80% for short commands in quiet environments
- Call success rate: >90% after confirmation

### Practical KPIs
- **The Real Test**: After 2 weeks of daily use, do I still use it, or have I stopped?

---

# 4. Product Scope

## 4.1 Phase 1: Proof of Concept (6 Weeks)

### Functional Requirements

#### 4.1.1 Wake Word Detection
- Always-on listening via foreground service
- Single wake word: "Hey Nova" (or "Okay Nova")
- Low false-positives in normal household environment
- Visual + audio feedback on detection
- **Technology**: Porcupine Android SDK (free tier)

#### 4.1.2 Speech Recognition (STT)
- Short utterances only (<5 seconds)
- Offline-only (no cloud)
- Captures command after wake word trigger
- **Technology**: Vosk offline STT (~50MB model)
- **Supported**: English only for POC

#### 4.1.3 Intent Extraction
- **Simple keyword/regex matching** (NO LLM in POC)
- Recognizes: "call [contact name]"
- Extracts contact name from utterance
- **Future**: LLM-based NLU in Phase 2

#### 4.1.4 Contact Calling
- Access Android contacts
- **Exact match only** (no fuzzy matching, no disambiguation)
- If "John" → finds first "John" in contacts
- If multiple Johns → picks first match (no disambiguation in POC)
- If no match → error message

#### 4.1.5 Confirmation Flow
- **Voice-based confirmation** (hands-free operation)
- TTS asks: "Call [contact name]?"
- Listen for confirmation: "Yes" / "Yeah" / "Okay" / "Sure"
- Listen for cancellation: "No" / "Cancel" / "Stop"
- Visual feedback on screen (contact info shown)
- Simple keyword matching for yes/no (no complex NLU needed)

#### 4.1.6 Call Execution
- Native Android calling intent
- Places call to selected contact
- TTS confirmation: "Calling [name]"

#### 4.1.7 Voice Response (TTS)
- Basic text-to-speech feedback
- **Technology**: Android TextToSpeech API (built-in)
- Natural-sounding voice (system default)
- Minimal responses:
  - "Call [name]?" (confirmation question)
  - "Calling [name]" (after confirmation)
  - "Call cancelled"
  - "I didn't find [name]"
  - "Say again?"

#### 4.1.8 Error Handling (Basic)
- "I didn't catch that" (STT failure)
- "I didn't find [name]" (no contact match)
- Service restart on crash
- **No advanced error recovery in POC**

### Non-Functional Requirements (POC)

#### Performance
- Wake word CPU: <7% on mid-range devices
- STT latency: <500ms for 2-3 second utterances
- TTS latency: <300ms for short responses
- Total end-to-end: <1.5 seconds

#### Reliability
- No crashes during normal operation
- Service survives device sleep/wake
- Handles audio interruptions gracefully

#### Privacy
- No cloud calls
- No data leaves device
- No transcripts stored
- Permissions requested: Microphone, Contacts, Phone

#### Battery
- Target: <3% per hour during passive listening
- Acceptable: <5% per hour (still usable)
- Unacceptable: >5% per hour (needs optimization or abandon)

---

## 4.2 Phase 2: Intelligent Conversation (Post-POC)

**IF POC validates core assumptions, add:**

*Note: POC already includes basic voice confirmation using keyword matching. Phase 2 enhances this with LLM-powered understanding for more natural interactions.*

### Enhanced Natural Language Understanding
- Integrate lightweight on-device LLM (Qwen3-0.6B or Gemma 2B)
- Intent classification with confidence scores
- Context extraction from natural speech
- Multi-intent handling

### Advanced Confirmation Flows
- LLM-powered understanding of confirmations (beyond keyword matching)
- Context-aware follow-up questions
- Handling ambiguous responses ("maybe", "I think so")
- Multi-step confirmations for complex actions

### Contact Disambiguation
- Handle multiple matches intelligently
- "Which John? John Smith or John Doe?"
- Multiple phone numbers per contact
- Smart defaults (recent calls, favorites)

### Improved Error Recovery
- Clarifying questions
- Suggest corrections
- Retry logic with guidance

---

## 4.3 Phase 3: Expanded Actions (Post-Phase 2)

**Add core device functions:**

### Messaging
- Send SMS/WhatsApp messages
- Read recent messages
- Reply to messages

### Time Management
- Set alarms and timers
- Create reminders
- Check calendar

### Information Queries
- "What time is it?"
- "What's my next meeting?"
- "Battery level?"

---

## 4.4 Phase 4: Online Integration (Post-Phase 3)

**Add cloud-connected capabilities:**

### Web Search
- Online search queries
- Weather information
- News updates

### Cloud LLM Integration
- Complex question answering
- Creative tasks
- Advanced reasoning

### Navigation
- Directions and ETA
- Traffic updates
- Android Auto integration

### Memory & Personalization
- Conversation history
- User preferences
- Learning from interactions
- Optional cloud sync (encrypted)

---

# 5. Technical Architecture (POC)

## 5.1 Technology Stack

### Platform
- **Language**: Kotlin
- **Min SDK**: Android 14 (API 34)
- **Target SDK**: Android 15 (API 35)
- **Baseline Device**: Nothing Phone (3a) - Snapdragon 7s Gen 3, 8GB RAM

### Architecture
- **Pattern**: MVVM + Clean Architecture (lightweight)
- **UI Framework**: Jetpack Compose (declarative UI)
- **DI**: Hilt for dependency injection
- **Async**: Kotlin Coroutines + StateFlow/Flow
- **Navigation**: Compose Navigation
- **State Management**: ViewModel + Compose State
- **Testing**: JUnit, MockK, Compose UI Testing (minimal for POC)

### Voice Pipeline Components
- **Wake Word**: Porcupine Android SDK (Picovoice)
- **STT**: Vosk offline speech recognition
- **Intent Extraction**: Regex/keyword matching (custom)
- **TTS**: Android TextToSpeech API
- **LLM**: None in POC (Phase 2: MLLM framework with Qwen3-0.6B)

### Android Components
- **Service**: Foreground service for wake word detection
  - Service Type: `microphone` (required for API 34+)
  - Required Permissions:
    - `FOREGROUND_SERVICE` (normal permission, granted by default)
    - `FOREGROUND_SERVICE_MICROPHONE` (required for API 34+)
    - `RECORD_AUDIO` (dangerous permission, runtime request)
    - `POST_NOTIFICATIONS` (for foreground service notification, API 33+)
- **Contacts & Calling Permissions**:
  - `READ_CONTACTS` (dangerous permission, runtime request)
  - `CALL_PHONE` (dangerous permission, runtime request)
- **Manifest Declarations**:
  - Foreground service type must be declared in manifest
  - While-in-use permission model for microphone access
  - Play Console: Must declare foreground service usage (effective Jan 22, 2025)
- **Intents**: ACTION_CALL for placing calls
- **Storage**: DataStore (modern replacement for SharedPreferences)

### Performance Baseline (Nothing Phone 3a)

**Hardware Specifications:**
- **Chipset**: Snapdragon 7s Gen 3 (4nm) - mid-range, power-efficient
- **CPU**: Octa-core (1x2.5 GHz + 3x2.4 GHz Cortex-A720 + 4x1.8 GHz Cortex-A520)
- **GPU**: Adreno 710 (940 MHz)
- **RAM**: 8GB (user's device)
- **AnTuTu Score**: ~825,564
- **Release**: March 2025

**Performance Expectations:**
- **Wake word detection**: Excellent - Porcupine optimized for mobile
- **Vosk STT**: Good - 50MB model runs efficiently on Snapdragon 7s Gen 3
- **Phase 2 LLM (0.6-1B params)**: Feasible - 8GB RAM sufficient for quantized models
- **Phase 2 LLM (2-3B params)**: Marginal - expect 5-8 tokens/sec, may need aggressive quantization
- **Battery**: Mid-range chip is power-efficient, should meet <3% per hour target
- **Thermal**: 4nm process handles sustained workloads well

## 5.2 System Flow (POC)

```
┌──────────────────────┐
│  MainActivity        │
│  - Start/Stop toggle │
│  - Status indicator  │
│  - Settings          │
└──────────┬───────────┘
           │
┌──────────▼───────────────────────────┐
│  WakeWordService (Foreground)        │
│  - Porcupine engine                  │
│  - Always-on listening               │
│  - Wake word detection               │
└──────────┬───────────────────────────┘
           │ wake word detected
┌──────────▼───────────────────────────┐
│  AudioCaptureService                 │
│  - Record 3-5 seconds                │
│  - Pass to STT                       │
└──────────┬───────────────────────────┘
           │ audio buffer
┌──────────▼───────────────────────────┐
│  VoskSTTEngine                       │
│  - Transcribe audio                  │
│  - Return text                       │
└──────────┬───────────────────────────┘
           │ text transcript
┌──────────▼───────────────────────────┐
│  IntentExtractor (Regex/Keywords)    │
│  - Parse "call [name]"               │
│  - Extract contact name              │
└──────────┬───────────────────────────┘
           │ contact name
┌──────────▼───────────────────────────┐
│  ContactRepository                   │
│  - Query Android contacts            │
│  - Return first exact match          │
└──────────┬───────────────────────────┘
           │ contact + phone number
┌──────────▼───────────────────────────┐
│  ConfirmationHandler                 │
│  - TTS: "Call [name]?"               │
│  - Show contact on screen            │
│  - Listen for Yes/No (STT)           │
│  - Keyword match confirmation        │
└──────────┬───────────────────────────┘
           │ user confirmed (voice)
┌──────────▼───────────────────────────┐
│  CallingHandler                      │
│  - ACTION_CALL intent                │
│  - TTS: "Calling [name]"             │
└──────────────────────────────────────┘
```

## 5.3 Project Structure

```
nova/
├── app/
│   ├── presentation/
│   │   ├── main/
│   │   │   ├── MainActivity.kt (hosts Compose UI)
│   │   │   ├── MainScreen.kt (Compose UI)
│   │   │   └── MainViewModel.kt
│   │   ├── confirmation/
│   │   │   ├── ConfirmationScreen.kt (Compose UI)
│   │   │   └── ConfirmationViewModel.kt
│   │   ├── components/
│   │   │   ├── StatusIndicator.kt (Compose)
│   │   │   ├── ContactCard.kt (Compose)
│   │   │   └── PermissionHandler.kt
│   │   ├── navigation/
│   │   │   └── NavGraph.kt (Compose Navigation)
│   │   └── theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── domain/
│   │   ├── usecases/
│   │   │   ├── DetectWakeWordUseCase.kt
│   │   │   ├── TranscribeAudioUseCase.kt
│   │   │   ├── ExtractIntentUseCase.kt
│   │   │   ├── FindContactUseCase.kt
│   │   │   ├── ConfirmActionUseCase.kt
│   │   │   └── PlaceCallUseCase.kt
│   │   ├── models/
│   │   │   ├── Contact.kt
│   │   │   ├── VoiceCommand.kt
│   │   │   └── AssistantState.kt
│   │   └── repository/
│   │       ├── ContactRepository.kt (interface)
│   │       └── SettingsRepository.kt (interface)
│   ├── data/
│   │   ├── repository/
│   │   │   ├── ContactRepositoryImpl.kt
│   │   │   └── SettingsRepositoryImpl.kt
│   │   ├── local/
│   │   │   └── SettingsDataStore.kt
│   │   └── service/
│   │       ├── WakeWordService.kt
│   │       ├── VoskSTTEngine.kt
│   │       └── AndroidTTSEngine.kt
│   └── di/
│       ├── AppModule.kt
│       ├── DataModule.kt
│       └── ServiceModule.kt
└── build.gradle.kts
```

---

## 5.4 Android 14+ Permission Handling Strategy

### Critical Permission Requirements (API 34+)

**Foreground Service Type Declaration:**
```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".data.service.WakeWordService"
    android:foregroundServiceType="microphone"
    android:exported="false" />
```

**Required Manifest Permissions:**
```xml
<!-- Foreground service permissions -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />

<!-- Runtime permissions (dangerous) -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Runtime Permission Flow (Compose)

**Step 1: Request on App Launch**
- RECORD_AUDIO (critical - needed for wake word)
- POST_NOTIFICATIONS (for foreground service)

**Step 2: Request on First Use**
- READ_CONTACTS (when user first attempts to call)
- CALL_PHONE (when user confirms call)

**While-in-Use Restrictions:**
- Foreground services with `microphone` type can only access audio while app is in foreground or service is running
- If app is in background and tries to start FGS with microphone, system throws SecurityException unless app has active foreground service
- Solution: Start foreground service from foreground (user-initiated action)

### Permission Best Practices (2026)

1. **Explain Before Requesting**: Use Compose dialogs to explain why each permission is needed
2. **Graceful Degradation**: Handle permission denial gracefully
3. **Runtime Checks**: Always check permissions before using sensitive APIs
4. **Accompanist Permissions**: Use `accompanist-permissions` library for Compose-friendly permission handling
5. **Play Console Compliance**: Declare foreground service usage in Play Console (required as of Jan 2025)

### Permission Rationale (For Play Store Review)

- **FOREGROUND_SERVICE_MICROPHONE**: Required for always-on wake word detection
- **RECORD_AUDIO**: Capture voice commands after wake word trigger
- **READ_CONTACTS**: Find contact to call based on voice input
- **CALL_PHONE**: Place phone call hands-free
- **POST_NOTIFICATIONS**: Display foreground service notification (required by Android)

---

# 6. Phase 1 (POC) Deliverables

## Minimum Viable Features
1. Wake word detection service (Porcupine)
2. Offline STT pipeline (Vosk)
3. Keyword-based intent extraction
4. Contact lookup (exact match)
5. Voice-based call confirmation (yes/no keyword matching)
6. Native call placement
7. Basic TTS responses
8. Simple error messages
9. Settings screen (enable/disable, sensitivity)

## Documentation
1. Setup instructions (Android Studio, dependencies)
2. Testing guide (how to test wake word, calling)
3. Battery measurement results
4. Latency benchmarks
5. Known limitations

## Testing Deliverables
1. Manual testing checklist
2. Battery drain report (24-hour test)
3. Wake word accuracy test (100 attempts)
4. Real-world usage diary (2 weeks)
5. Go/No-go decision document

---

# 7. Success Metrics (POC)

## Technical Validation Gates

### Gate 1: Wake Word (Week 2)
- ✅ Detection accuracy >85% in quiet room
- ✅ Battery drain <5% per hour
- ❌ If fails → investigate optimization or alternative engine

### Gate 2: Voice→Action (Week 4)
- ✅ End-to-end latency <1.5s
- ✅ STT accuracy >80% for "call [name]"
- ❌ If fails → may need better STT model or UX adjustment

### Gate 3: Real-World Test (Week 6)
- ✅ Used successfully for 10+ calls over 2 weeks
- ✅ Feels faster than unlocking phone
- ✅ Battery drain acceptable for daily use
- ❌ If fails → concept not practical, document learnings

## Go/No-Go Decision Criteria (Week 6)

### GO (Build Phase 2)
- All technical gates passed
- Actual daily usage continues beyond testing period
- User feedback: "This is genuinely useful"
- Battery drain acceptable (<5% per hour)

### NO-GO (Pause or Pivot)
- Wake word unreliable (constant false triggers or misses)
- Battery drain unacceptable (>7% per hour)
- Latency feels sluggish (>2 seconds)
- STT too inaccurate for practical use
- User feedback: "Just easier to tap my phone"

**If NO-GO**: Document findings, evaluate pivot options (e.g., button-activated instead of wake word, limited contexts like driving only)

---

# 8. Future Roadmap (Post-POC)

## Phase 2: Intelligent Assistant (8 weeks)
**Prerequisites**: POC validated

- LLM integration (Qwen3-0.6B via MLLM framework)
- Voice-based confirmation (eliminate screen tap)
- Contact disambiguation
- Multi-turn conversation (2-3 turn context)
- Improved error recovery
- Custom wake word training

**Deliverable**: Natural conversation for calling without screen interaction

## Phase 3: Essential Actions (6 weeks)
**Prerequisites**: Phase 2 complete

- Messaging (SMS, WhatsApp)
- Alarms and timers
- Reminders
- Calendar integration
- Information queries (time, battery, etc.)
- Quick actions (toggle WiFi, Bluetooth)

**Deliverable**: Core device control suite

## Phase 4: Online Intelligence (8 weeks)
**Prerequisites**: Phase 3 complete

- Web search integration
- Cloud LLM fallback (for complex queries)
- Weather and news
- Navigation and directions
- Android Auto integration
- Memory and personalization
- Optional cloud sync (encrypted)

**Deliverable**: Full-featured personal assistant

## Phase 5: Advanced Features (Future)
- Multi-modal inputs (screen context, image, OCR)
- Smart home integration
- Proactive suggestions
- Routine automation
- Voice customization
- Multiple language support
- Wear OS companion

---

# 9. Risks & Mitigations

## POC Phase Risks

### Risk: Wake word too battery-intensive
**Impact**: High - kills core value proposition
**Mitigation**:
- Porcupine is optimized for low power
- Early battery testing (Week 2)
- Fallback: Manual activation button if needed

### Risk: Vosk STT accuracy insufficient
**Impact**: Medium - frustrating UX
**Mitigation**:
- Alternative: Moonshine (even smaller, competitive accuracy)
- Alternative: Whisper.cpp (heavier but more accurate)
- Test early with real commands (Week 3)

### Risk: Android kills background service
**Impact**: High - app stops working
**Mitigation**:
- Foreground service with notification
- Battery optimization exemption request
- Test on multiple devices/Android versions

### Risk: Contact matching too brittle
**Impact**: Low - can iterate
**Mitigation**:
- Start with exact match (POC)
- Add fuzzy matching in Phase 2
- User can see why match failed

### Risk: User fatigue / stops using after novelty wears off
**Impact**: Critical - invalidates concept
**Mitigation**:
- Honest 2-week usage test
- Track: frequency of use, success rate, frustrations
- Accept failure early rather than sunk cost

## Scaling Phase Risks

### Risk: LLM too slow on mid-range devices
**Mitigation**: Aggressive quantization, model selection, or cloud fallback

### Risk: Model size bloats app (>500MB)
**Mitigation**: On-demand model download, GGUF compression

### Risk: Scope creep without validation
**Mitigation**: Strict phase gates, validate before expanding

---

# 10. Technical Research & Recommendations (2026)

## On-Device LLMs (Phase 2+)
- **Qwen3-0.6B**: Most downloaded sub-1B model (Dec 2025), 100+ languages
- **Gemma 2B**: Google's mobile-optimized model
- **TinyLlama**: Lightweight alternative
- **Performance**: Expect 8-10 tokens/sec on Snapdragon 8 Gen 2, slower on mid-range
- **Framework**: MLLM (November 2025 Android update, stable streaming)

## Speech Recognition Alternatives
- **Moonshine**: 27M params, outperforms Whisper Tiny despite smaller size
- **Vosk**: Lightweight, good for edge devices
- **Whisper.cpp**: C++ port, more accurate but heavier

## Wake Word Detection
- **Porcupine**: Industry standard, actively maintained (Oct 2025)
- **flutter_wake_word/DaVoice**: 99%+ accuracy, 1.2% battery/hour
- **openWakeWord**: Open-source alternative

## Performance Considerations
- Native Android: 10-20% better ML performance than Flutter
- Battery: Native Android measurably better for always-on services
- TensorFlow Lite: Near-native performance in Flutter, but native has hardware access advantage
- For always-on voice assistant: Native Android recommended

---

# 11. Timeline

## Phase 1: POC (6 Weeks)

**Week 1**: Project setup & wake word
- Android Studio project structure
- Porcupine SDK integration
- Basic MainActivity with start/stop
- Foreground service skeleton

**Week 2**: Wake word validation
- Complete wake word service
- Battery testing (24-48 hour test)
- Accuracy testing
- **GATE 1: Go/No-go on wake word**

**Week 3**: STT integration
- Vosk setup and model download
- Audio capture after wake word
- STT engine integration
- Test transcription accuracy

**Week 4**: Calling pipeline
- Keyword extraction (regex)
- Contact repository
- Voice confirmation flow (yes/no STT)
- Call placement
- **GATE 2: Go/No-go on voice→action**

**Week 5**: TTS & polish
- Android TTS integration (confirmation questions + responses)
- Error messages
- Edge cases (cancellation, no match, etc.)
- Settings screen

**Week 6**: Testing & decision
- Real-world usage testing
- Battery drain measurement
- Latency benchmarking
- User experience evaluation
- **GATE 3: Go/No-go on Phase 2**

## Phase 2-4: See Section 8 (Future Roadmap)

---

# 12. Required Resources

## Phase 1 (POC)
- **Developer**: 1 Android engineer (Kotlin + Compose experience)
- **Devices**:
  - Primary: Nothing Phone (3a) - Snapdragon 7s Gen 3, 8GB RAM (baseline)
  - Optional: 1-2 additional devices for testing (different manufacturers/specs)
  - All devices must run Android 14+ (API 34+)
- **Licenses**: Porcupine free tier (3 wake words, sufficient for POC)
- **Time**: 6 weeks (can be part-time, ~20 hours/week)

## Phase 2+
- 1 Android engineer (full-time)
- 1 ML engineer (model optimization, part-time)
- Expanded device testing pool
- Potential cloud infrastructure (Phase 4)

---

# 13. Measuring Success

## POC Success = Validation, Not Perfection

**This POC succeeds if it answers the question:**
*"Is an offline voice assistant practical enough to build further?"*

### Success Looks Like:
- ✅ Wake word detection works reliably enough
- ✅ Battery drain is acceptable for daily use
- ✅ Voice→action feels responsive
- ✅ Actually used for 2+ weeks without stopping
- ✅ Clear path to improvement (not fundamental blocking issues)

### Failure Looks Like:
- ❌ Wake word constantly triggers falsely or misses
- ❌ Battery drain >7% per hour (unusable)
- ❌ Latency >2 seconds (feels sluggish)
- ❌ Stopped using after 3 days (not actually useful)
- ❌ Fundamental issues (e.g., Android kills service constantly)

**Either outcome is valuable**. A well-documented failure saves months of building the wrong thing.

---

# 14. Appendix: Key Differences from Original PRD

## Scope Reduction (POC)
1. **Removed LLM from POC**: Simple keyword matching instead
2. **Simple voice confirmation**: Keyword matching for yes/no (not LLM-based understanding)
3. **Exact contact match only**: No disambiguation
4. **Single action only**: Just calling, no messaging/alarms
5. **Minimal error handling**: Basic messages only

## Technology Updates (2026)
1. **Native Android instead of Flutter**: Better battery/performance
2. **Android 14 (API 34) minimum**: Leverages latest Android features and security
3. **Jetpack Compose**: Modern declarative UI (2025 standard)
4. **Porcupine for wake word**: Industry-standard, proven
5. **Vosk for STT**: Lighter than Whisper, good enough for POC
6. **No LLM in POC**: Deferred to Phase 2 with Qwen3-0.6B
7. **Modern frameworks**: MLLM for future LLM integration, Hilt DI, DataStore
8. **Foreground service types**: Proper Android 14+ permission model

## Philosophy Shift
1. **Experimental mindset**: Validate before building
2. **Clear phase gates**: Stop if not working
3. **Honest success criteria**: "Would I use this?" not just "Does it work?"
4. **Scalable architecture**: But minimal features
5. **Fast failure acceptance**: 6 weeks to yes/no, not 6 months

---

# 15. Summary

Nova POC is a **6-week experimental validation** of offline voice assistance on Android. By focusing ruthlessly on one use case (hands-free calling), we'll learn whether the core interaction is viable. Clean architecture ensures easy scaling if validated, while the minimal scope prevents wasted effort if not.

**The real question isn't "Can we build this?" but "Should we?"**

This POC will answer that question.

---

**Next Steps:**
1. Review and approve this PRD
2. Set up Android project structure
3. Begin Week 1: Porcupine wake word integration

**Questions? Clarifications needed?**
