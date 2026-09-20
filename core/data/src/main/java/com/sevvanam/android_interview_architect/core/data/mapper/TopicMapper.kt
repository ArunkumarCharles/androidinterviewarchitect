package com.sevvanam.android_interview_architect.core.data.mapper

import com.sevvanam.android_interview_architect.core.database.entity.TopicEntity
import com.sevvanam.android_interview_architect.core.model.Topic

fun TopicEntity.toDomain() = Topic(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl
)

fun Topic.toEntity() = TopicEntity(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl
)
