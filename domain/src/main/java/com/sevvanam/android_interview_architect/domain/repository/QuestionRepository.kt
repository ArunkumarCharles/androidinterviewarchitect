package com.sevvanam.android_interview_architect.domain.repository

import com.sevvanam.android_interview_architect.core.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    /** All questions; callers group by [Question.topicId]. */
    fun getQuestions(): Flow<List<Question>>
}
