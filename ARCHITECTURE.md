# System Architecture & Design Rationale

## 1. System Overview & Clean Architecture

The project enforces strict dependency inversion:
- **`:domain`**: Contains business logic (use cases, repository interfaces). It uses **no Android APIs**, so its tests run on the plain JVM. It is still an Android library module only so use cases can be `@ViewModelScoped`; converting it to `kotlin("jvm")` would make the isolation compiler-enforced at the cost of that annotation.
- **`:data` / `:database` / `:network` / `:datastore`**: Implement domain repository interfaces and data sources.
- **`:feature:*`**: Presentation layer modules depending on `:domain` and `:core:model` (shared UI components currently live in each feature module; a `:core:ui` module is a natural next step).
- **`:app`**: Composition root responsible for dependency injection wiring and navigation graph assembly.

```
[ :app ] ──► [ :feature:* ] ──► [ :domain ] ◄── [ :core:data ] ◄── [ :core:database / :core:network ]
```

---

## 2. Data Flow Walkthrough (Offline-First Policy)

Taking the **Feed Feature** as an example:
1. **`FeedViewModel`** dispatches `FeedIntent.LoadFeed`.
2. **`GetFeedUseCase`** invokes `PostRepository.getFeedStream()`.
3. **`PostRepositoryImpl`** (Offline-First Single Source of Truth):
   - Emits `Result.Loading`.
   - Immediately queries and emits a Room (`PostDao.getPostsSnapshot()`) snapshot so the UI renders instantly without waiting for network.
   - Fetches fresh data from remote `ApiService` in the background via `syncRemotePosts()`, merging it into Room while preserving any locally-toggled `isLiked` state (a plain `REPLACE` upsert would otherwise silently clobber it), and records `lastSyncTime` via `:core:datastore`.
   - A background sync failure (e.g. offline) is logged and tolerated here — the UI keeps showing cached data instead of surfacing an error.
   - The repository then stays subscribed to Room's reactive `Flow`, so any further local write (a like toggle, or a later successful `CacheSyncWorker` run) keeps pushing fresh state to the UI.
4. **`FeedViewModel`** reduces emitted results into `FeedUiState` (`Loading`, `Success`, `Error`).
5. **`FeedRoute` / `FeedContent`** renders state reactively via `collectAsStateWithLifecycle()`. The list itself comes from `FeedViewModel.pagedPosts` (`GetPagedFeedUseCase`, see *Paging* below) while `FeedUiState` still drives loading, the empty state and refresh errors. `FeedContent` falls back to `FeedUiState.Success.posts` when no paged items are passed, which previews and UI tests use.

`:feature:checkout` and `:feature:profile` follow the same `domain` repository/use-case pattern as Feed (`CheckoutRepository`/`SubmitOrderUseCase` and `UserProfileRepository`/`GetUserProfileUseCase`+`UpdateBioUseCase`+`ToggleNotificationsUseCase`+`SetThemeModeUseCase` respectively), rather than calling data sources directly from their ViewModels.

---

## 3. MVVM vs MVI Decision Rationale

| Screen / Feature | Architecture Pattern | Rationale |
|---|---|---|
| `ProfileScreen` (`:feature:profile`) | **MVVM** | Simple CRUD/settings screen with mostly linear state and straightforward user events. Avoids boilerplate reducer overhead. |
| `FeedScreen` (`:feature:feed`) | **MVI** | Multiple event sources (initial load, manual refresh, toggling likes) reconciled through Room as the single source of truth. Unidirectional data flow and pure reducers ensure predictable state transitions. |
| `CheckoutScreen` (`:feature:checkout`) | **MVI** | Multi-step transaction workflow requiring strict state validation and step-to-step state machines to prevent invalid user transitions. |

---

## 4. Cross-Cutting Concerns

- **Dependency Injection**: `@Singleton` scope for database, Retrofit API, and DataStore; `@ViewModelScoped` for use cases holding per-screen transient state.
- **Background Sync**: `CacheSyncWorker` (WorkManager, `@HiltWorker`) runs periodic background syncs constrained by network connectivity and unconstrained battery levels (`NetworkType.CONNECTED`, `requiresBatteryNotLow = true`). `ArchitectApplication` implements `Configuration.Provider` with an injected `HiltWorkerFactory` so Hilt can construct the worker, and enqueues the periodic request (`ExistingPeriodicWorkPolicy.KEEP`) on app startup.
- **DataStore**: Type-safe Preferences DataStore manages theme mode, last sync timestamp, and (via `UserProfileRepositoryImpl`) the user's bio and notification preference asynchronously. `MainViewModel` in `:app` reads the theme through `GetUserProfileUseCase` and starts as `"system"` until DataStore emits, so the first frame follows the OS setting instead of flashing.
- **Navigation**: Compose Navigation uses type-safe `@Serializable` route objects (`AppRoute.Feed`/`Profile`/`Checkout`/`Topic` in `:app`) rather than string routes, giving compile-time-checked navigation calls.
- **Topics & Q&A content**: `:feature:topic` renders `topics` and `questions` (FK `questions.topicId` -> `topics.id`, `CASCADE`) straight from Room. There is no backend, so `DatabaseModule`'s `RoomDatabase.Callback.onCreate` seeds posts, topics and questions from `SeedData.kt`. `TopicDao` uses `@Upsert` rather than `REPLACE` because REPLACE deletes the parent row first and would cascade-delete its questions. Schema changes ship as explicit `Migration` objects (see *Migrations* below), never a destructive fallback on upgrade.

### Paging, migrations and dispatchers
- **Paging**: `PostRepositoryImpl.getPagedFeed()` builds a `Pager` over `PostDao.getPostsPaged()` (Room-generated `PagingSource`). `PostRemoteMediator` refreshes Room from the network; the UI never reads the network directly. The API returns the whole list, so only `REFRESH` does work; a paged backend would add per-row next-page keys.
- **Migrations**: `AppDatabase` exports its schema (`core/database/schemas`) and ships `MIGRATION_1_2` instead of destructive fallback, so likes survive upgrades. Destructive fallback is kept for downgrades only.
- **Dispatchers**: `DispatchersModule` provides `@IoDispatcher` / `@DefaultDispatcher`; repositories use `flowOn` / `withContext` with the injected dispatcher.
- **One-shot events**: `TopicViewModel` and `FeedViewModel` expose a `Channel`-backed `events` flow for snackbars.

### Testing
- **JVM unit tests** (JUnit 4, MockK, Turbine, `kotlinx-coroutines-test`): ViewModels (`FeedViewModelTest`, `ProfileViewModelTest`, `CheckoutViewModelTest`, `TopicViewModelTest`, `MainViewModelTest`), every use case (`UseCaseTest`), repositories (`PostRepositoryImplTest`, `TopicRepositoryImplTest`, `QuestionRepositoryImplTest`, `UserProfileRepositoryImplTest`, `CheckoutRepositoryImplTest`), `PostRemoteMediatorTest`, and the entity/domain mappers (`MappersTest`).
- **Robolectric** (in-memory Room, WorkManager): `DaoTest`, `AppDatabaseMigrationTest`, `CacheSyncWorkerTest`. These pin the test JVM to JDK 21 because Robolectric's bundled ASM cannot read JDK 25 class files.
- **Compose UI tests** (`androidTest`): `FeedScreenTest`, `TopicScreenTest`.
- **Not covered**: `UserPreferencesDataSource` (thin DataStore wrapper, only exercised through `UserProfileRepositoryImplTest` with a mock), the Profile and Checkout screens' Compose UI, and `SeedData` contents.

### Build logic (convention plugins)
- `build-logic/convention` is an *included build* (`pluginManagement { includeBuild("build-logic") }`). It defines `architect.android.application`, `.library`, `.hilt`, `.compose` and `.feature`, so a module's build file is only its namespace and its real dependencies.
- **Single source of truth**: compileSdk/minSdk/targetSdk live in `gradle/libs.versions.toml`; the JVM target and Hilt+KSP wiring live in the plugins. Before this, the same ~25 lines were copy-pasted into 11 files and could drift.
- `architect.android.feature` also encodes the dependency rule: a feature gets `:domain` and `:core:model` and nothing else, so it cannot pick up `:core:data` or another feature by copy-paste.
- Catalog **bundles** (`androidx-compose`, `androidx-lifecycle-compose`, `unit-test`) keep the usual dependency sets identical everywhere.
- The plugins use `compileOnly` for AGP/Kotlin/KSP/Hilt: the root build already loads them (`apply false`), and bundling a second copy would risk two versions of the same plugin.

