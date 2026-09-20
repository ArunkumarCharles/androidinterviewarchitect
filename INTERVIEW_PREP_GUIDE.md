# Lead Android Interview Prep Guide

How to use this repo to prepare for senior / lead / staff Android interviews. Read it alongside
[`ARCHITECTURE.md`](ARCHITECTURE.md) (the design) and [`INTERVIEW_CHEAT_SHEET.md`](INTERVIEW_CHEAT_SHEET.md)
(question -> file -> answer table).

> **Rule for yourself:** only claim in an interview what you can open, point to and explain. Section 8 lists
> what this project does *not* do yet. Say "I'd do X here, and here's why" for those, not "I built X".

---

## 1. What a lead interview actually tests

At lead level the code is the easy part. Interviewers are checking four things:

| They probe | They want to hear |
|---|---|
| **Judgment** | The trade-off, not just the choice: "MVI here because..., MVVM there because..." |
| **Scale** | What breaks at 50 engineers / 30 modules / 10M users, and what you'd change |
| **Failure modes** | Offline, process death, slow network, bad data, partial failure |
| **Leadership** | How you set standards, review, mentor, and decide when to *not* build something |

For every topic below, practise this shape: **what it is -> why we chose it -> what it costs -> when I'd choose
differently.** The last two parts are what separate a lead from a senior.

---

## 2. Your 2-minute project pitch

Rehearse this until it is smooth:

> "It's a multi-module Android app I use as a reference architecture: Kotlin, Compose, Hilt, Room, Paging 3,
> WorkManager and DataStore. Layering is strict: features depend only on `:domain` and `:core:model`; `:domain`
> defines repository interfaces; `:core:data` implements them. Room is the single source of truth, so the app is
> offline-first: the UI reads only from the database and a `RemoteMediator` refreshes it from the network.
> I deliberately use both MVVM and MVI, MVI for feed and checkout where state transitions matter, MVVM for the
> simple profile screen, to show I choose per screen rather than by habit. Build config is centralised in
> convention plugins, and the JVM tests run in seconds because the domain layer has no Android dependencies."

Follow-ups to expect: *Why both patterns? What would you drop at a small startup? What's the weakest part?*
(Section 7 has honest answers to the last one.)

---

## 3. Guided code tour (open these in order)

Spend one session per row: read the file, then explain it out loud without looking.

| # | Open | Be able to explain |
|---|---|---|
| 1 | `settings.gradle.kts`, `build-logic/convention/` | Module list, included build, why convention plugins beat `subprojects {}` |
| 2 | `core/model/.../Post.kt`, `Result.kt` | Pure domain models; the sealed result type and its weakness (see section 7) |
| 3 | `domain/repository/*`, `domain/usecase/*` | Dependency inversion; why use cases are one-action classes; testable on the plain JVM |
| 4 | `core/database/.../AppDatabase.kt`, `PostDao.kt`, `Migrations.kt`, `schemas/` | Entities, `@Upsert` vs `REPLACE` (cascade), exported schema, `MIGRATION_1_2` |
| 5 | `core/data/.../PostRepositoryImpl.kt` | Cache-first emit, background sync, preserving local `isLiked`, dispatcher injection |
| 6 | `PostRemoteMediator.kt` + `PostRepositoryImpl.getPagedFeed` | Paging 3 with Room; what `REFRESH` does; `LoadState.Error` keeps cached pages |
| 7 | `feature/feed/FeedViewModel.kt`, `FeedScreen.kt` | MVI: intent -> reducer -> state; one-shot events via `Channel`; `cachedIn` |
| 8 | `feature/checkout/CheckoutViewModel.kt` | State machine, `SavedStateHandle` and process death (and the card-number smell) |
| 9 | `feature/topic/TopicViewModel.kt` | `combine`, debounced search, `flatMapLatest`, expanded ids kept in the ViewModel |
| 10 | `core/data/.../CacheSyncWorker.kt`, `ArchitectApplication.kt` | `@HiltWorker`, constraints, unique periodic work, retry policy |
| 11 | `core/datastore/.../UserPreferencesDataSource.kt`, `MainViewModel.kt` | DataStore vs SharedPreferences; theme without a first-frame flash |
| 12 | `app/.../MainActivity.kt`, `navigation/AppRoutes.kt` | Type-safe `@Serializable` routes, tab back-stack save/restore |
| 13 | Tests: `PostRepositoryImplTest`, `PostRemoteMediatorTest`, `AppDatabaseMigrationTest`, `TopicViewModelTest` | Turbine, MockK vs fakes, virtual time, Robolectric for Room/Work |

**Trace drill (do this weekly):** tap "Like" on a post and narrate every hop until the heart changes colour:
`FeedContent` -> `FeedIntent.ToggleLike` -> `FeedViewModel` -> `ToggleLikeUseCase` -> `PostRepository.toggleLike`
-> `PostDao` write -> Room invalidates -> Paging source refreshes -> UI recomposes. Then repeat it with the network
off, and again with the process killed mid-way.

---

## 4. Topic playbooks

Each block: the core answer, then the follow-up a lead gets asked.

### Architecture and modularisation
- **Core answer:** dependency inversion. `presentation -> domain <- data`. Features never see Retrofit or Room.
- **Why multi-module:** enforced boundaries, parallel builds, incremental compilation, clear ownership.
- **Cost you should volunteer:** more build files, slower first sync, module-boundary bikeshedding. Modularise
  along *change* boundaries, not folders.
- **Follow-up:** *"How do you stop features depending on each other?"* -> convention plugin grants only
  `:domain` + `:core:model`; verify with a dependency-graph check in CI.
- **Follow-up:** *"Is `:domain` really Android-free?"* -> Honest answer: it is an Android library so use cases can
  be `@ViewModelScoped`. Converting to a pure Kotlin/JVM module makes it compiler-enforced. Roadmap phase 3.

### MVVM vs MVI
- **MVVM:** ViewModel exposes state; events are method calls. Low ceremony. Good for CRUD/settings.
- **MVI:** single immutable state + explicit intents + reducer. Predictable, replayable, easy to test. Good when
  many sources mutate one screen (feed: load, refresh, like) or the flow is a state machine (checkout).
- **Cost of MVI:** boilerplate, and one giant state class can cause over-recomposition.
- **Follow-up:** *"Which would you pick for a new team of juniors?"* -> MVVM + a unidirectional convention;
  add MVI ceremony only where bugs come from inconsistent state.

### Compose
- Stateful/stateless split, state hoisting, stable keys in `LazyColumn`, `derivedStateOf` (scroll-to-top FAB),
  `collectAsStateWithLifecycle`.
- **Follow-up:** *"A list janks, what do you do?"* -> measure first (compiler metrics, layout inspector
  recomposition counts), check stability of params (`List` is unstable), keys, `remember` of expensive work,
  avoid reading state too high, then Baseline Profile for startup/scroll.
- **Follow-up:** *`remember` vs `rememberSaveable` vs ViewModel?* -> recomposition / config+process death /
  config change only (plus `SavedStateHandle` for process death).

### Coroutines and Flow
- `StateFlow` for state, `Channel` + `receiveAsFlow()` for one-shot events, `SharedFlow` when there are many
  collectors.
- `stateIn(WhileSubscribed(5_000))`: survives rotation, stops upstream when the UI is gone.
- `flatMapLatest` + `debounce` for search; `combine` for multiple sources.
- **Follow-up:** *cancellation* -> structured concurrency; never swallow `CancellationException` in a broad
  `catch` (see section 7, this codebase has one).
- **Follow-up:** *dispatchers in tests* -> inject them (`DispatchersModule`); or `Dispatchers.setMain`.

### Room, offline-first, Paging
- **Offline-first rule:** UI reads *only* the database; network writes *into* the database.
- **Conflict story:** a sync must not clobber local state (the repo preserves `isLiked`). Be ready to generalise:
  last-write-wins vs field-level merge vs server-authoritative, and outbox pattern for offline writes.
- **Migrations:** export schema, explicit `Migration`, test with a real old DB; destructive fallback only for
  downgrade.
- **Paging:** `PagingSource` from Room + `RemoteMediator`; cached pages stay visible on error;
  this sample's API returns everything, so only `REFRESH` does work (a paged API needs remote keys).

### Dependency injection
- `@Singleton` for DB/Retrofit/DataStore; `@ViewModelScoped` for use cases; `@HiltWorker` for WorkManager.
- **Follow-up:** *Hilt vs Koin vs manual* -> compile-time validation and scoping vs simplicity; Hilt build-time
  cost; manual DI is fine for small apps.

### Testing
- Pyramid: many JVM tests (ViewModels, use cases, repositories), Robolectric for Room/WorkManager, thin Compose UI
  layer.
- **MockK vs fakes:** mocks verify interactions (brittle); fakes verify behaviour (better for stateful
  repositories). A lead should say when they'd switch.
- Turbine for Flow assertions; virtual time (`runTest`) for delays.

### Build, CI and performance
- Convention plugins, version catalog, build cache, configuration cache, parallel execution.
- CI here runs detekt, lint, unit tests, debug + release (R8) builds.
- **Performance vocabulary to be fluent in:** cold/warm start, Baseline Profiles, Macrobenchmark, R8, Compose
  stability, overdraw, main-thread work, StrictMode, Perfetto.

---

## 5. System-design scenarios (using these building blocks)

Practise each for 30 minutes on a whiteboard. Structure: requirements -> API/data model -> client architecture
-> offline/failure -> scale/perf -> testing/observability -> what you'd cut for v1.

1. **Offline-first news feed** (this app): Room SSOT, `RemoteMediator`, like as optimistic write + outbox sync.
2. **Chat app:** local DB as truth, WebSocket/FCM for delivery, message states (sending/sent/failed), ordering
   and dedup ids, unread counts, pagination both directions, attachments via WorkManager upload.
3. **Image-heavy grid:** Coil, memory/disk cache sizing, `contentScale`, placeholders, prefetch, bitmap
   downsampling, Paging.
4. **Checkout/payments:** idempotency keys, never store card data, process-death-safe state machine, retry
   without double charge, PCI scope minimisation (tokenise via provider SDK).
5. **Super-app modularisation for 50+ engineers:** feature modules + api/impl split, dependency rules in CI,
   build-time budgets, ownership (CODEOWNERS), feature flags, staged rollouts.

---

## 6. Leadership and behavioural prompts

Prepare a real story (situation, your action, measurable result) for each:

- A technical decision you drove that others disagreed with. How did you decide, and were you right?
- You inherited a legacy codebase: how did you migrate incrementally (strangler pattern, module by module)?
- Improving quality: how did you raise test coverage / cut crashes / reduce build time, with numbers?
- Mentoring: a junior who kept repeating an issue. What did you change (docs, lint rule, pairing)?
- Production incident: root cause, mitigation, and the systemic fix (not just the hotfix).
- Saying no: a feature or library you refused to add, and why.
- Estimation and trade-offs: how you cut scope to hit a date without hiding risk.

Lead-level signals: you set **guardrails** (lint/detekt rules, convention plugins, CI gates, ADRs) instead of
relying on reviews; you measure (crash-free rate, startup time, build time); you write down decisions.

---

## 7. Honest weak spots in this project (and how to talk about them)

Interviewers respect self-critique. Know these cold, and the fix you'd make:

| Weak spot | Where | Fix you would make |
|---|---|---|
| DTOs and domain models are the same class (`@Serializable` in `:core:model`) | `core/model`, `ApiService` | Separate `*Dto` in `:core:network` with mappers, so API changes don't leak into domain |
| `Result.Error` carries a `Throwable`/message, UI shows raw text | `Result.kt`, `FeedViewModel` | Typed `AppError` (network/http/unauthorized), map to string resources |
| `catch (e: Exception)` may swallow `CancellationException` | `FeedViewModel.refresh` | Rethrow cancellation |
| Theme stored as a `String` | `UserPreferencesDataSource`, `MainActivity` | `ThemeMode` enum |
| Card number kept in `SavedStateHandle` | `CheckoutViewModel` | Never persist PAN; keep in memory, tokenise |
| Retrofit has no OkHttp client config | `NetworkModule` | Timeouts, auth interceptor + `Authenticator`, cache, pinning, per-flavor base URL |
| `:domain` is an Android library with `@ViewModelScoped` | `domain/build.gradle.kts` | Pure Kotlin module, scope in DI modules |
| UI strings hardcoded, "(MVI)/(MVVM)" in product labels | screens | `strings.xml`, localisation |
| Feed exposes both `uiState.posts` and `pagedPosts` | `FeedViewModel` | One source for the list |
| No baseline profile, no analytics/crash abstraction, no edge-to-edge/adaptive UI | app | See roadmap below |

---

## 8. Implemented vs roadmap (do not over-claim)

**Implemented and demoable:** multi-module Clean Architecture, MVVM + MVI, Room SSOT with migration and
exported schema, Paging 3 + `RemoteMediator`, WorkManager with Hilt, DataStore, type-safe navigation,
injected dispatchers, convention plugins + version catalog, detekt + lint + R8 in CI, JVM/Robolectric/Compose
tests.

**Planned, not built yet** (tracked as phases in the working plan): dependency upgrade, pure-Kotlin `:domain`,
typed errors, `:core:designsystem`, real network/security stack and build flavors, observability
(logger/analytics/crash/feature flags), Baseline Profile + Macrobenchmark, Hilt test setup + screenshot tests +
coverage gate, split CI, edge-to-edge/adaptive layouts and deep links, ADRs.

When asked about any planned item, say: *"It's not in this project yet; here is how I'd design it and the
trade-offs."* That is a strong answer. A false claim is not.

---

## 9. Two-week study plan

| Days | Focus | Output |
|---|---|---|
| 1-2 | Section 3 tour rows 1-6, trace drill | Explain data flow aloud for 5 min, no notes |
| 3-4 | Rows 7-12, MVVM vs MVI, Compose state | Whiteboard both patterns for one screen |
| 5-6 | Coroutines/Flow, Room/Paging playbooks | Answer 10 cheat-sheet questions aloud |
| 7 | Rest / review weak spots (section 7) | One-page "what I'd fix" |
| 8-9 | System-design scenarios 1-3 | Two timed whiteboard sessions |
| 10-11 | Scenarios 4-5, build/CI/perf vocabulary | Draw a module graph for 50 engineers |
| 12 | Behavioural stories (section 6) | 6 STAR stories with numbers |
| 13 | Mock interview (ask a friend, or ask Claude) | Recorded, reviewed |
| 14 | Light review, sleep | Pitch (section 2) from memory |

---

## 10. Self-test drills

1. Explain each module in one sentence, in dependency order, without notes.
2. Kill the network, kill the process: what does the user see in each screen, and why?
3. Change a Room column: list every file you touch and what test proves it is safe.
4. Add a new feature module: list exactly what the convention plugin gives you and what you still write.
5. A reviewer says "just use `Dispatchers.IO` directly". Give the two-sentence rebuttal.
6. A PM asks for a 3x faster startup. What do you measure first, and what are the three biggest levers?
7. Take any file above and say what you would change if the team grew tenfold.
