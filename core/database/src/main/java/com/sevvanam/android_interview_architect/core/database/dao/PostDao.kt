package com.sevvanam.android_interview_architect.core.database.dao

import androidx.room.Dao
import androidx.paging.PagingSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getPosts(): Flow<List<PostEntity>>

    // Room generates a PagingSource that loads pages with LIMIT/OFFSET and auto-invalidates on table change.
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getPostsPaged(): PagingSource<Int, PostEntity>

    // One-shot snapshot (vs. the reactive getPosts() Flow above) used by the repository to read
    // locally-liked post IDs before a network sync overwrites rows via insertPosts's REPLACE strategy.
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    suspend fun getPostsSnapshot(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("UPDATE posts SET isLiked = :isLiked WHERE id = :postId")
    suspend fun updateLikeStatus(postId: String, isLiked: Boolean)
}
