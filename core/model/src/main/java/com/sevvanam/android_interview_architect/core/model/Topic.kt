package com.sevvanam.android_interview_architect.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing an interview topic.
 */
@Serializable
data class Topic(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null
)
