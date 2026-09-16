package com.sevvanam.android_interview_architect.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sevvanam.android_interview_architect.core.database.dao.PostDao
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
