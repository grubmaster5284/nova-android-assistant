# Nova Assistant - TODO Checklist

**Last Updated:** January 27, 2026  
**Project:** Nova — Offline Voice Assistant (Experimental POC)

---

## Phase 1: Proof of Concept (POC) - 6 Weeks

### Week 1: Project Setup & Wake Word

#### Project Structure
- [x] Android Studio project structure
- [x] Clean Architecture setup (MVVM + Compose)
- [x] Hilt dependency injection
- [x] Jetpack Compose UI framework
- [x] Navigation setup

#### Wake Word Detection
- [x] Porcupine SDK integration
- [x] Foreground service skeleton (`WakeWordService`)
- [x] Basic MainActivity with start/stop
- [x] Service lifecycle management
- [x] Wake word detection implementation
- [x] Service status broadcasting
- [x] Comprehensive logging system
- [ ] Custom notification icon (currently using default)

### Week 2: Wake Word Validation

#### Testing & Validation
- [ ] Battery testing (24-48 hour test)
- [ ] Battery drain measurement implementation
- [ ] Accuracy testing (100 attempts)
- [ ] Wake word accuracy >85% in quiet room
- [ ] Battery drain <5% per hour
- [ ] **GATE 1: Go/No-go decision on wake word**

#### Documentation
- [x] Wake word debugging guide (`WAKE_WORD_DEBUGGING.md`)
- [ ] Battery measurement results documentation
- [ ] Known limitations document

### Week 3: STT Integration

#### Speech-to-Text Setup
- [ ] Vosk setup and model download
- [ ] Audio capture service after wake word
- [ ] STT engine integration
- [ ] Test transcription accuracy
- [ ] STT accuracy >80% for "call [name]"

#### Audio Pipeline
- [ ] Audio capture implementation
- [ ] Audio buffer management
- [ ] Audio format configuration

### Week 4: Calling Pipeline

#### Intent Extraction
- [ ] Keyword extraction (regex matching)
- [ ] Parse "call [name]" pattern
- [ ] Extract contact name from utterance
- [ ] Intent extraction use case

#### Contact Management
- [ ] Contact repository implementation
- [ ] Android contacts access
- [ ] Exact match contact lookup
- [ ] Contact model/data classes
- [ ] READ_CONTACTS permission handling

#### Voice Confirmation Flow
- [ ] TTS integration for confirmation questions
- [ ] Listen for Yes/No after confirmation
- [ ] Keyword matching for yes/no responses
- [ ] Confirmation handler implementation
- [ ] Visual feedback on screen (contact info shown)

#### Call Execution
- [ ] Native call placement (ACTION_CALL intent)
- [ ] CALL_PHONE permission handling
- [ ] TTS confirmation: "Calling [name]"
- [ ] Call placement use case
- [ ] End-to-end latency <1.5s
- [ ] **GATE 2: Go/No-go on voice→action**

### Week 5: TTS & Polish

#### Text-to-Speech
- [ ] Android TTS integration
- [ ] TTS for confirmation questions ("Call [name]?")
- [ ] TTS for responses ("Calling [name]", "Call cancelled")
- [ ] TTS error messages
- [ ] TTS latency <300ms for short responses
- [ ] Stop TTS playback on interrupt (TODO in `BetaMainViewModel.kt`)

#### Error Handling
- [ ] "I didn't catch that" (STT failure)
- [ ] "I didn't find [name]" (no contact match)
- [ ] "Say again?" (retry prompt)
- [ ] Service restart on crash
- [ ] Error state management in UI

#### Settings Screen
- [ ] Settings screen UI
- [ ] Enable/disable toggle
- [ ] Sensitivity adjustment
- [ ] Audio feedback toggle
- [ ] Settings persistence (DataStore)

#### UI Polish
- [ ] Audio feedback beep on wake word (TODO in `BetaMainScreen.kt`)
- [ ] Error beep sound (TODO in `BetaMainScreen.kt`)
- [ ] Toggle captions functionality (TODO in `TopControlsPill.kt`)
- [ ] Volume control (TODO in `TopControlsPill.kt`)
- [ ] Play beep sound via AudioFeedbackManager (TODO in `BetaMainScreen.kt`)

### Week 6: Testing & Decision

#### Real-World Testing
- [ ] Real-world usage testing (2 weeks)
- [ ] Used successfully for 10+ calls over 2 weeks
- [ ] Feels faster than unlocking phone
- [ ] Battery drain acceptable for daily use
- [ ] Manual testing checklist
- [ ] Real-world usage diary

#### Performance Benchmarks
- [ ] Latency benchmarking
- [ ] Wake word CPU usage <7% on mid-range devices
- [ ] STT latency <500ms for 2-3 second utterances
- [ ] Total end-to-end: <1.5 seconds

#### Documentation
- [ ] Setup instructions (Android Studio, dependencies)
- [ ] Testing guide (how to test wake word, calling)
- [ ] Battery drain report (24-hour test)
- [ ] Latency benchmarks
- [ ] Go/No-go decision document
- [ ] **GATE 3: Go/No-go on Phase 2**

---

## Code TODOs (From Codebase)

### BetaMainViewModel.kt
- [ ] Stop TTS playback on interrupt (line 135)
- [ ] Start recording user speech after wake word (line 182)
- [ ] Send to STT and LLM (line 189)
- [ ] Play TTS for assistant responses (line 199)

### BetaMainScreen.kt
- [ ] Play beep sound via AudioFeedbackManager (line 82)
- [ ] Play error beep (line 86)

### TopControlsPill.kt
- [ ] Toggle captions functionality (line 46)
- [ ] Volume control (line 57)

### WakeWordService.kt
- [ ] Add custom notification icon (line 385)
- [ ] Measure actual battery impact (line 412)

---

## Phase 1 Deliverables Checklist

### Minimum Viable Features
- [x] Wake word detection service (Porcupine)
- [ ] Offline STT pipeline (Vosk)
- [ ] Keyword-based intent extraction
- [ ] Contact lookup (exact match)
- [ ] Voice-based call confirmation (yes/no keyword matching)
- [ ] Native call placement
- [ ] Basic TTS responses
- [ ] Simple error messages
- [ ] Settings screen (enable/disable, sensitivity)

### Documentation
- [x] Setup instructions (Android Studio, dependencies) - Partial
- [x] Testing guide (how to test wake word, calling) - Partial (`WAKE_WORD_DEBUGGING.md`)
- [ ] Battery measurement results
- [ ] Latency benchmarks
- [ ] Known limitations

### Testing Deliverables
- [ ] Manual testing checklist
- [ ] Battery drain report (24-hour test)
- [ ] Wake word accuracy test (100 attempts)
- [ ] Real-world usage diary (2 weeks)
- [ ] Go/No-go decision document

---

## Success Metrics (Validation Gates)

### Gate 1: Wake Word (Week 2)
- [ ] Detection accuracy >85% in quiet room
- [ ] Battery drain <5% per hour
- [ ] **Status:** ⏳ Pending

### Gate 2: Voice→Action (Week 4)
- [ ] End-to-end latency <1.5s
- [ ] STT accuracy >80% for "call [name]"
- [ ] **Status:** ⏳ Pending

### Gate 3: Real-World Test (Week 6)
- [ ] Used successfully for 10+ calls over 2 weeks
- [ ] Feels faster than unlocking phone
- [ ] Battery drain acceptable for daily use
- [ ] **Status:** ⏳ Pending

---

## Phase 2: Intelligent Conversation (Post-POC)

**Prerequisites:** POC validated (Gate 3 passed)

### Enhanced Natural Language Understanding
- [ ] Integrate lightweight on-device LLM (Qwen3-0.6B or Gemma 2B)
- [ ] Intent classification with confidence scores
- [ ] Context extraction from natural speech
- [ ] Multi-intent handling
- [ ] MLLM framework integration

### Advanced Confirmation Flows
- [ ] LLM-powered understanding of confirmations (beyond keyword matching)
- [ ] Context-aware follow-up questions
- [ ] Handling ambiguous responses ("maybe", "I think so")
- [ ] Multi-step confirmations for complex actions

### Contact Disambiguation
- [ ] Handle multiple matches intelligently
- [ ] "Which John? John Smith or John Doe?"
- [ ] Multiple phone numbers per contact
- [ ] Smart defaults (recent calls, favorites)

### Improved Error Recovery
- [ ] Clarifying questions
- [ ] Suggest corrections
- [ ] Retry logic with guidance

### Custom Wake Word Training
- [ ] Custom wake word training feature
- [ ] User-specific wake word models

**Deliverable:** Natural conversation for calling without screen interaction

---

## Phase 3: Essential Actions (Post-Phase 2)

**Prerequisites:** Phase 2 complete

### Messaging
- [ ] Send SMS messages
- [ ] Send WhatsApp messages
- [ ] Read recent messages
- [ ] Reply to messages

### Time Management
- [ ] Set alarms
- [ ] Set timers
- [ ] Create reminders
- [ ] Check calendar

### Information Queries
- [ ] "What time is it?"
- [ ] "What's my next meeting?"
- [ ] "Battery level?"

### Quick Actions
- [ ] Toggle WiFi
- [ ] Toggle Bluetooth
- [ ] Other device controls

**Deliverable:** Core device control suite

---

## Phase 4: Online Intelligence (Post-Phase 3)

**Prerequisites:** Phase 3 complete

### Web Search
- [ ] Online search queries
- [ ] Weather information
- [ ] News updates

### Cloud LLM Integration
- [ ] Complex question answering
- [ ] Creative tasks
- [ ] Advanced reasoning
- [ ] Cloud LLM fallback

### Navigation
- [ ] Directions and ETA
- [ ] Traffic updates
- [ ] Android Auto integration

### Memory & Personalization
- [ ] Conversation history
- [ ] User preferences
- [ ] Learning from interactions
- [ ] Optional cloud sync (encrypted)

**Deliverable:** Full-featured personal assistant

---

## Phase 5: Advanced Features (Future)

- [ ] Multi-modal inputs (screen context, image, OCR)
- [ ] Smart home integration
- [ ] Proactive suggestions
- [ ] Routine automation
- [ ] Voice customization
- [ ] Multiple language support
- [ ] Wear OS companion

---

## Notes

- ✅ = Completed
- [ ] = Pending
- ⏳ = In Progress
- ❌ = Blocked/Failed

**Legend:**
- Items marked with `[x]` are completed
- Items marked with `[ ]` are pending
- Code TODOs are tracked separately in the "Code TODOs" section
- Validation gates must pass before proceeding to next phase

---

## Quick Reference

**Current Phase:** Phase 1 (POC) - Week 2-3  
**Next Milestone:** Gate 1 validation (Wake Word)  
**Blockers:** None identified  
**Priority Focus:** STT integration and calling pipeline
