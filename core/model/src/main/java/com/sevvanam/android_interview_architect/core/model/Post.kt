package com.sevvanam.android_interview_architect.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a feed post.
 * Pure Kotlin data class with kotlinx.serialization annotations for network mapping.
 */
@Serializable
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val timestamp: Long,
    val isLiked: Boolean = false
)
