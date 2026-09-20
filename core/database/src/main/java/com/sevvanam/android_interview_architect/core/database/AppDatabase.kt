package com.sevvanam.android_interview_architect.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sevvanam.android_interview_architect.core.database.dao.PostDao
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import com.sevvanam.android_interview_architect.core.database.dao.QuestionDao
import com.sevvanam.android_interview_architect.core.database.dao.TopicDao
import com.sevvanam.android_interview_architect.core.database.entity.QuestionEntity
import com.sevvanam.android_interview_architect.core.database.entity.TopicEntity

@Database(entities = [PostEntity::class, TopicEntity::class, QuestionEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
}
