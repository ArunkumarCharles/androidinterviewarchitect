package com.sevvanam.android_interview_architect.domain.usecase

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.model.OrderConfirmation
import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.repository.CheckoutRepository
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import com.sevvanam.android_interview_architect.domain.repository.QuestionRepository
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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

    @Test
    fun `ToggleLike turns a not-yet-liked post into liked`() = runTest {
        val repo = mockk<PostRepository>(relaxUnitFun = true)

        ToggleLikeUseCase(repo)("p1", currentLikeStatus = false)

        coVerify(exactly = 1) { repo.toggleLike("p1", true) }
    }

    @Test
    fun `RefreshFeed asks the repository to refresh`() = runTest {
        val repo = mockk<PostRepository>(relaxUnitFun = true)

        RefreshFeedUseCase(repo)()

        coVerify(exactly = 1) { repo.refreshFeed() }
    }

    @Test
    fun `GetFeed exposes the repository stream`() = runTest {
        val repo = mockk<PostRepository>()
        every { repo.getFeedStream() } returns flowOf(Result.Success(emptyList()))

        GetFeedUseCase(repo)().test {
            assertEquals(Result.Success(emptyList<com.sevvanam.android_interview_architect.core.model.Post>()), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `GetTopics and GetQuestions expose repository streams`() = runTest {
        val topics = listOf(Topic("t", "T", "d"))
        val questions = listOf(Question("q", "t", "Q?", "A."))
        val topicRepo = mockk<TopicRepository> { every { getTopics() } returns flowOf(topics) }
        val questionRepo = mockk<QuestionRepository> { every { getQuestions() } returns flowOf(questions) }

        GetTopicsUseCase(topicRepo)().test { assertEquals(topics, awaitItem()); awaitComplete() }
        GetQuestionsUseCase(questionRepo)().test { assertEquals(questions, awaitItem()); awaitComplete() }
    }

    @Test
    fun `profile use cases delegate to the repository`() = runTest {
        val profile = UserProfile("id", "n", "e", "bio", true)
        val repo = mockk<UserProfileRepository>(relaxUnitFun = true) {
            every { observeProfile() } returns flowOf(profile)
        }

        GetUserProfileUseCase(repo)().test { assertEquals(profile, awaitItem()); awaitComplete() }
        UpdateBioUseCase(repo)("new bio")
        ToggleNotificationsUseCase(repo)(false)
        SetThemeModeUseCase(repo)("dark")

        coVerify { repo.updateBio("new bio") }
        coVerify { repo.setNotificationsEnabled(false) }
        coVerify { repo.setThemeMode("dark") }
    }

    @Test
    fun `SubmitOrder passes address and card through and returns the result`() = runTest {
        val confirmation = Result.Success(OrderConfirmation("o1", 1L))
        val repo = mockk<CheckoutRepository>()
        coEvery { repo.submitOrder("addr", "4242") } returns confirmation

        assertEquals(confirmation, SubmitOrderUseCase(repo)("addr", "4242"))
    }
}
