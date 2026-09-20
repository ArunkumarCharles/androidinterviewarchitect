package com.sevvanam.android_interview_architect.core.data.repository

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.database.dao.TopicDao
import com.sevvanam.android_interview_architect.core.database.entity.TopicEntity
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.model.Topic
import com.sevvanam.android_interview_architect.core.network.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicRepositoryImplTest {

    private val topicDao: TopicDao = mockk(relaxUnitFun = true)
    private val apiService: ApiService = mockk()
    private val repository = TopicRepositoryImpl(topicDao, apiService, UnconfinedTestDispatcher())

    @Test
    fun `getTopics maps entities to domain models`() = runTest {
        every { topicDao.getTopics() } returns flowOf(listOf(TopicEntity("room", "Room", "Offline", null)))

        repository.getTopics().test {
            assertEquals(listOf(Topic("room", "Room", "Offline", null)), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `syncTopics upserts network topics and returns Success`() = runTest {
        coEvery { apiService.getTopics() } returns listOf(Topic("hilt", "Hilt", "DI", null))

        val result = repository.syncTopics()

        assertTrue(result is Result.Success)
        coVerify { topicDao.insertTopics(listOf(TopicEntity("hilt", "Hilt", "DI", null))) }
    }

    @Test
    fun `syncTopics returns Error and leaves cache untouched when network fails`() = runTest {
        coEvery { apiService.getTopics() } throws java.io.IOException("offline")

        val result = repository.syncTopics()

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { topicDao.insertTopics(any()) }
    }
}
