package com.sevvanam.android_interview_architect.core.database.di

// Bundled content inserted on first DB creation (see DatabaseModule). NetworkModule points at a
// placeholder URL, so without this the Feed and Topics screens would be permanently empty.
// Ids are stable strings so a future network sync can upsert over them without duplicating rows.

internal data class SeedPost(val id: String, val title: String, val content: String, val author: String, val timestamp: Long)

internal data class SeedTopic(val id: String, val name: String, val description: String)

internal data class SeedQuestion(val id: String, val topicId: String, val question: String, val answer: String)

private const val SEED_AUTHOR = "Staff Architect"
private const val SEED_EPOCH = 1_700_000_000_000L

private fun post(n: Int, title: String, content: String) =
    SeedPost("seed-$n", title, content, SEED_AUTHOR, SEED_EPOCH + (n - 1) * 100_000L)

internal val seedPosts = listOf(
    post(1, "Why StateFlow over LiveData?",
        "StateFlow is hot, conflated, and Kotlin-native — no lifecycle-transformation boilerplate, and it composes naturally with Flow operators."),
    post(2, "Offline-first with Room as SSOT",
        "The repository always renders from Room first, then syncs the network in the background — the UI never blocks on connectivity."),
    post(3, "MVI vs MVVM: when to reach for which",
        "MVVM suits linear CRUD screens; MVI's reducer earns its keep once a screen has multiple concurrent event sources to reconcile."),
    post(4, "Hilt scoping: Singleton vs ViewModelScoped",
        "Expensive shared singletons (DB, Retrofit) get @Singleton; per-screen use cases get @ViewModelScoped so they die with the screen."),
    post(5, "Type-safe navigation with @Serializable routes",
        "Kotlin Serializable route objects replace string routes — the compiler catches a typo'd destination instead of a runtime crash."),
    post(6, "Surviving process death with SavedStateHandle",
        "A ViewModel survives rotation but not the OS killing your process in the background. SavedStateHandle persists small, serializable UI state (ids, filters, scroll keys) so the user returns to where they were. Keep big data in Room, not in the handle."),
    post(7, "Compose stability and skipping recomposition",
        "Compose skips a composable only when all its parameters are stable and unchanged. Unstable types (plain List, classes with var fields) force recomposition. Prefer immutable data classes, kotlinx ImmutableList, or @Immutable, and verify with the compiler metrics report."),
    post(8, "remember vs rememberSaveable",
        "remember survives recomposition only; rememberSaveable also survives configuration change and process death by writing to the saved-instance-state Bundle. Use it for user input like text fields and expanded state, never for large objects."),
    post(9, "Choosing the right coroutine dispatcher",
        "Dispatchers.Main for UI, IO for blocking network/disk, Default for CPU-heavy work. Inject dispatchers instead of hardcoding them so tests can swap in a TestDispatcher and stay deterministic."),
    post(10, "Structured concurrency and cancellation",
        "Child coroutines are bound to their parent scope, so cancelling a ViewModel's viewModelScope cancels everything it launched. Never swallow CancellationException in a broad catch — rethrow it, or the coroutine will not stop."),
    post(11, "flatMapLatest for search-as-you-type",
        "flatMapLatest cancels the previous inner flow when a new query arrives, so stale network results can never overwrite fresh ones. Pair it with debounce and distinctUntilChanged to cut wasted requests."),
    post(12, "Room migrations: never ship destructive by default",
        "fallbackToDestructiveMigration wipes user data on a version bump. For production, write explicit Migration objects, export schemas, and test them with MigrationTestHelper. Reserve destructive fallback for caches you can rebuild."),
    post(13, "Paging 3 with Room and RemoteMediator",
        "Room is the single source of truth and RemoteMediator fetches the next page from the network into it, so the list keeps working offline. PagingSource loads only what is visible, keeping memory flat for very long lists."),
    post(14, "Baseline Profiles and startup performance",
        "A Baseline Profile ships pre-compiled hot code paths with your APK, cutting cold-start time and first-frame jank by up to ~30%. Generate it with Macrobenchmark and measure before and after — never guess at performance."),
    post(15, "The testing pyramid for Android",
        "Many fast JVM unit tests (ViewModels, use cases, reducers), fewer integration tests (Room in-memory, repository), and a thin layer of UI tests. Clean Architecture makes the bottom layer cheap because the domain has no Android dependencies.")
)

internal val seedTopics = listOf(
    SeedTopic("architecture", "Architecture", "Clean Architecture, MVVM vs MVI, layering and dependency rules."),
    SeedTopic("compose", "Jetpack Compose", "State, recomposition, stability, side effects and performance."),
    SeedTopic("coroutines", "Coroutines & Flow", "Structured concurrency, dispatchers, StateFlow, SharedFlow and operators."),
    SeedTopic("room", "Room & Offline-first", "Single source of truth, sync strategies, migrations and caching."),
    SeedTopic("hilt", "Hilt & DI", "Scopes, modules, assisted injection and testing with DI."),
    SeedTopic("testing", "Testing", "Unit, integration and UI testing strategies with fakes, MockK and Turbine."),
    SeedTopic("performance", "Performance", "Startup, jank, memory leaks, Baseline Profiles and profiling."),
    SeedTopic("navigation", "Navigation", "Type-safe routes, back stack, deep links and multi-module navigation."),
    SeedTopic("workmanager", "WorkManager", "Reliable background work, constraints, chaining and Hilt workers."),
    SeedTopic("modularization", "Modularization", "Module boundaries, api vs implementation, build speed and ownership.")
)

private fun q(topicId: String, n: Int, question: String, answer: String) =
    SeedQuestion("$topicId-$n", topicId, question, answer)

internal val seedQuestions = listOf(
    q("architecture", 1, "Why keep the domain layer free of Android dependencies?",
        "Pure Kotlin business logic runs in fast JVM unit tests, cannot accidentally depend on framework lifecycles, and stays reusable if the UI toolkit or platform changes."),
    q("architecture", 2, "MVVM or MVI — how do you choose?",
        "MVVM is simpler and fits CRUD-style screens. MVI adds a single immutable state plus a reducer, which pays off when several event sources (user, network, timers) must be reconciled predictably and replayed in tests."),
    q("architecture", 3, "Where should mapping between data, domain and UI models happen?",
        "At each layer boundary: data mappers convert entities/DTOs to domain models; the presentation layer maps domain to UI state. This stops schema or API changes from leaking into upper layers."),
    q("compose", 1, "What triggers recomposition and how do you reduce it?",
        "Reading a changed State triggers recomposition of the readers. Reduce it by hoisting state, passing stable/immutable parameters, using keys in lazy lists, deferring reads with lambdas, and derivedStateOf for computed values."),
    q("compose", 2, "Explain LaunchedEffect, DisposableEffect and rememberCoroutineScope.",
        "LaunchedEffect runs a suspend block tied to the composition and restarts when its key changes. DisposableEffect is for setup/cleanup of non-suspending resources. rememberCoroutineScope gives a scope to launch from event callbacks."),
    q("compose", 3, "What is state hoisting?",
        "Moving state out of a composable into its caller, exposing the value and an onChange callback. The composable becomes stateless, reusable and easily previewable and testable."),
    q("coroutines", 1, "StateFlow vs SharedFlow — when do you use each?",
        "StateFlow always has a current value and conflates, ideal for UI state. SharedFlow has configurable replay and no initial value, suited to one-shot events, though a Channel is often safer for events that must not be missed."),
    q("coroutines", 2, "Why use collectAsStateWithLifecycle instead of collectAsState?",
        "It stops collecting when the lifecycle drops below STARTED, so upstream flows (DB, location) are not kept active in the background, saving battery and work."),
    q("coroutines", 3, "What happens if a child coroutine throws?",
        "In a regular Job the exception cancels the parent and siblings. With a SupervisorJob (viewModelScope uses one) siblings survive, but you must still handle the exception in the failing child or a CoroutineExceptionHandler."),
    q("room", 1, "How does offline-first caching work end to end?",
        "The UI observes a Flow from Room, the single source of truth. The repository fetches from the network in the background and upserts into Room, which re-emits automatically, so the UI never waits on connectivity."),
    q("room", 2, "How do you preserve local-only fields (e.g. isLiked) during a sync?",
        "Merge instead of blind replace: read existing rows, keep the local fields, and overwrite only server-owned columns, or store local state in a separate table joined at query time."),
    q("room", 3, "How do you handle a Room schema change safely?",
        "Bump the version, write a Migration with explicit SQL, export schemas to source control, and verify with MigrationTestHelper. Only use destructive fallback for rebuildable caches."),
    q("hilt", 1, "When do you use @Singleton vs @ViewModelScoped?",
        "@Singleton for app-wide expensive objects (database, Retrofit, OkHttp). @ViewModelScoped for objects that should live and die with one ViewModel, such as per-screen use cases holding state."),
    q("hilt", 2, "@Binds or @Provides?",
        "@Binds maps an interface to an implementation with no generated body, so it is cheaper and preferred. @Provides is for third-party or builder-created objects you cannot constructor-inject."),
    q("hilt", 3, "How do you swap dependencies in tests?",
        "Use @TestInstallIn to replace a production module with a fake one, or @UninstallModules with @BindValue. Prefer constructor-injected fakes in plain unit tests and skip Hilt entirely."),
    q("testing", 1, "How do you test a ViewModel that exposes StateFlow?",
        "Inject a TestDispatcher via Dispatchers.setMain, fake or mock the repository, and use Turbine's test { awaitItem() } to assert each emission deterministically."),
    q("testing", 2, "Fakes or mocks?",
        "Fakes give realistic behaviour and survive refactors; mocks verify interactions and are quick for simple collaborators. Prefer fakes for repositories and data sources, mocks for pure interaction checks."),
    q("testing", 3, "What belongs in each layer of the testing pyramid?",
        "Base: many JVM unit tests for logic. Middle: integration tests such as in-memory Room and repositories. Top: a few UI/end-to-end tests for critical journeys, since they are slow and flaky."),
    q("performance", 1, "How do you diagnose slow startup?",
        "Measure first with Macrobenchmark and Perfetto/System Trace, find work on the main thread in Application.onCreate and first-frame path, defer or lazy-init it, and add a Baseline Profile."),
    q("performance", 2, "What causes memory leaks on Android and how do you find them?",
        "Long-lived references to Activities/Contexts (static fields, listeners, coroutines outliving lifecycle). Find them with LeakCanary and the Android Studio heap profiler, and scope work to lifecycles."),
    q("performance", 3, "How do you keep long lists smooth?",
        "Use lazy lists with stable keys and contentType, keep item composables cheap, avoid work in composition, page data with Paging 3, and size images properly via Coil."),
    q("navigation", 1, "What are the benefits of type-safe navigation?",
        "Routes are @Serializable Kotlin types, so arguments are compile-time checked and refactor-safe, replacing error-prone string concatenation and manual argument parsing."),
    q("navigation", 2, "How do you navigate across feature modules without coupling them?",
        "Features expose only a route/destination contract; the app module owns the NavHost and wires them together. Features never depend on each other directly."),
    q("navigation", 3, "How do you preserve state when switching bottom-nav tabs?",
        "Navigate with saveState = true and restoreState = true with launchSingleTop and popUpTo the start destination, so each tab's back stack and ViewModel state are restored."),
    q("workmanager", 1, "When do you choose WorkManager over a coroutine or a foreground service?",
        "WorkManager is for deferrable work that must run even if the app is closed or the device restarts (sync, uploads). Coroutines are for in-process work; foreground services are for user-visible ongoing tasks."),
    q("workmanager", 2, "How do you inject dependencies into a Worker?",
        "Annotate it @HiltWorker with @AssistedInject, and have the Application implement Configuration.Provider returning a HiltWorkerFactory."),
    q("workmanager", 3, "How do you make periodic sync battery-friendly?",
        "Set constraints (NetworkType.CONNECTED, requiresBatteryNotLow), use PeriodicWorkRequest with a sensible interval and exponential backoff, and enqueue as unique work to avoid duplicates."),
    q("modularization", 1, "Why modularize an Android app?",
        "Enforced boundaries, parallel and incremental builds, clear team ownership, and the ability to reuse or replace modules. The cost is more configuration, so split by real seams, not prematurely."),
    q("modularization", 2, "api vs implementation in Gradle?",
        "implementation hides a dependency from consumers so changes do not trigger their recompilation; api exposes it transitively. Default to implementation and use api only when the type appears in your public surface."),
    q("modularization", 3, "How do you stop feature modules depending on each other?",
        "Features depend only on core and domain modules. Cross-feature communication goes through the app module's navigation or shared domain interfaces, and a lint/dependency-graph check enforces it in CI.")
)

/** Topics before questions: questions.topicId is a foreign key to topics.id. */
internal fun seedTopicsAndQuestions(db: androidx.sqlite.db.SupportSQLiteDatabase) {
    seedTopics.forEach { topic ->
        db.execSQL(
            "INSERT OR IGNORE INTO topics (id, name, description, imageUrl) VALUES (?, ?, ?, NULL)",
            arrayOf(topic.id, topic.name, topic.description)
        )
    }
    seedQuestions.forEach { q ->
        db.execSQL(
            "INSERT OR IGNORE INTO questions (id, topicId, question, answer) VALUES (?, ?, ?, ?)",
            arrayOf(q.id, q.topicId, q.question, q.answer)
        )
    }
}
