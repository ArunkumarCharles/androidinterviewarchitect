package com.sevvanam.android_interview_architect.core.model

import kotlinx.serialization.Serializable

/**
 * A single interview question with its model answer, belonging to a [Topic].
 */
@Serializable
data class Question(
    val id: String,
    val topicId: String,
    val question: String,
    val answer: String
)
