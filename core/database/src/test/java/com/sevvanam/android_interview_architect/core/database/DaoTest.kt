package com.sevvanam.android_interview_architect.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import com.sevvanam.android_interview_architect.core.database.entity.QuestionEntity
import com.sevvanam.android_interview_architect.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** In-memory Room on the JVM (Robolectric): exercises real SQL without an emulator. */
@RunWith(RobolectricTestRunner::class)
class DaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    private fun post(id: String, ts: Long, liked: Boolean = false) = PostEntity(id, "t$id", "c", "a", ts, liked)

    @Test
    fun `posts are ordered newest first`() = runTest {
        db.postDao().insertPosts(listOf(post("old", 1), post("new", 2)))

        assertEquals(listOf("new", "old"), db.postDao().getPosts().first().map { it.id })
    }

    @Test
    fun `updateLikeStatus only changes the target row and re-emits`() = runTest {
        db.postDao().insertPosts(listOf(post("a", 1), post("b", 2)))

        db.postDao().getPosts().test {
            awaitItem()
            db.postDao().updateLikeStatus("a", true)
            val updated = awaitItem().associateBy { it.id }
            assertTrue(updated.getValue("a").isLiked)
            assertEquals(false, updated.getValue("b").isLiked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `topic upsert keeps questions but deleting a topic cascades to them`() = runTest {
        db.topicDao().insertTopics(listOf(TopicEntity("room", "Room", "d", null)))
        db.openHelper.writableDatabase.execSQL(
            "INSERT INTO questions (id, topicId, question, answer) VALUES ('q1','room','Q','A')"
        )

        // @Upsert updates in place; REPLACE would delete the parent first and cascade-delete the question.
        db.topicDao().insertTopics(listOf(TopicEntity("room", "Room v2", "d", null)))
        assertEquals(listOf(QuestionEntity("q1", "room", "Q", "A")), db.questionDao().getQuestions().first())

        db.topicDao().clearTopics()
        assertTrue(db.questionDao().getQuestions().first().isEmpty())
    }
}
