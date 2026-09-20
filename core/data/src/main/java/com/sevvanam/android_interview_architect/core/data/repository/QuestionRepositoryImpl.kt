package com.sevvanam.android_interview_architect.core.data.repository

import com.sevvanam.android_interview_architect.core.data.mapper.toDomain
import com.sevvanam.android_interview_architect.core.database.dao.QuestionDao
import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Local-only: questions are bundled seed data (no backend exists), so there is no sync step.
class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    override fun getQuestions(): Flow<List<Question>> =
        questionDao.getQuestions().map { entities -> entities.map { it.toDomain() } }
}
