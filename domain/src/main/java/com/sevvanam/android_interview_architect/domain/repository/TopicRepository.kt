package com.sevvanam.android_interview_architect.domain.repository

import com.sevvanam.android_interview_architect.core.model.Topic
import kotlinx.coroutines.flow.Flow
import com.sevvanam.android_interview_architect.core.model.Result

interface TopicRepository {
    fun getTopics(): Flow<List<Topic>>
    suspend fun syncTopics(): Result<Unit>
}
