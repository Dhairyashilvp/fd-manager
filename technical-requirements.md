# Technical Requirements — FD-Tracker (Current Codebase)

This document reflects the **current implemented state** of the FD-Tracker repository (dependencies, tooling, and runtime behavior), not an aspirational future target.

---

## 1. Platform & SDK Targets

| Parameter | Value |
| --- | --- |
| **Platform** | Android (native) |
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Minimum SDK** | API 26 |
| **Target SDK** | API 35 |
| **Compile SDK** | API 35 |
| **Architecture** | Multi-module clean architecture, MVVM + UDF |
| **Build System** | Gradle wrapper 8.14.5 (`gradle-wrapper.properties`) |
| **Android Gradle Plugin** | 8.13.2 (`libs.versions.toml`) |
| **JDK Target** | 17 |

### Compatibility Notes

- `coreLibraryDesugaring` is enabled in `app` and `core:data`.
- `minSdk 26` enables `java.time` usage across domain/data layers.
- Dynamic Material You theming is enabled on Android 12+ (`Build.VERSION_CODES.S`).

---

## 2. Core Dependencies & Versions

All versions are managed in `gradle/libs.versions.toml`.

### 2.1 Dependencies actively used in current runtime flow

| Dependency | Version | Current usage |
| --- | --- | --- |
| Compose BOM / UI / M3 | 2024.12.01 (BOM) | All feature UIs |
| Activity Compose | 1.9.3 | `MainActivity.setContent {}` |
| Navigation Compose | 2.8.5 | Type-safe route navigation |
| Lifecycle Compose | 2.8.7 | `collectAsStateWithLifecycle`, ViewModel bindings |
| Core KTX / Splashscreen | 1.15.0 / 1.0.1 | App foundation |
| Room (runtime/ktx/compiler) | 2.6.1 | FD, bank, reminder, SMTP config persistence |
| Hilt + Hilt Work | 2.52 / 1.2.0 | DI across app + workers |
| Coroutines | 1.9.0 | Use cases, repositories, ViewModels, worker logic |
| WorkManager KTX | 2.10.0 | Periodic reminder checks |
| ML Kit Text Recognition | 16.0.1 | OCR extraction from selected image/bitmap |
| Jakarta Mail | 2.0.1 | SMTP reminder email sending |
| Kotlinx Serialization JSON | 1.7.3 | Route serialization + JSON helpers |
| DataStore Preferences | 1.1.1 | User profile and app settings |
| Biometric | 1.2.0-alpha05 | Startup app-lock authentication |

### 2.2 Dependencies declared but not currently wired into primary runtime path

| Dependency | Version | Current status |
| --- | --- | --- |
| CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) | 1.4.1 | Declared; current OCR capture flow uses `ActivityResultContracts.GetContent()` (gallery/SAF), not a CameraX preview pipeline |
| Vico (`compose-m3`) | 2.0.1 | Declared in some feature modules; charts currently implemented with custom Compose components |
| Compose Calendar | 2.6.1 | Declared in calendar module; calendar UI currently uses custom Compose grid implementation |
| Security Crypto | 1.1.0-alpha06 | Declared; not currently used by repository/storage code |

### 2.3 Current SMTP storage behavior

- SMTP configuration is persisted via Room (`smtp_config` table in `core:data`).
- Credentials are **not currently encrypted at rest** in the implemented repository path.

---

## 3. Build Tooling & Plugins

| Tool / Plugin | Version | Source |
| --- | --- | --- |
| **Gradle Wrapper** | 8.14.5 | `gradle/wrapper/gradle-wrapper.properties` |
| **Android Gradle Plugin** | 8.13.2 | `libs.versions.toml` |
| **Kotlin** | 2.0.21 | `libs.versions.toml` |
| **KSP** | 2.0.21-1.0.28 | `libs.versions.toml` |
| **Hilt Plugin** | 2.52 | `libs.versions.toml` |
| **Compose Compiler Plugin** | Kotlin plugin managed | Root `build.gradle.kts` |
| **Kotlin Serialization Plugin** | 2.0.21 | Root `build.gradle.kts` |
| **Core Library Desugaring** | 2.1.3 | App + core:data modules |

### Current Module Topology

- `:app`
- `:core:common`, `:core:domain`, `:core:data`, `:core:ui`
- `:feature:dashboard`, `:feature:fdlist`, `:feature:fddetail`, `:feature:calendar`, `:feature:ocr`, `:feature:tax`, `:feature:strategy`, `:feature:settings`

### Room Versioning in Code

- `FdTrackerDatabase` is currently at **version 2**.
- Explicit migration path `MIGRATION_1_2` is registered in `DatabaseModule`.

---

## 4. Pre-requisites

### 4.1 Development Environment

| Requirement | Minimum |
| --- | --- |
| IDE | Android Studio with AGP 8.13.x support |
| JDK | JDK 17 |
| OS | macOS / Windows / Linux with Android SDK |
| RAM | 8 GB (16 GB recommended) |
| Disk | ~10 GB free (SDK + Gradle cache + build outputs) |

### 4.2 Android SDK Components

- Android SDK Platform 35
- Build tools compatible with AGP 8.13.x
- Emulator/device running API 26+
- Platform-tools (`adb`)

### 4.3 SMTP Runtime Configuration (from Settings screen)

| Parameter | Notes |
| --- | --- |
| SMTP Host | Required |
| SMTP Port | Required (`587` default in UI) |
| Username / Password | Required by provider |
| From Address / To Address | Required by app workflow |
| TLS toggle | Supported via `useTls` |

Gmail requires app-password based authentication when 2FA is enabled.

---

## 5. Feature-to-Dependency Mapping

| Feature | Current implementation in code |
| --- | --- |
| Manual FD entry/edit | Compose forms + `EditFdViewModel` + Room persistence |
| OCR ingestion | SAF content picker (`image/*`, `application/pdf`) + ML Kit text recognition |
| OCR review to FD prefill | Parsed/editable OCR fields are passed to `EditFdRoute` and consumed by `EditFdViewModel` |
| Dashboard KPIs + exposures | Compose components + domain use cases + repository flows |
| Maturity calendar | Custom Compose calendar grid (`MaturityCalendar`) |
| Cash-flow chart | Custom Compose bar chart (`CashFlowChart`) |
| Reminder scheduling | Periodic WorkManager worker (`ReminderCheckWorker`) every 6 hours |
| Push reminders | Notification channel + `NotificationManager` from worker |
| Email reminders | Jakarta Mail SMTP service from worker using stored SMTP config |
| Tax tools (TDS + Form 15G/H) | Domain use cases + settings profile data |
| Strategy tools | Laddering analysis + break-FD recommendations in domain layer |
| App lock | BiometricPrompt gate in `MainActivity` based on DataStore flag |
| Settings/profile | DataStore (profile + toggles), Room (`smtp_config`) |
| Dark mode preference | Persisted in DataStore; app theme currently still follows system default unless separately wired |
| Account aggregator sync | Not implemented |

---

## 6. Testing Stack

### Current state in repository

- Version catalog defines testing library versions (JUnit5, Mockk, Turbine, Truth, Espresso, Room/Hilt/Work test artifacts).
- Module `build.gradle.kts` files currently do **not** declare `testImplementation` / `androidTestImplementation` dependencies.
- No CI-enforced automated test workflow is configured in this repository snapshot.

### Practical verification command (currently used)

```bash
./gradlew :app:compileDebugKotlin :core:data:compileDebugKotlin :feature:fddetail:compileDebugKotlin :feature:settings:compileDebugKotlin :feature:ocr:compileDebugKotlin :feature:fdlist:compileDebugKotlin :core:domain:compileKotlin
```

---

## 7. Permissions

Declared in `app/src/main/AndroidManifest.xml`:

| Permission | Runtime / special access | Current usage |
| --- | --- | --- |
| `android.permission.CAMERA` | Runtime | Requested in OCR screen before image selection flow |
| `android.permission.INTERNET` | Normal | SMTP email sending |
| `android.permission.POST_NOTIFICATIONS` | Runtime (API 33+) | Push maturity reminders |
| `android.permission.SCHEDULE_EXACT_ALARM` | Special access | Declared; exact alarm scheduling path is not currently implemented |
| `android.permission.USE_EXACT_ALARM` | Normal (declared) | Declared; exact alarm scheduling path is not currently implemented |
| `android.permission.USE_BIOMETRIC` | Normal | Used by startup app-lock flow |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Normal | `BootReceiver` reinitializes WorkManager reminders |
| `android.permission.FOREGROUND_SERVICE` | Normal | Declared; app currently uses WorkManager worker, not foreground service |
| `android.permission.FOREGROUND_SERVICE_DATA_SYNC` | Normal | Declared; currently unused |
| `android.permission.WAKE_LOCK` | Normal | Used indirectly by WorkManager |

---

## 8. ProGuard / R8 Rules

Current release config (`app/build.gradle.kts`) has `isMinifyEnabled = true`.

Existing `app/proguard-rules.pro` includes explicit rules for:

- Jakarta Mail / Jakarta Activation
- Kotlinx Serialization generated serializers

Room/Hilt rely on their generated/bundled keep behavior plus AGP defaults.

---

## 9. CI/CD Considerations

Current repository status:

- No CI workflow files are part of this code snapshot.
- No Detekt/ktlint plugin integration is configured.
- Recommended baseline for CI (not yet implemented in repo):
  - `./gradlew lint`
  - `./gradlew test`
  - `./gradlew assembleRelease`

---

## 10. Current Constraints & Follow-ups

| Area | Current status | Follow-up |
| --- | --- | --- |
| SMTP credential security | Stored in Room via `smtp_config` | Move secrets to encrypted storage path (e.g., Security Crypto) |
| Dark mode setting | Preference is persisted in DataStore | Wire `Settings` dark-mode state into `FdTrackerTheme(darkTheme=...)` |
| Alarm permissions | Declared in manifest | Either implement exact alarm scheduling or remove unused permissions |
| CameraX / Vico / Compose Calendar | Declared dependencies | Remove unused libs or migrate UI/features to use them |
| Automated testing | No active test dependencies configured in modules | Add `testImplementation`/`androidTestImplementation` and CI checks |

---

## 11. Last Verified

- Document aligned with repository state after recent implementation updates (OCR handoff, DOB picker, biometric app lock, migration registration, table layout fix, converter and precision updates).
