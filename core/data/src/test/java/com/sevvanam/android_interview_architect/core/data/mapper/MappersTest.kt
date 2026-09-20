package com.sevvanam.android_interview_architect.core.data.mapper

import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import com.sevvanam.android_interview_architect.core.database.entity.QuestionEntity
import com.sevvanam.android_interview_architect.core.database.entity.TopicEntity
import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.core.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private val post = Post("p1", "Title", "Body", "Author", 42L, isLiked = true)

    @Test
    fun `Post round-trips through entity`() {
        assertEquals(post, post.toEntity().toDomain())
    }

    @Test
    fun `Post toEntity keeps its own isLiked by default`() {
        assertTrue(post.toEntity().isLiked)
    }

    @Test
    fun `Post toEntity lets a sync override isLiked with the local value`() {
        assertFalse(post.toEntity(isLiked = false).isLiked)
    }

    @Test
    fun `PostEntity maps every field to the domain model`() {
        val entity = PostEntity("p2", "T", "C", "A", 7L, false)

        assertEquals(Post("p2", "T", "C", "A", 7L, false), entity.toDomain())
    }

    @Test
    fun `Topic round-trips through entity including null imageUrl`() {
        val withImage = Topic("room", "Room", "Offline", "https://img")
        val withoutImage = Topic("hilt", "Hilt", "DI", null)

        assertEquals(withImage, withImage.toEntity().toDomain())
        assertEquals(withoutImage, withoutImage.toEntity().toDomain())
    }

    @Test
    fun `TopicEntity maps to domain`() {
        assertEquals(Topic("a", "B", "C", null), TopicEntity("a", "B", "C", null).toDomain())
    }

    @Test
    fun `QuestionEntity maps to domain`() {
        assertEquals(
            Question("q1", "room", "Why?", "Because."),
            QuestionEntity("q1", "room", "Why?", "Because.").toDomain()
        )
    }
}
