# Nova Android Assistant

An offline-first voice assistant for Android using Porcupine wake word detection and Clean Architecture.

## Overview

Nova is a proof-of-concept Android voice assistant that demonstrates:
- Offline wake word detection using Porcupine
- Feature-based Clean Architecture
- Foreground service for background listening
- Audio device management (Bluetooth, headphones)
- Settings persistence with DataStore
- Modern UI with Jetpack Compose

## Features

- **Wake Word Detection**: "Hey Nova" wake word detection using Porcupine
- **Background Service**: Continues listening even when app is backgrounded
- **Audio Management**: Automatic audio routing to Bluetooth devices and headphones
- **Settings**: Customizable sensitivity and audio feedback
- **Modern UI**: Material Design 3 with animated orb visualizations

## Architecture

This project follows **feature-based Clean Architecture** with modular organization:

```
novaassistant/
├── MainActivity.kt
├── NovaApplication.kt
├── di/                     # Dependency injection modules
├── core/                   # Shared infrastructure
│   ├── config/
│   ├── navigation/
│   ├── theme/
│   ├── ui/
│   └── util/
└── features/               # Feature modules
    ├── wakeword/           # Wake word detection
    ├── settings/           # User preferences
    ├── audio/              # Audio management
    └── assistant/          # Main UI
```

Each feature follows Clean Architecture layers:
- **domain/**: Pure Kotlin business logic (models, repositories, use cases)
- **data/**: Data sources and repository implementations
- **presentation/**: UI (ViewModels, Composables)

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed documentation.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: Clean Architecture, MVVM
- **DI**: Hilt
- **Persistence**: DataStore
- **Wake Word**: Porcupine
- **Audio**: Android AudioRecord

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 24+
- Porcupine access key (for wake word detection)

### Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd nova_android_assistant
```

2. Open in Android Studio

3. Add your Porcupine access key in `local.properties`:
```properties
PORCUPINE_ACCESS_KEY=your-access-key-here
```

4. Build and run the app

### Permissions

The app requires the following permissions:
- **RECORD_AUDIO**: For wake word detection
- **FOREGROUND_SERVICE**: For background listening
- **POST_NOTIFICATIONS**: For foreground service notification

## Project Structure

### Features

#### Wake Word (`features/wakeword/`)
- Porcupine integration
- Android foreground service
- Wake word event broadcasting
- Service status management

#### Settings (`features/settings/`)
- User preferences with DataStore
- Sensitivity adjustment
- Audio feedback toggles

#### Audio (`features/audio/`)
- Audio device detection
- Bluetooth/headphone routing
- Sound feedback playback

#### Assistant (`features/assistant/`)
- Main UI screen
- State management
- Visual feedback (orb animations)

### Core (`core/`)
- **config/**: App configuration
- **navigation/**: Navigation graph
- **theme/**: Material Design theme
- **ui/**: Shared UI components
- **util/**: Shared utilities

### Dependency Injection (`di/`)
- Feature-specific DI modules
- Root-level application dependencies

## Adding New Features

To add a new feature:

1. Create feature package: `features/yourfeature/`
2. Add Clean Architecture layers:
   - `domain/` (models, repository interfaces, use cases)
   - `data/` (data sources, repository implementations)
   - `presentation/` (ViewModels, Composables)
3. Create DI module: `di/YourFeatureModule.kt`
4. Add navigation if needed

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed guidelines.

## Testing

Run tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## License

[Add your license here]

## Acknowledgments

- [Picovoice Porcupine](https://github.com/Picovoice/porcupine) for wake word detection
