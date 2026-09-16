package com.sevvanam.android_interview_architect.domain.repository

import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface adhering to Clean Architecture Dependency Inversion.
 */
interface PostRepository {
    fun getFeedStream(): Flow<Result<List<Post>>>
    suspend fun refreshFeed()
    suspend fun toggleLike(postId: String, isLiked: Boolean)
}
