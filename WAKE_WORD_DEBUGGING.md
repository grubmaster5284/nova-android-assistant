# Wake Word Debugging Guide

## Overview

This document explains how to debug wake word detection issues in the Nova Assistant app. Comprehensive logging has been added throughout the wake word detection pipeline to help identify and resolve issues.

## Logcat Filtering

All wake word logs use the tag `NovaWakeWord` for easy filtering. Use one of these commands:

```bash
# View all wake word logs
adb logcat -s NovaWakeWord

# View wake word logs with timestamps
adb logcat -s NovaWakeWord -v time

# View wake word logs and save to file
adb logcat -s NovaWakeWord > wake_word_logs.txt

# View wake word logs with other system logs (useful for debugging)
adb logcat NovaWakeWord:* AndroidRuntime:E *:S
```

## Log Categories

The logging system uses emoji prefixes for easy visual scanning:

- 🔵 **SERVICE**: Service lifecycle events (onCreate, onStartCommand, onDestroy)
- 🟢 **PORCUPINE_INIT**: Porcupine engine initialization steps
- 🔴 **PORCUPINE_ERROR**: Porcupine-related errors
- ✅ **WAKE_WORD_DETECTED**: Wake word detection events
- 🎤 **AUDIO**: Audio processing events
- 🔐 **PERMISSION**: Permission checks
- 🔄 **STATE**: State changes

## Common Issues and Solutions

### 1. Wake Word Not Detected

**Symptoms:**
- Service starts but no wake word detection
- No logs showing "WAKE_WORD_DETECTED"

**Debugging Steps:**

1. **Check if service is running:**
   ```bash
   adb logcat -s NovaWakeWord | grep "SERVICE"
   ```
   Look for: `🔵 SERVICE: startWakeWordDetection`

2. **Check Porcupine initialization:**
   ```bash
   adb logcat -s NovaWakeWord | grep "PORCUPINE_INIT"
   ```
   Should see:
   - `🟢 PORCUPINE_INIT: Starting initialization`
   - `🟢 PORCUPINE_INIT: Creating PorcupineManager`
   - `🟢 PORCUPINE_INIT: PorcupineManager created successfully`
   - `🟢 PORCUPINE_INIT: Audio processing started`

3. **Check for errors:**
   ```bash
   adb logcat -s NovaWakeWord | grep "ERROR"
   ```

**Common Causes:**

- **Missing Access Key**: Porcupine requires a free access key from https://console.picovoice.ai/
  - Error: `Porcupine access key is not configured`
  - Solution: Get a free access key and set `PORCUPINE_ACCESS_KEY` in `WakeWordService.kt`

- **Permission Denied**: RECORD_AUDIO permission not granted
  - Error: `🔐 PERMISSION: android.permission.RECORD_AUDIO = DENIED`
  - Solution: Grant microphone permission in app settings

- **Activation Errors**: Porcupine activation failed
  - Error: `🔴 PORCUPINE_ERROR: Activation failed`
  - Solution: Check access key validity, internet connection (for first-time activation)

### 2. Service Not Starting

**Symptoms:**
- No service logs appearing
- App shows service as "stopped"

**Debugging Steps:**

1. **Check service lifecycle:**
   ```bash
   adb logcat -s NovaWakeWord | grep "SERVICE"
   ```

2. **Check Android system logs:**
   ```bash
   adb logcat | grep -i "WakeWordService\|ForegroundService"
   ```

**Common Causes:**

- **Foreground Service Permission**: Android 14+ requires `FOREGROUND_SERVICE_MICROPHONE` permission
- **Battery Optimization**: Device may be killing the service
  - Solution: Disable battery optimization for the app

### 3. False Positives / Too Many Detections

**Symptoms:**
- Wake word detected when not spoken
- Multiple detections for single utterance

**Solution:**
Adjust sensitivity in `WakeWordService.kt`:
```kotlin
.setSensitivity(0.3f) // Lower = less sensitive (fewer false positives)
```

### 4. No Audio Processing

**Symptoms:**
- Service running but no audio logs
- No "AUDIO" category logs

**Debugging Steps:**

1. **Check if Porcupine started:**
   ```bash
   adb logcat -s NovaWakeWord | grep "Audio processing started"
   ```

2. **Check microphone access:**
   ```bash
   adb logcat -s NovaWakeWord | grep "PERMISSION"
   ```

3. **Check Android audio system:**
   ```bash
   adb logcat | grep -i "audio\|microphone\|record"
   ```

## Expected Log Flow

When wake word detection is working correctly, you should see this log sequence:

```
🔵 SERVICE: onCreate - Service instance created
🔵 SERVICE: onStartCommand - action=START_WAKE_WORD
🔵 SERVICE: startWakeWordDetection - Starting wake word detection
🔐 PERMISSION: android.permission.RECORD_AUDIO = GRANTED
🟢 PORCUPINE_INIT: Starting initialization
🟢 PORCUPINE_INIT: Creating PorcupineManager - Using built-in keyword: Hey Nova
🟢 PORCUPINE_INIT: PorcupineManager created successfully
🟢 PORCUPINE_INIT: Starting audio processing
🟢 PORCUPINE_INIT: Audio processing started - Listening for wake word
🔄 STATE: running=false, listening=false -> running=true, listening=true
```

When wake word is detected:

```
✅ WAKE_WORD_DETECTED: keyword='Hey Nova', confidence=1.0
🎤 AUDIO: Wake word detected - keywordIndex=0
```

## Testing Wake Word Detection

1. **Start the service** (via app UI or adb):
   ```bash
   adb shell am startservice -n com.ctrlbsketr.novaassistant/.data.service.WakeWordService
   ```

2. **Monitor logs in real-time:**
   ```bash
   adb logcat -s NovaWakeWord -v time
   ```

3. **Say "Hey Nova"** and watch for detection logs

4. **Check detection rate:**
   - Count successful detections
   - Note any false positives
   - Measure latency (time from utterance to detection log)

## Performance Monitoring

Monitor these metrics in logs:

- **Initialization Time**: Time from service start to "Audio processing started"
- **Detection Latency**: Time from wake word utterance to detection log
- **False Positive Rate**: Count of detections when wake word not spoken
- **False Negative Rate**: Count of missed detections when wake word is spoken

## Additional Debugging Commands

```bash
# Check if service is running
adb shell dumpsys activity services | grep WakeWordService

# Check app permissions
adb shell dumpsys package com.ctrlbsketr.novaassistant | grep permission

# Monitor battery usage (wake word should be low)
adb shell dumpsys batterystats | grep novaassistant

# Check foreground services
adb shell dumpsys activity services | grep -A 5 "foreground"
```

## Getting Help

If issues persist after checking logs:

1. **Collect full logcat:**
   ```bash
   adb logcat -s NovaWakeWord:* AndroidRuntime:E > debug_logs.txt
   ```

2. **Check Porcupine documentation:**
   - https://picovoice.ai/docs/porcupine/
   - https://github.com/Picovoice/porcupine

3. **Verify access key:**
   - Ensure access key is valid at https://console.picovoice.ai/
   - Check if access key has expired or reached usage limits

## Configuration

Key configuration points in `WakeWordService.kt`:

- **Access Key**: `PORCUPINE_ACCESS_KEY` constant
- **Sensitivity**: `.setSensitivity(0.5f)` in `initializePorcupine()`
- **Wake Word**: `Porcupine.BuiltInKeyword.HEY_NOVA` (can be changed to other built-in keywords)

## Next Steps

After wake word detection is working:

1. Test in different environments (quiet, noisy, different distances)
2. Measure battery drain (target: <3% per hour)
3. Test accuracy (target: >85% in quiet environments)
4. Adjust sensitivity based on results

