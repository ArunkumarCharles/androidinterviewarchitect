package com.sevvanam.android_interview_architect.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sevvanam.android_interview_architect.core.database.AppDatabase
import com.sevvanam.android_interview_architect.core.database.MIGRATION_1_2
import com.sevvanam.android_interview_architect.core.database.dao.PostDao
import com.sevvanam.android_interview_architect.core.database.dao.QuestionDao
import com.sevvanam.android_interview_architect.core.database.dao.TopicDao
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
            // this teaching sample), so network syncs always fail. Seed posts, topics and
            // questions (see SeedData.kt) on first DB creation so the offline-first screens have
            // real content instead of a permanently blank list.
            // Explicit migrations keep user data (likes) across upgrades. Destructive fallback is limited
            // to downgrades, which only happen on dev devices.
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedTopicsAndQuestions(db)
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

    @Provides
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    fun provideTopicDao(database: AppDatabase): TopicDao {
        return database.topicDao()
    }
}
