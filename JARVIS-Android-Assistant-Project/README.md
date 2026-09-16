# J.A.R.V.I.S. — Personal Android AI Operating Assistant

> **"Always Here For You."**  
> An independent, privacy-first, modular Android AI assistant designed for real on-device operation, smart hardware control, persistent memory, and multi-provider AI reasoning.

---

## 1. System Overview

J.A.R.V.I.S. is built with native Android tooling (Kotlin, Jetpack Compose, Room Database, Coroutines, WorkManager, OkHttp) and incorporates a multi-layer intent execution architecture:

```
[ User Input (Voice / Text) ]
              │
              ▼
    [ Speech Engine ] ──> SpeechRecognizer (English, Hindi, Hinglish)
              │
              ▼
    [ Intent Router ]
     ├── 1. Math / Calculator ──────────> Instant Local Evaluation
     ├── 2. Deterministic Command Engine ─> Battery, Flashlight, Volume, Time, App Launcher
     ├── 3. Persistent Memory Vault ─────> Keyword & Context Retrieval (Room DB)
     ├── 4. Reminders & Tasks ───────────> AlarmManager & High-Priority Notifications
     ├── 5. Accessibility Automation ───> Gestures, Screen OCR, Navigation
     └── 6. Multi-Provider AI Brain ────> Gemini / OpenAI / OpenRouter / Local LAN
              │
              ▼
     [ Natural Voice Synthesis ] ───────> Android TextToSpeech (Pitch/Rate controls)
```

---

## 2. Key Capabilities & Commands

### A. Local Hardware & Device Controls
- **Battery**: `"Jarvis, what's my battery?"` / `"Battery percentage"` / `"Battery kitni hai"`
- **Flashlight**: `"Turn on flashlight"` / `"Flashlight off"` / `"Torch chalu karo"`
- **Volume**: `"Volume up"` / `"Volume down"` / `"Awaz badhao"`
- **Time & Date**: `"What time is it?"` / `"Today's date"` / `"What day is it?"`
- **Settings**: `"Open settings"` / `"Open Wi-Fi settings"` / `"Open Bluetooth settings"`

### B. App Launching & Discovery
- Dynamically discovers installed apps via Android `PackageManager`.
- Commands: `"Open YouTube"`, `"Launch Spotify"`, `"WhatsApp kholo"`, `"Camera open karo"`.
- Asks for clarification if multiple matches exist.

### C. Persistent Memory System
- **Store**: `"Remember that my Minecraft project is called Sub-Terra."`
- **Recall**: `"What do you remember about my project?"` / `"Show my memories"`
- **Vault**: Search, inspect, and delete memories in the dedicated Memory Screen.

### D. Scheduled Reminders & Alerts
- Natural language parsing for time delays.
- Commands: `"Remind me in 10 minutes to check the server"`, `"Remind me to study"`.
- Schedules exact alarms via Android `AlarmManager` with waking broadcast receiver.

### E. Calculator & Fast Math
- Local evaluation without external AI latency.
- Commands: `"25 times 8"`, `"100 divided by 4"`, `"20 percent of 500"`, `"500 ka 20 percent"`.

### F. Android Accessibility Automation (Legitimate & Safe)
- **Navigation**: `"Go home"`, `"Go back"`, `"Show recent apps"`, `"Take a screenshot"`.
- **Screen Interaction**: `"Scroll down"`, `"Scroll up"`, `"Tap Continue"`.
- **Screen Reading**: `"What does this screen say?"`.
- **Safety Policy**: Strictly blocks automated taps inside banking, wallet, authentication, or password entry screens.

### G. Notification Assistant
- Read active notifications across all apps with summary counts.
- Command: `"Read my notifications"` / `"Do I have any messages?"`.

### H. Multi-Provider AI Brain & Tool Calling
- **Supported Providers**:
  1. Google Gemini (`gemini-1.5-flash`, `gemini-1.5-pro`)
  2. OpenAI (`gpt-4o-mini`, `gpt-4o`)
  3. OpenRouter (`llama-3.1-8b`, `claude-3.5-sonnet`)
  4. Local LAN / Ollama (`http://<ip>:11434/v1`)
- Stored with AES-256 GCM hardware-backed `EncryptedSharedPreferences`.
- Structured tool execution for web search, YouTube queries, app launches, reminders, and memory storage.

---

## 3. UI / UX Design

Inspired by modern sci-fi interfaces:
- **Central Animated Core (J-Orb)**:
  - Concentric rotating segmented rings.
  - Dynamic scaling reactive to microphone audio dB.
  - State indicators: Idle (Cyan), Listening (Cyan Glow), Thinking (Pulsing Amber), Speaking (Dynamic Waveform), Executing (Deep Blue), Error (Red).
- **Reactive Audio Waveform**: Smooth multi-harmonic sine visualizer.
- **System Status Bar**: Real-time status indicators for AI, Voice, Memory, and Automation.
- **Bottom Navigation**: Home, Chat, Tools, Memory, Alerts, Settings, Diagnostics.

---

## 4. Building the Project

### Requirements
- Android Studio Iguana (2023.2.1) or Ladybug (2024.2.1+)
- JDK 17+
- Android SDK 34 (Upside Down Cake / Android 14)
- Minimum SDK: 26 (Android 8.0 Oreo)

### Compilation Steps
1. Open Android Studio.
2. Select **Open** and select the `jarvis-android` folder.
3. Allow Gradle to sync dependencies.
4. To build the Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`
5. To build Release APK:
   ```bash
   ./gradlew assembleRelease
   ```
6. To run unit tests:
   ```bash
   ./gradlew test
   ```

---

## 5. Security & Privacy Safeguards

- **Zero Unprompted Data Transmission**: Local commands and math queries are evaluated entirely on device.
- **Confirmation Policy**: High-risk actions (phone calls, sending messages, deleting memories, system resetting) require explicit user confirmation.
- **Encrypted Keystore**: API keys are never stored in plain text or hardcoded in source code.
- **Audit Log**: Every voice command, text command, and automated tool execution is recorded in an inspectable local database.
