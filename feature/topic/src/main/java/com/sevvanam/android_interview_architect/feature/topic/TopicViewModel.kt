package com.sevvanam.android_interview_architect.feature.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.domain.usecase.GetQuestionsUseCase
import com.sevvanam.android_interview_architect.domain.usecase.GetTopicsUseCase
import com.sevvanam.android_interview_architect.domain.usecase.SyncTopicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TopicUiState {
    data object Loading : TopicUiState
    data class Success(
        val topics: List<Topic>,
        val questionsByTopic: Map<String, List<Question>> = emptyMap(),
        val expandedTopicIds: Set<String> = emptySet(),
        val query: String = ""
    ) : TopicUiState
    data class Error(val message: String) : TopicUiState
}

/** One-shot effects. Delivered through a Channel so they are consumed exactly once (not replayed on rotation). */
sealed interface TopicEvent {
    data class SyncFailed(val message: String) : TopicEvent
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopicViewModel @Inject constructor(
    private val getTopics: GetTopicsUseCase,
    private val getQuestions: GetQuestionsUseCase,
    private val syncTopics: SyncTopicsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopicUiState>(TopicUiState.Loading)
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    private val _events = Channel<TopicEvent>(Channel.BUFFERED)
    val events: Flow<TopicEvent> = _events.receiveAsFlow()

    // Kept separate from repository data so expanding a card never triggers a DB read.
    private val expandedTopicIds = MutableStateFlow<Set<String>>(emptySet())
    private val query = MutableStateFlow("")

    init {
        observeContent()
        sync()
    }

    // Private: called once from init. A public entry point could start a second collector.
    private fun observeContent() {
        // Debounce keystrokes so filtering runs once typing pauses. An empty query (initial value / cleared box)
        // passes straight through so first paint isn't delayed. distinctUntilChanged drops no-op edits.
        val debouncedQuery = query
            .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
            .distinctUntilChanged()
        viewModelScope.launch {
            combine(getTopics(), getQuestions(), expandedTopicIds, debouncedQuery) { topics, questions, expanded, q ->
                val byTopic = questions.groupBy { it.topicId }
                val needle = q.trim()
                val visible = if (needle.isEmpty()) topics else topics.filter { topic ->
                    topic.name.contains(needle, ignoreCase = true) ||
                        topic.description.contains(needle, ignoreCase = true) ||
                        byTopic[topic.id].orEmpty().any {
                            it.question.contains(needle, ignoreCase = true) ||
                                it.answer.contains(needle, ignoreCase = true)
                        }
                }
                TopicUiState.Success(
                    topics = visible,
                    questionsByTopic = byTopic,
                    expandedTopicIds = expanded,
                    query = q
                )
            }
                .onStart { _uiState.value = TopicUiState.Loading }
                .catch { e -> _uiState.value = TopicUiState.Error(e.message ?: "Unknown error") }
                .collect { _uiState.value = it }
        }
    }

    fun toggleTopic(topicId: String) {
        expandedTopicIds.update { if (topicId in it) it - topicId else it + topicId }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun sync() {
        viewModelScope.launch {
            // Cached/seeded content keeps showing on failure, so a sync error is a transient message, not a screen state.
            val result = syncTopics()
            if (result is Result.Error) {
                _events.send(TopicEvent.SyncFailed(result.message ?: "Sync failed"))
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
