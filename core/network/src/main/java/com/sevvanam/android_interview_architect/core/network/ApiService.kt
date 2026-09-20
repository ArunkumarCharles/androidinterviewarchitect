package com.sevvanam.android_interview_architect.core.network

import com.sevvanam.android_interview_architect.core.model.Post
import com.sevvanam.android_interview_architect.core.model.Topic
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("topics")
    suspend fun getTopics(): List<Topic>
}
