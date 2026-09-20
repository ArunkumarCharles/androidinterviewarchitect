package com.sevvanam.android_interview_architect.core.data.repository

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.database.dao.QuestionDao
import com.sevvanam.android_interview_architect.core.database.entity.QuestionEntity
import com.sevvanam.android_interview_architect.core.model.Question
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionRepositoryImplTest {

    private val questionDao: QuestionDao = mockk()

    @Test
    fun `getQuestions maps entities to domain models`() = runTest {
        every { questionDao.getQuestions() } returns flowOf(
            listOf(QuestionEntity("room-1", "room", "Q?", "A."))
        )

        QuestionRepositoryImpl(questionDao).getQuestions().test {
            assertEquals(listOf(Question("room-1", "room", "Q?", "A.")), awaitItem())
            awaitComplete()
        }
    }
}
