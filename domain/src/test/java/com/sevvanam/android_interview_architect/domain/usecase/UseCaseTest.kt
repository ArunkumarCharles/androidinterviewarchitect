package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Plain-JVM tests: use cases take repository interfaces, so no Android or Hilt is involved. */
class UseCaseTest {

    @Test
    fun `ToggleLike writes the inverse of the current status`() = runTest {
        val repo = mockk<PostRepository>(relaxUnitFun = true)
        val useCase = ToggleLikeUseCase(repo)

        useCase("p1", currentLikeStatus = false)
        useCase("p2", currentLikeStatus = true)

        coVerify { repo.toggleLike("p1", true) }
        coVerify { repo.toggleLike("p2", false) }
    }

    @Test
    fun `SyncTopics returns the repository result unchanged`() = runTest {
        val error = Result.Error(java.io.IOException("offline"))
        val repo = mockk<TopicRepository>()
        coEvery { repo.syncTopics() } returns error

        assertEquals(error, SyncTopicsUseCase(repo)())
    }
}
