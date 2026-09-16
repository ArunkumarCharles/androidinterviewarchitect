package com.sevvanam.android_interview_architect.core.data.mapper

import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import com.sevvanam.android_interview_architect.core.model.Post

fun PostEntity.toDomain(): Post = Post(
    id = id,
    title = title,
    content = content,
    author = author,
    timestamp = timestamp,
    isLiked = isLiked
)

// isLiked defaults to the Post's own value but is exposed as a parameter so a sync can preserve
// a locally-known like state instead of blindly trusting what the network returned.
fun Post.toEntity(isLiked: Boolean = this.isLiked): PostEntity = PostEntity(
    id = id,
    title = title,
    content = content,
    author = author,
    timestamp = timestamp,
    isLiked = isLiked
)
