package com.sevvanam.android_interview_architect.feature.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TopicUiState {
    data object Loading : TopicUiState
    data class Success(val topics: List<Topic>) : TopicUiState
    data class Error(val message: String) : TopicUiState
}

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopicUiState>(TopicUiState.Loading)
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    init {
        loadTopics()
        syncTopics()
    }

    fun loadTopics() {
        viewModelScope.launch {
            topicRepository.getTopics()
                .onStart { _uiState.value = TopicUiState.Loading }
                .catch { e -> _uiState.value = TopicUiState.Error(e.message ?: "Unknown error") }
                .collect { topics ->
                    _uiState.value = TopicUiState.Success(topics)
                }
        }
    }

    fun syncTopics() {
        viewModelScope.launch {
            // Background sync
            topicRepository.syncTopics()
        }
    }
}
