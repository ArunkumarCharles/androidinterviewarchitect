package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * UseCase inverting a post's like status. Takes the caller's known current status rather than
 * re-reading it from the repository, since the ViewModel already holds it in UI state.
 */
@ViewModelScoped
class ToggleLikeUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String, currentLikeStatus: Boolean) {
        repository.toggleLike(postId, !currentLikeStatus)
    }
}
