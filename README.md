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
:domain                  — Use cases and repository interfaces (no Android APIs; packaged as a library module so Hilt's `@ViewModelScoped` is available)
:feature:feed            — MVI feature module (unidirectional data flow, reducer, FeedScreen)
:feature:checkout        — MVI feature module (multi-step checkout state machine)
:feature:profile         — MVVM feature module (standard CRUD-ish profile screen)
:feature:topic           — MVVM feature module (interview topics with expandable Q&A, seeded offline)
```

---

## Tech Stack & Architecture Highlights

- **Clean Architecture**: Strict layering (`presentation` -> `domain` <- `data`).
- **Both MVVM & MVI**: Demonstrated across features (`:feature:profile` uses MVVM; `:feature:feed` and `:feature:checkout` use MVI).
- **100% Jetpack Compose**: Stateful/stateless screen split, custom modifiers, and high-performance recomposition.
- **Dependency Injection**: Dagger Hilt with scoped bindings (`@Singleton`, `@ViewModelScoped`).
- **Concurrency**: Coroutines & Flow (`StateFlow` for UI state, `Channel` + `receiveAsFlow()` for one-shot events (see `TopicViewModel`), cold flows from Room).
- **Jetpack Components**: Type-safe Compose Navigation, Room reactive queries, WorkManager with constraints, DataStore preferences (drives the app theme).
- **Paging 3 + Room**: `Pager` over Room's `PagingSource` with a `RemoteMediator` (Room stays the single source of truth).
- **Room migrations**: schema export + explicit `MIGRATION_1_2` (keeps user likes) with a Robolectric migration test.
- **Injected dispatchers**: `@IoDispatcher` / `@DefaultDispatcher` so repositories are deterministic under test.
- **Release hardening**: R8 minify + resource shrinking enabled for release.
- **Testing**: JUnit 4, MockK, Turbine for Flow assertions, Robolectric (in-memory Room, WorkManager), and Compose UI tests.
- **CI / quality**: GitHub Actions runs detekt, lint, unit tests and debug + release builds (`.github/workflows/ci.yml`).
  Robolectric tests pin the test JVM to JDK 21 (its bundled ASM cannot read JDK 25 class files).

---

## How to Build & Run

1. Clone or open the project in Android Studio (Jellyfish / Koala or newer).
2. Sync project with Gradle files.
3. Build and run on an Android emulator or physical device.
