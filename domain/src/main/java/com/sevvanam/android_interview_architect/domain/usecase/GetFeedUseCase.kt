package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase encapsulating the business logic for fetching feed posts.
 * Promotes single responsibility and testability in interview architectures.
 */
class GetFeedUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<Result<List<Post>>> {
        return repository.getFeedStream()
    }
}
