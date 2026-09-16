package com.sevvanam.android_interview_architect.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.usecase.GetFeedUseCase
import com.sevvanam.android_interview_architect.domain.usecase.RefreshFeedUseCase
import com.sevvanam.android_interview_architect.domain.usecase.ToggleLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI (Model-View-Intent) architecture implementation for the Feed screen.
 * - State: Immutable UI state object.
 * - Intent: User actions captured as a sealed hierarchy.
 * - Reducer: Predictable state updates triggered by intents.
 */
sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(val posts: List<Post>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

sealed interface FeedIntent {
    object LoadFeed : FeedIntent
    object Refresh : FeedIntent
    data class ToggleLike(val postId: String, val currentStatus: Boolean) : FeedIntent
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val refreshFeedUseCase: RefreshFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        handleIntent(FeedIntent.LoadFeed)
    }

    fun handleIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.LoadFeed -> loadFeed()
            is FeedIntent.Refresh -> refresh()
            is FeedIntent.ToggleLike -> toggleLike(intent.postId, intent.currentStatus)
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            getFeedUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = FeedUiState.Loading
                    is Result.Success -> _uiState.value = FeedUiState.Success(result.data)
                    is Result.Error -> _uiState.value = FeedUiState.Error(result.message ?: "Unknown error")
                }
            }
        }
    }

    // The reactive Room-backed flow that loadFeed() already collects pushes the refreshed data
    // through automatically once the repository sync completes — this just triggers that sync.
    // refreshFeedUseCase() rethrows on failure (PostRepositoryImpl.refreshFeed() needs to, so
    // CacheSyncWorker can see it for retry) — a manual pull-to-refresh tap has to swallow that
    // itself, or a transient network failure here would crash the app instead of just leaving
    // the already-visible cached list on screen.
    private fun refresh() {
        viewModelScope.launch {
            try {
                refreshFeedUseCase()
            } catch (e: Exception) {
                // Cached data is still showing via loadFeed()'s ongoing collection; nothing to do.
            }
        }
    }

    private fun toggleLike(postId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            toggleLikeUseCase(postId, currentStatus)
        }
    }
}
