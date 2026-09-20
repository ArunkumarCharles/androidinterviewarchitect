package com.sevvanam.android_interview_architect.domain.usecase

import androidx.paging.PagingData
import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Room-backed, incrementally loaded feed. Complements GetFeedUseCase (full-list snapshot stream). */
@ViewModelScoped
class GetPagedFeedUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<PagingData<Post>> = repository.getPagedFeed()
}
