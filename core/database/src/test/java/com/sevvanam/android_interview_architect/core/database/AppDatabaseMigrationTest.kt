package com.sevvanam.android_interview_architect.core.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Builds a genuine v1 database (posts only, as shipped before topics existed), then opens it through
 * Room at v2 with MIGRATION_1_2. Room validates the migrated schema against the entities on open, so a
 * wrong CREATE TABLE fails here rather than on a user's device.
 */
@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "migration-test.db"

    private fun createV1() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `posts` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                                "`content` TEXT NOT NULL, `author` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, " +
                                "`isLiked` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                        )
                        db.execSQL("INSERT INTO posts VALUES ('p1','t','c','a',1,1)")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        helper.writableDatabase.close()
        helper.close()
    }

    @Test
    fun `migrating 1 to 2 keeps posts and likes and seeds topics`() = runTest {
        createV1()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val posts = db.postDao().getPostsSnapshot()
        assertEquals(1, posts.size)
        assertTrue("like survived the migration", posts.single().isLiked)
        assertTrue(db.topicDao().getTopics().first().isNotEmpty())
        assertTrue(db.questionDao().getQuestions().first().isNotEmpty())
        db.close()
    }
}
