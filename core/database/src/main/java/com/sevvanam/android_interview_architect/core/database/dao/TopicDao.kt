package com.sevvanam.android_interview_architect.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sevvanam.android_interview_architect.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    fun getTopics(): Flow<List<TopicEntity>>

    // Upsert (UPDATE in place), not REPLACE: REPLACE deletes the row first, which would cascade-delete
    // the topic's questions via the foreign key.
    @Upsert
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("DELETE FROM topics")
    suspend fun clearTopics()
}
