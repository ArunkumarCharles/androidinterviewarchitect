package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SyncTopicsUseCase @Inject constructor(
    private val repository: TopicRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.syncTopics()
}
