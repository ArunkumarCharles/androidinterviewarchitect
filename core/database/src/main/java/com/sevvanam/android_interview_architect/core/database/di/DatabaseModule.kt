package com.sevvanam.android_interview_architect.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sevvanam.android_interview_architect.core.database.AppDatabase
import com.sevvanam.android_interview_architect.core.database.dao.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "architect_database"
        )
            // NetworkModule's ApiService points at a placeholder URL (no real backend exists for
            // this teaching sample), so the network sync in PostRepositoryImpl always fails.
            // Seed a few sample posts on first DB creation so the offline-first Feed screen has
            // real content to demonstrate instead of showing a permanently blank list.
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedPosts.forEach { post ->
                        db.execSQL(
                            "INSERT INTO posts (id, title, content, author, timestamp, isLiked) VALUES (?, ?, ?, ?, ?, 0)",
                            arrayOf(post.id, post.title, post.content, post.author, post.timestamp)
                        )
                    }
                }
            })
            .build()
    }

    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }
}

private data class SeedPost(val id: String, val title: String, val content: String, val author: String, val timestamp: Long)

private val seedPosts = listOf(
    SeedPost(
        id = "seed-1",
        title = "Why StateFlow over LiveData?",
        content = "StateFlow is hot, conflated, and Kotlin-native — no lifecycle-transformation boilerplate, and it composes naturally with Flow operators.",
        author = "Staff Architect",
        timestamp = 1_700_000_000_000L
    ),
    SeedPost(
        id = "seed-2",
        title = "Offline-first with Room as SSOT",
        content = "The repository always renders from Room first, then syncs the network in the background — the UI never blocks on connectivity.",
        author = "Staff Architect",
        timestamp = 1_700_000_100_000L
    ),
    SeedPost(
        id = "seed-3",
        title = "MVI vs MVVM: when to reach for which",
        content = "MVVM suits linear CRUD screens; MVI's reducer earns its keep once a screen has multiple concurrent event sources to reconcile.",
        author = "Staff Architect",
        timestamp = 1_700_000_200_000L
    ),
    SeedPost(
        id = "seed-4",
        title = "Hilt scoping: Singleton vs ViewModelScoped",
        content = "Expensive shared singletons (DB, Retrofit) get @Singleton; per-screen use cases get @ViewModelScoped so they die with the screen.",
        author = "Staff Architect",
        timestamp = 1_700_000_300_000L
    ),
    SeedPost(
        id = "seed-5",
        title = "Type-safe navigation with @Serializable routes",
        content = "Kotlin Serializable route objects replace string routes — the compiler catches a typo'd destination instead of a runtime crash.",
        author = "Staff Architect",
        timestamp = 1_700_000_400_000L
    )
)
