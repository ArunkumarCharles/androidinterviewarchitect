package com.sevvanam.android_interview_architect.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity for posts. Separated from domain model to maintain Clean Architecture isolation.
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val timestamp: Long,
    val isLiked: Boolean
)
