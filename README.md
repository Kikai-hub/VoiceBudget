# VoiceBudget

Record an expense or income transaction by voice in a few seconds — no registration, no backend, fully offline.

Say **"Coffee 350"** or **"Кофе 350"**, confirm the parsed amount/category, and it's saved to a local database.

## Features

- **Voice input** — Android `SpeechRecognizer`, with a confirmation step before anything is saved.
- **Offline rule-based parser** — no AI/network calls. Understands Russian and English (`Coffee 350`, `Такси 850`, `Зарплата 120000`, ...).
- **Dashboard** — current month income/expense/balance, recent transactions.
- **Transactions** — full list with category and date-range filters, edit, delete.
- **Statistics** — expense breakdown (pie chart) and income vs. expense trend (bar chart), built with [Vico](https://patrykandpatrick.com/vico/).
- **Settings** — currency, voice recognition language (RU/EN), light/dark/system theme, CSV export/import via the system file picker (Storage Access Framework), clear all data.

## Architecture

Clean Architecture + MVVM, as required by the spec:

```
presentation/   Compose UI screens + ViewModels (one per screen)
domain/         Models, use cases, the voice-input parser — no Android dependencies
data/           Room database, repository implementations, CSV (de)serialization
di/             Hilt modules wiring data implementations to domain interfaces
```

- **Domain → Data**: `domain/repository` declares interfaces (`TransactionRepository`, `SettingsRepository`); `data/repository` implements them on top of Room / DataStore. ViewModels never see Room or DataStore types directly.
- **Parser**: `domain/parser/TransactionParser` is a pure Kotlin class — tokenizes input on Unicode letters (so it works for Cyrillic), matches against bilingual keyword dictionaries, extracts the amount with a regex, and falls back to `OTHER` when no category keyword matches. Returns a `ParseResult` (`Success`/`Failure`) rather than throwing.
- **State**: every screen's ViewModel exposes a single `StateFlow<UiState>` (loading/empty/error are explicit states or fields, not booleans scattered around).

## Tech stack

| Concern | Library | Version |
|---|---|---|
| Language | Kotlin | 2.3.21 |
| Build | AGP (built-in Kotlin compilation) | 9.2.0 |
| UI | Jetpack Compose + Material 3 | BOM 2026.06.00 |
| DI | Hilt | 2.59.2 |
| Database | Room | 2.8.4 |
| Preferences | DataStore | 1.2.1 |
| Navigation | Navigation Compose | 2.9.8 |
| Charts | Vico (compose + compose-m3) | 3.2.2 |
| Annotation processing | KSP | 2.3.9 |
| Tests | JUnit4, MockK, Turbine, kotlinx-coroutines-test | — |
| Coverage | JaCoCo | 0.8.15 |

All versions are pinned in `gradle/libs.versions.toml` (Gradle Version Catalog).

## Project structure

```
com.voicebudget
├── data
│   ├── csv          # TransactionCsv: plain Kotlin CSV (de)serializer, no Android deps
│   ├── database      # Room: TransactionEntity, TransactionDao, AppDatabase
│   └── repository    # Repository implementations + Entity<->Domain mappers
├── domain
│   ├── model          # Transaction, Category, AppSettings, ...
│   ├── parser          # TransactionParser + bilingual keyword dictionaries
│   ├── repository      # Repository interfaces
│   └── usecase          # One class per business operation
├── presentation
│   ├── dashboard, transactions, statistics, settings, voice   # Screen + ViewModel pairs
│   ├── components      # Shared composables (TransactionItem, dialogs, state views)
│   ├── navigation       # NavGraph, Routes
│   └── theme            # Material3 theme, light/dark/dynamic color
├── di                 # Hilt modules
└── utils              # formatAmount(), etc.
```

## Building

### Requirements

- JDK 17+
- Android SDK with **Platform 37** and **Build-Tools 36.0.0+** (the project targets `compileSdk = 37`; AGP 9.2 requires Build-Tools ≥ 36)
- Internet access on first build (Gradle needs to download dependencies)

### Debug APK

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release APK

The release build type has `isMinifyEnabled = true` (R8 shrinking/obfuscation). To produce a release APK you first need a signing config — either:

- open the project in Android Studio and use **Build > Generate Signed Bundle / APK**, or
- add a `signingConfigs { release { ... } }` block to `app/build.gradle.kts` pointing at your keystore, then run:

```bash
./gradlew assembleRelease
```

Without a signing config, `assembleRelease` produces an unsigned APK that Android will refuse to install.

### Running tests

```bash
./gradlew testDebugUnitTest          # all unit tests
./gradlew jacocoTestReport           # coverage report -> app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Testing approach

- **Parser** (`TransactionParserTest`, 17 cases): both languages, the exact examples from the spec, and edge cases (no amount, unknown category, decimal amounts, currency noise words, word order).
- **CSV** (`TransactionCsvTest`): round-trips, including descriptions containing commas/quotes.
- **Repository** (`TransactionRepositoryImplTest`): exercised against a fake in-memory `TransactionDao` (no Room/Robolectric/emulator available in this environment) — verifies Entity↔Domain mapping and CRUD behavior.
- **ViewModels** (Dashboard/Transactions/AddTransaction/Statistics/Settings): real use cases wired to fake repositories, driven with `kotlinx-coroutines-test` + Turbine.
- Coverage is measured over `domain` + `data` + ViewModels — Compose UI (`*Screen.kt`, `theme/`, `components/`, `navigation/`) is excluded from the JaCoCo gate, since Composables are exercised through manual/instrumented testing, not JVM unit tests. Within that scope, line coverage is ~79% (target: 70%).

## Known limitations / things to be aware of

- **No `androidTest` (instrumented) suite.** This was developed without an emulator or physical device available, so Compose UI tests and a real-Room instrumented test were not added. The unit test suite (47 tests) covers parser/repository/CSV/ViewModel logic instead.
- **AGP 9's built-in Kotlin.** Since AGP 9.0, Kotlin compilation is built into the Android Gradle Plugin and the separate `org.jetbrains.kotlin.android` plugin is no longer applied — `gradle/libs.versions.toml` and `app/build.gradle.kts` reflect this. If you see tooling that expects the old plugin (very old AGP-upgrade-assistant versions, certain linters), that's why.
- **`./gradlew` distribution download.** The Gradle wrapper is configured normally (`gradle/wrapper/gradle-wrapper.properties`, `distributionUrl=...gradle-9.5.1-bin.zip`) and will work on a normal internet connection. It was *not* exercised end-to-end while building this project, because the sandboxed dev environment could not sustain the large download; verification builds used a locally installed Gradle 9.5.1 instead.
- **Currency conversion** is display-only (changing the symbol does not convert amounts).

## Future extensions

Out of scope for this MVP, listed in the original spec: OCR receipt scanning, cloud sync, multi-device support, family budgeting, AI categorization, recurring transactions, bank integrations, subscriptions, push notifications, expense predictions, financial insights.
# VoiceBudget
