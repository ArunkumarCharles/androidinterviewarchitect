package com.sevvanam.android_interview_architect.core.data.repository

import android.util.Log
import com.sevvanam.android_interview_architect.core.data.mapper.toDomain
import com.sevvanam.android_interview_architect.core.data.mapper.toEntity
import com.sevvanam.android_interview_architect.core.database.dao.PostDao
import com.sevvanam.android_interview_architect.core.datastore.UserPreferencesDataSource
import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.core.network.ApiService
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

private const val TAG = "PostRepositoryImpl"

/**
 * Offline-first repository implementation demonstrating Single Source of Truth pattern.
 * Room DB acts as the SSOT. Network data is fetched and upserted into Room.
 */
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val preferencesDataSource: UserPreferencesDataSource
) : PostRepository {

    override fun getFeedStream(): Flow<Result<List<Post>>> = flow {
        emit(Result.Loading)

        // Emit the current Room snapshot immediately (Offline-first / SSOT) so the UI renders
        // instantly without waiting for the network round-trip below.
        emit(Result.Success(postDao.getPostsSnapshot().map { it.toDomain() }))

        try {
            syncRemotePosts()
        } catch (e: Exception) {
            // Offline-first tolerates a failed background sync as long as cached data already
            // exists: we log it but deliberately don't propagate to Result.Error here, since the
            // reactive getPosts() collection below still has stale-but-valid data to show.
            Log.w(TAG, "Background feed sync failed; continuing with cached data", e)
        }

        // Stay subscribed so any further local writes (e.g. toggleLike, or a later successful
        // CacheSyncWorker run) keep pushing fresh state to the UI reactively.
        emitAll(postDao.getPosts().map { entities -> Result.Success(entities.map { it.toDomain() }) })
    }.catch { throwable ->
        emit(Result.Error(throwable))
    }

    override suspend fun refreshFeed() {
        try {
            syncRemotePosts()
        } catch (e: Exception) {
            Log.w(TAG, "refreshFeed sync failed", e)
            // Rethrown (unlike the tolerant swallow in getFeedStream) because callers of this
            // suspend function — notably CacheSyncWorker — need the failure to decide retry vs.
            // giving up, and silently "succeeding" here would break that retry logic.
            throw e
        }
    }

    override suspend fun toggleLike(postId: String, isLiked: Boolean) {
        postDao.updateLikeStatus(postId, isLiked)
    }

    /**
     * Fetches posts from the network and upserts them into Room, preserving any locally-toggled
     * like state. insertPosts uses REPLACE, so without this merge step a background sync would
     * silently overwrite a user's like (the network's Post.isLiked always defaults to false).
     */
    private suspend fun syncRemotePosts() {
        val locallyLikedIds = postDao.getPostsSnapshot()
            .filter { it.isLiked }
            .map { it.id }
            .toSet()

        val remotePosts = apiService.getPosts()
        val entities = remotePosts.map { post ->
            post.toEntity(isLiked = post.id in locallyLikedIds)
        }
        postDao.insertPosts(entities)
        preferencesDataSource.setLastSyncTime(Instant.now().toString())
    }
}
