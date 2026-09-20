package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * UseCase for a user-triggered manual refresh, distinct from the reactive getFeedStream() collection
 * that FeedViewModel keeps observing throughout the screen's lifetime.
 */
@ViewModelScoped
class RefreshFeedUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke() {
        repository.refreshFeed()
    }
}
