package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.domain.repository.QuestionRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GetQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(): Flow<List<Question>> = repository.getQuestions()
}
