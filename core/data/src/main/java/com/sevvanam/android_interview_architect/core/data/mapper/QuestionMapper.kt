package com.sevvanam.android_interview_architect.core.data.mapper

import com.sevvanam.android_interview_architect.core.database.entity.QuestionEntity
import com.sevvanam.android_interview_architect.core.model.Question

fun QuestionEntity.toDomain() = Question(
    id = id,
    topicId = topicId,
    question = question,
    answer = answer
)
