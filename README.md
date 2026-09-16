# Android Interview Architect

A reference-quality Android sample application and architecture guide designed for senior/staff-level Android interview preparation. Every architectural decision in this project is deliberate, documented, and explainable in technical interviews.

---

## Module Map

```
:app                     — Composition root, navigation graph, Hilt application wiring
:core:model              — Domain data classes, sealed Result/Outcome wrappers
:core:database           — Room database, entities, DAOs (Single Source of Truth)
:core:network            — Retrofit API service and OkHttp setup
:core:datastore          — Preferences DataStore for user theme, last sync timestamp, and profile settings (bio, notifications)
:core:data               — Repository implementations, mappers, WorkManager sync worker
:domain                  — Use cases and repository interfaces (pure Kotlin, zero Android dependencies)
:feature:feed            — MVI feature module (unidirectional data flow, reducer, FeedScreen)
:feature:checkout        — MVI feature module (multi-step checkout state machine)
:feature:profile         — MVVM feature module (standard CRUD-ish profile screen)
```

---

## Tech Stack & Architecture Highlights

- **Clean Architecture**: Strict layering (`presentation` -> `domain` <- `data`).
- **Both MVVM & MVI**: Demonstrated across features (`:feature:profile` uses MVVM; `:feature:feed` and `:feature:checkout` use MVI).
- **100% Jetpack Compose**: Stateful/stateless screen split, custom modifiers, and high-performance recomposition.
- **Dependency Injection**: Dagger Hilt with scoped bindings (`@Singleton`, `@ViewModelScoped`).
- **Concurrency**: Coroutines & Flow (`StateFlow` for UI state, `SharedFlow` for one-shot events, cold flows from Room).
- **Jetpack Components**: Type-safe Compose Navigation, Room reactive queries, WorkManager with constraints, DataStore preferences.
- **Testing**: JUnit 5, MockK, Turbine for Flow assertions, and Compose UI tests.

---

## How to Build & Run

1. Clone or open the project in Android Studio (Jellyfish / Koala or newer).
2. Sync project with Gradle files.
3. Build and run on an Android emulator or physical device.
