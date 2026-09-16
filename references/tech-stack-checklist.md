# Tech Stack Checklist & Rationale

Read this before scaffolding modules or writing implementation files. Each item is something the generated code should visibly demonstrate, and something the docs' Interview Cheat Sheet should be able to point to.

## Architecture

- **Clean Architecture layering**: `domain` module has zero Android/framework dependencies (pure Kotlin) — this itself is a common interview question ("why does your domain layer not depend on Android?").
- **MVVM** for screens with straightforward, mostly-linear state (e.g. profile/settings, item detail): one `UiState` data class, one `ViewModel` exposing `StateFlow<UiState>`, simple one-shot events via `SharedFlow` or a `Channel`.
- **MVI** for screens with complex, multi-source state (feed with pagination/filters/refresh, checkout with multi-step validation): explicit `Intent`/`Action` sealed class, a reducer function (pure, testable), a single `StateFlow<State>` as source of truth, and an `Effect`/`SideEffect` channel for one-shot navigation/toasts. Be explicit in comments about *why* a reducer pattern helps here (predictability, easy to unit test, avoids scattered `viewModelScope.launch` mutations).

## Compose

- Split every screen into a **stateful "screen" composable** (owns ViewModel, hoists nothing) and **stateless "content" composables** (pure functions of params, easily previewable/testable).
- At least one custom `Modifier` extension demonstrating composition over inheritance for UI behavior.
- At least one deliberate use of `derivedStateOf` with a comment explaining the recomposition problem it avoids (e.g. scroll-position-derived boolean that would otherwise recompute every frame).
- Use `@Immutable`/`@Stable` annotations where relevant and mention them in docs — a classic performance interview topic.

## Hilt

- `@HiltAndroidApp` on the Application class.
- Module-level `@Module`/`@InstallIn` for network, database, and repository bindings.
- Demonstrate at least two different scopes (`@Singleton` for DB/network, `@ViewModelScoped` for a use case that holds transient state) and explain the difference in docs.

## Coroutines & Flow

- `StateFlow` for UI state (hot, conflated, always has a value).
- `SharedFlow` for one-shot events (hot, no replay by default, buffered) — contrast with `StateFlow` explicitly in a comment or doc section.
- At least one **cold** flow example (e.g. a Room `Flow<List<T>>` query, or a Retrofit call wrapped with `flow {}`) contrasted with the hot flows above.
- Structured concurrency: `viewModelScope`, and if WorkManager or a repository-level scope is used, explain why it isn't `GlobalScope`.

## Jetpack components

- **Navigation**: type-safe routes (Kotlin serializable route objects, not string routes) — this replaced string-based nav args and is worth calling out as a modern-vs-legacy interview point.
- **Room**: at least one `@Dao` with a `Flow`-returning query (reactive read) and a suspend function (one-shot write); demonstrate a `TypeConverter` if the domain model needs one.
- **WorkManager**: one realistic periodic or constrained background task (e.g. cache sync) with `Constraints` (network required, battery not low) — a good hook for "how do you handle background work reliably" questions.
- **DataStore**: Preferences or Proto DataStore for a small piece of user prefs (e.g. theme, last-sync timestamp) — contrast with SharedPreferences in docs (async, transactional, type-safe with Proto).
- **Lifecycle/ViewModel**: `SavedStateHandle` usage for surviving process death, not just configuration change — a frequently-missed distinction worth calling out explicitly.

## Network & data layer

- Sealed `Result`/`Outcome<T>` wrapper (Success/Error/Loading or similar) rather than throwing raw exceptions across layers.
- Offline-first: Room is the single source of truth; repository exposes a `Flow` from DB, and a background refresh (via WorkManager or a repository-triggered coroutine) updates the DB, which the UI observes reactively — never returning network data directly to the UI layer.
- OkHttp interceptor example (logging or auth header) to show composition of the network stack.

## Testing

- **JUnit 5** (not JUnit 4) — note the migration/config nuance (`useJUnitPlatform()` in Gradle) since this trips people up.
- **MockK** for mocking Kotlin classes/interfaces including `coEvery`/`coVerify` for suspend functions.
- **Turbine** for asserting `Flow`/`StateFlow` emissions in ViewModel/reducer tests — show `awaitItem()`/`expectNoEvents()` usage.
- **Compose UI test** using `createComposeRule()` with at least one semantics-based assertion (`onNodeWithText`, `performClick`).

## Modularization

Minimum suggested module set (adjust names to the chosen sample domain):

```
:app                     — composition root, NavHost, DI wiring
:core:ui                 — design system, shared composables, theme
:core:common             — Result wrapper, dispatchers, utility code
:core:network            — Retrofit/Ktor + OkHttp setup
:core:database            — Room database + shared DAOs (or split per-feature if justified)
:core:datastore          — DataStore setup
:domain                  — use cases, domain models, repository interfaces (pure Kotlin)
:data                    — repository implementations, mappers (implements :domain interfaces)
:feature:feed            — MVI example
:feature:checkout        — MVI example
:feature:profile         — MVVM example
```

Explain in docs: feature modules depend on `:domain` (never on each other directly), `:data` depends on `:domain` + `:core:*`, `:app` depends on everything for DI wiring and nav graph assembly. This dependency direction is itself a common system-design interview question (why prevent feature-to-feature dependencies?).