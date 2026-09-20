package com.sevvanam.android_interview_architect.core.data.repository

import com.sevvanam.android_interview_architect.core.data.mapper.toDomain
import com.sevvanam.android_interview_architect.core.data.mapper.toEntity
import com.sevvanam.android_interview_architect.core.database.dao.TopicDao
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.core.network.ApiService
import com.sevvanam.android_interview_architect.domain.repository.TopicRepository
import com.sevvanam.android_interview_architect.core.data.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TopicRepositoryImpl @Inject constructor(
    private val topicDao: TopicDao,
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TopicRepository {

    override fun getTopics(): Flow<List<Topic>> {
        return topicDao.getTopics().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun syncTopics(): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val networkTopics = apiService.getTopics()
                topicDao.insertTopics(networkTopics.map { it.toEntity() })
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}
