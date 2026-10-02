# AlarmApp (Neon Clock)

A modern, feature-rich Clock and Alarm application for Android built entirely with **Kotlin** and **Jetpack Compose**. This project features a highly aesthetic, custom UI utilizing "Glassmorphism" design principles, neon glowing accents, and smooth canvas-based animations. 

It implements core clock functionalities—Alarms, Timers, and Stopwatches—backed by robust foreground services to ensure accurate timekeeping and reliable notifications even when the app is minimized or the screen is locked.

## 🌟 Features

### ⏰ Alarm
*   **Custom Scheduling:** Create, edit, and delete alarms with specific day-of-the-week repetition.
*   **Persistent Storage:** Alarms are saved locally using **Room Database**.
*   **Reliable Triggers:** Utilizes `AlarmManager` for precise scheduling and a Foreground Service (`AlarmService1`) to ensure alarms fire reliably.
*   **Lock-Screen Waking:** Employs Full-Screen Intents to wake the device and display the ringing UI (`AlarmAlertActivity`) over the lock screen.
*   **Custom UI:** Glassmorphism list items with integrated toggles to easily enable/disable alarms.

### ⏳ Timer
*   **Neon Circular UI:** Features a custom-drawn glowing neon progress arc (`NeonProgressArc`) built using Compose `Canvas`.
*   **Background Execution:** Uses a Foreground Service (`TimerForegroundService`) to keep the countdown active when the app is closed.
*   **Live Notification:** Displays a real-time progress bar directly in the system notification drawer.
*   **Quick Keypad:** Custom keypad interface for quickly setting hours, minutes, and seconds.

### ⏱️ Stopwatch
*   **Stadium Display:** Unique "stadium-shaped" UI layout for clear visibility.
*   **Lap Tracking:** Record and display split times in a scrollable, glass-styled list.
*   **Service-Backed:** State is maintained by `StopWatchForegroundService`, ensuring the stopwatch doesn't reset if the activity is destroyed.

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose
*   **Dependency Injection:** Koin (`koin-android`, `koin-androidx-compose`)
*   **Local Database:** Room Database (with custom TypeConverters using Gson for `LocalDateTime`)
*   **Navigation:** Jetpack Navigation 3 (Experimental) (`androidx.navigation3:navigation3-runtime`)
*   **Concurrency:** Kotlin Coroutines & StateFlow (Event Buses for Timer/Stopwatch state propagation)
*   **Serialization:** Kotlinx Serialization & Gson
*   **Architecture:** MVVM (Model-View-ViewModel)

## 🎨 UI/UX Highlights

This app moves away from standard Material Design components to implement a highly customized aesthetic:
*   **Glassmorphism:** Transparent panels with blurred backgrounds and subtle borders (`GlassCard`, `GlassBottomNavigation`).
*   **Neon & Glow Effects:** Custom modifiers (`Modifier.neonGlow`) and animated radial gradient blobs simulating ambient light.
*   **Fluid Animations:** Infinite transitions for pulsing buttons, bouncing icons, and custom loaders (`Grid3x3Loader`).

## 🔐 Permissions Required

To function correctly on modern Android versions (Android 12+ / API 31+), this app requires the following permissions (declared in `AndroidManifest.xml`):
*   `POST_NOTIFICATIONS`: To show live timer progress and stopwatch states.
*   `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: To ensure alarms ring at the exact specified time.
*   `WAKE_LOCK` / `USE_FULL_SCREEN_INTENT`: To wake the device screen when an alarm rings.
*   `FOREGROUND_SERVICE` (Media Playback & Data Sync): To run timers and stopwatches accurately in the background.

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Latest stable version recommended)
*   JDK 11 or higher
*   Android SDK API Level 35 (Target SDK) / Minimum API 29

### Installation
1.  Clone the repository to your local machine.
2.  Open the project in Android Studio.
3.  Sync the project with Gradle files (this will download Koin, Room, Compose BOM, and Navigation 3 dependencies).
4.  Build and run the app on an emulator or physical device. *Note: Grant notification and exact alarm permissions when prompted for the app to function correctly.*
