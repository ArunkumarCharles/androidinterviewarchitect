# Documentation Outline Template

A starting skeleton — adapt to what was actually built rather than filling mechanically. Real class/function names from the generated code must replace every bracketed placeholder.

## README.md

1. One-paragraph summary of the sample app and its purpose (interview prep reference, not a shipping product).
2. Module map (tree + one-line purpose per module — reuse from the project-structure step).
3. How to build/run/test (Gradle commands, JUnit 5 platform note).
4. Link to ARCHITECTURE.md.

## ARCHITECTURE.md

1. **System overview** — text or Mermaid diagram of module dependency direction and Clean Architecture layers (presentation → domain → data, with data implementing domain interfaces).
2. **Data flow walkthrough** — for one concrete feature (e.g. the feed):
    - `[FeedRemoteDataSource]` / `[FeedLocalDataSource]` responsibilities
    - `[FeedRepositoryImpl]` — how it merges/reconciles the two, offline-first policy
    - `[GetFeedUseCase]` — why a use case exists here instead of ViewModel calling repository directly
    - `[FeedViewModel]` (MVI) — intent → reducer → state, effects channel
    - `[FeedScreen]` / `[FeedContent]` — stateful/stateless split, what triggers recomposition
3. **MVVM vs MVI decision table**:

   | Screen | Pattern | Why |
      |---|---|---|
   | `[ProfileScreen]` | MVVM | simple, mostly-linear state, few events |
   | `[FeedScreen]` | MVI | multiple event sources (filter, refresh, pagination), needs predictable reducer |
   | `[CheckoutScreen]` | MVI | multi-step validation, needs strict state machine to avoid invalid transitions |

4. **Cross-cutting concerns**: DI scoping table, offline-first strategy diagram, background work (WorkManager) strategy, DataStore usage.

## Interview Cheat Sheet (standalone section or file)

Format as a table: Question → Where in the codebase → One-line answer.

| Interview Question | Code Reference | Answer |
|---|---|---|
| How is UI state handled? | `[FeedViewModel.state]` | `StateFlow<FeedState>`, hot + conflated, survives recomposition, single source of truth for the screen |
| How are config changes / process death handled? | `[CheckoutViewModel]` + `SavedStateHandle` | ViewModel survives config change automatically; `SavedStateHandle` persists critical fields across process death |
| How does offline-first work? | `[FeedRepositoryImpl]` | Room is source of truth; UI observes `Flow` from DB; WorkManager/coroutine refreshes network → DB in background |
| How is DI scoped? | `[NetworkModule]`, `[DatabaseModule]` | `@Singleton` for DB/network clients; `@ViewModelScoped` for use cases holding per-screen transient state |
| How is navigation type-safe? | `[NavGraph]` routes | Kotlin `@Serializable` route objects instead of string args — compile-time safety for nav arguments |
| How would you test the reducer? | `[FeedReducerTest]` | Pure function, no mocking needed for reducer logic itself; MockK for the ViewModel's use case dependencies, Turbine to assert `StateFlow` emissions |
| Why this module structure? | module tree | Prevents feature-to-feature coupling, enables parallel team ownership, improves incremental build times |
| Where is `derivedStateOf` used and why? | `[FeedScreen]` scroll state | Avoids recomputing a derived boolean (e.g. "show scroll-to-top button") on every scroll pixel change, only on the input actually changing |

Extend this table with any additional patterns the generated code introduces (e.g. WorkManager constraints, DataStore vs SharedPreferences, hot vs cold Flow contrast, sealed `Result` wrapper vs exceptions, OkHttp interceptor).