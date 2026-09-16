package com.sevvanam.android_interview_architect.feature.feed

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.usecase.GetFeedUseCase
import com.sevvanam.android_interview_architect.domain.usecase.RefreshFeedUseCase
import com.sevvanam.android_interview_architect.domain.usecase.ToggleLikeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val getFeedUseCase: GetFeedUseCase = mockk()
    private val toggleLikeUseCase: ToggleLikeUseCase = mockk()
    private val refreshFeedUseCase: RefreshFeedUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFeed success updates uiState to Success`() = runTest {
        val posts = listOf(
            Post("1", "Test Title", "Test Content", "Author", 123456L, false)
        )
        coEvery { getFeedUseCase() } returns flowOf(Result.Success(posts))

        viewModel = FeedViewModel(getFeedUseCase, toggleLikeUseCase, refreshFeedUseCase)

        viewModel.uiState.test {
            assertEquals(FeedUiState.Loading, awaitItem())
            val item = awaitItem()
            assert(item is FeedUiState.Success)
            assertEquals(posts, (item as FeedUiState.Success).posts)
        }
    }

    @Test
    fun `Refresh intent swallows a rethrown sync failure instead of crashing`() = runTest {
        val posts = listOf(Post("1", "Test Title", "Test Content", "Author", 123456L, false))
        coEvery { getFeedUseCase() } returns flowOf(Result.Success(posts))
        // PostRepositoryImpl.refreshFeed() rethrows on failure (so CacheSyncWorker can retry) --
        // FeedViewModel.refresh() must not let that propagate uncaught out of viewModelScope.
        coEvery { refreshFeedUseCase() } throws IOException("offline")

        viewModel = FeedViewModel(getFeedUseCase, toggleLikeUseCase, refreshFeedUseCase)
        viewModel.handleIntent(FeedIntent.Refresh)
        advanceUntilIdle()

        assert(viewModel.uiState.value is FeedUiState.Success)
    }

    @Test
    fun `loadFeed error updates uiState to Error`() = runTest {
        coEvery { getFeedUseCase() } returns flowOf(Result.Error(IllegalStateException("network down")))

        viewModel = FeedViewModel(getFeedUseCase, toggleLikeUseCase, refreshFeedUseCase)

        viewModel.uiState.test {
            assertEquals(FeedUiState.Loading, awaitItem())
            val item = awaitItem()
            assert(item is FeedUiState.Error)
            assertEquals("network down", (item as FeedUiState.Error).message)
        }
    }
}
