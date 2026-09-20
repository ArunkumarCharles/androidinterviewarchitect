package com.sevvanam.android_interview_architect.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// CASCADE so clearing/replacing a topic never leaves orphaned questions behind.
// The index on topicId keeps the FK check and per-topic lookups from scanning the table.
@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val question: String,
    val answer: String
)
