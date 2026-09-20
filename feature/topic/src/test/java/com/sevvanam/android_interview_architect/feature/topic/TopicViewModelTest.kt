package com.sevvanam.android_interview_architect.feature.topic

import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.domain.repository.QuestionRepository
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import com.sevvanam.android_interview_architect.domain.usecase.GetQuestionsUseCase
import com.sevvanam.android_interview_architect.domain.usecase.GetTopicsUseCase
import com.sevvanam.android_interview_architect.domain.usecase.SyncTopicsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TopicViewModelTest {

    private val topicRepository: TopicRepository = mockk()
    private val questionRepository: QuestionRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val topic = Topic("room", "Room", "Offline-first")
    private val question = Question("room-1", "room", "Q?", "A.")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { topicRepository.getTopics() } returns flowOf(listOf(topic))
        every { questionRepository.getQuestions() } returns flowOf(listOf(question))
        coEvery { topicRepository.syncTopics() } returns Result.Success(Unit)
    }

    private fun newViewModel() = TopicViewModel(
        GetTopicsUseCase(topicRepository),
        GetQuestionsUseCase(questionRepository),
        SyncTopicsUseCase(topicRepository)
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `questions are grouped by topic and collapsed initially`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as TopicUiState.Success
        assertEquals(mapOf("room" to listOf(question)), state.questionsByTopic)
        assertEquals(emptySet(), state.expandedTopicIds)
    }

    @Test
    fun `toggleTopic expands then collapses`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.toggleTopic("room")
        advanceUntilIdle()
        assertEquals(setOf("room"), (viewModel.uiState.value as TopicUiState.Success).expandedTopicIds)

        viewModel.toggleTopic("room")
        advanceUntilIdle()
        assertEquals(emptySet(), (viewModel.uiState.value as TopicUiState.Success).expandedTopicIds)
    }

    @Test
    fun `search filters topics after debounce`() = runTest {
        val other = Topic("hilt", "Hilt", "DI")
        every { topicRepository.getTopics() } returns flowOf(listOf(topic, other))
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("hil")
        advanceTimeBy(100)
        // Debounce window not elapsed: still unfiltered.
        assertEquals(2, (viewModel.uiState.value as TopicUiState.Success).topics.size)

        advanceUntilIdle()
        assertEquals(listOf(other), (viewModel.uiState.value as TopicUiState.Success).topics)
    }

    @Test
    fun `sync failure emits one-shot event but keeps content`() = runTest {
        coEvery { topicRepository.syncTopics() } returns Result.Error(RuntimeException("offline"))
        val viewModel = newViewModel()

        viewModel.events.test {
            advanceUntilIdle()
            assertEquals(TopicEvent.SyncFailed("offline"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(topic), (viewModel.uiState.value as TopicUiState.Success).topics)
    }

    @Test
    fun `flow error surfaces as Error state`() = runTest {
        every { topicRepository.getTopics() } returns kotlinx.coroutines.flow.flow { throw IllegalStateException("db") }
        val viewModel = newViewModel()
        advanceUntilIdle()

        assertEquals(TopicUiState.Error("db"), viewModel.uiState.value)
    }
}
