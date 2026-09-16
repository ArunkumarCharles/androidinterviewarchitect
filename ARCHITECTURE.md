# System Architecture & Design Rationale

## 1. System Overview & Clean Architecture

The project enforces strict dependency inversion:
- **`:domain`**: Contains pure Kotlin business logic (use cases, repository interfaces). It has **zero Android or framework dependencies**, ensuring maximum testability and platform independence.
- **`:data` / `:database` / `:network` / `:datastore`**: Implement domain repository interfaces and data sources.
- **`:feature:*`**: Presentation layer modules depending on `:domain` and `:core:ui`.
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
5. **`FeedRoute` / `FeedContent`** renders state reactively via `collectAsStateWithLifecycle()`.

`:feature:checkout` and `:feature:profile` follow the same `domain` repository/use-case pattern as Feed (`CheckoutRepository`/`SubmitOrderUseCase` and `UserProfileRepository`/`GetUserProfileUseCase`+`UpdateBioUseCase`+`ToggleNotificationsUseCase` respectively), rather than calling data sources directly from their ViewModels.

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
- **DataStore**: Type-safe Preferences DataStore manages theme mode, last sync timestamp, and (via `UserProfileRepositoryImpl`) the user's bio and notification preference asynchronously.
- **Navigation**: Compose Navigation uses type-safe `@Serializable` route objects (`AppRoute.Feed`/`Profile`/`Checkout` in `:app`) rather than string routes, giving compile-time-checked navigation calls.
