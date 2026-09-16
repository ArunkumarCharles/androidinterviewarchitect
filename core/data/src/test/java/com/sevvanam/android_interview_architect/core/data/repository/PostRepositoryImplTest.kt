package com.sevvanam.android_interview_architect.core.data.repository

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.database.dao.PostDao
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import com.sevvanam.android_interview_architect.core.datastore.UserPreferencesDataSource
import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.network.ApiService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Verifies the offline-first contract of PostRepositoryImpl: cached data renders before the
 * network resolves, a failed sync doesn't turn into a user-facing error while cache exists, and a
 * background sync never clobbers a locally-toggled like.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryImplTest {

    private val postDao: PostDao = mockk()
    private val apiService: ApiService = mockk()
    private val preferencesDataSource: UserPreferencesDataSource = mockk(relaxUnitFun = true)

    private fun repository() = PostRepositoryImpl(postDao, apiService, preferencesDataSource)

    private fun entity(id: String, isLiked: Boolean) = PostEntity(
        id = id,
        title = "Title $id",
        content = "Content",
        author = "Author",
        timestamp = 1L,
        isLiked = isLiked
    )

    private fun post(id: String, isLiked: Boolean) = Post(
        id = id,
        title = "Title $id",
        content = "Content",
        author = "Author",
        timestamp = 1L,
        isLiked = isLiked
    )

    @Test
    fun `cached snapshot is emitted before the network call resolves`() = runTest {
        val cached = listOf(entity("1", isLiked = false))
        coEvery { postDao.getPostsSnapshot() } returns cached
        val networkGate = CompletableDeferred<List<Post>>()
        coEvery { apiService.getPosts() } coAnswers { networkGate.await() }
        coEvery { postDao.insertPosts(any()) } returns Unit
        every { postDao.getPosts() } returns flowOf(cached)

        repository().getFeedStream().test {
            assertEquals(Result.Loading, awaitItem())

            val cachedItem = awaitItem()
            assertTrue(cachedItem is Result.Success<*>)
            assertEquals(listOf(post("1", isLiked = false)), (cachedItem as Result.Success<*>).data)

            // Only now let the network call resolve; the cached item above must already have
            // arrived, proving the cache doesn't wait on the network.
            networkGate.complete(emptyList())

            val finalItem = awaitItem()
            assertTrue(finalItem is Result.Success<*>)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network failure still yields cached data, not Result Error`() = runTest {
        val cached = listOf(entity("1", isLiked = false))
        coEvery { postDao.getPostsSnapshot() } returns cached
        coEvery { apiService.getPosts() } throws IOException("offline")
        every { postDao.getPosts() } returns flowOf(cached)

        repository().getFeedStream().test {
            assertEquals(Result.Loading, awaitItem())
            assertTrue(awaitItem() is Result.Success<*>)
            assertTrue(awaitItem() is Result.Success<*>)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `locally liked post survives a sync where the network reports it unliked`() = runTest {
        val likedLocally = entity("1", isLiked = true)
        coEvery { postDao.getPostsSnapshot() } returns listOf(likedLocally)
        coEvery { apiService.getPosts() } returns listOf(post("1", isLiked = false))
        var insertedEntities: List<PostEntity> = emptyList()
        coEvery { postDao.insertPosts(any()) } coAnswers { insertedEntities = firstArg() }

        repository().refreshFeed()

        assertTrue(insertedEntities.single { it.id == "1" }.isLiked)
    }
}
