package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GetTopicsUseCase @Inject constructor(
    private val repository: TopicRepository
) {
    operator fun invoke(): Flow<List<Topic>> = repository.getTopics()
}
