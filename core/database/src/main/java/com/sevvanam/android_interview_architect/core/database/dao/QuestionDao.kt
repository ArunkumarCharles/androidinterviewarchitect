package com.sevvanam.android_interview_architect.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.sevvanam.android_interview_architect.core.database.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id")
    fun getQuestions(): Flow<List<QuestionEntity>>
}
