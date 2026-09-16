package com.sevvanam.android_interview_architect.core.network

import com.sevvanam.android_interview_architect.core.model.Post
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}
