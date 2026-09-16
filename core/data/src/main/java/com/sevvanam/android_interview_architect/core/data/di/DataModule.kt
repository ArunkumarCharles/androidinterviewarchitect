package com.sevvanam.android_interview_architect.core.data.di

import com.sevvanam.android_interview_architect.core.data.repository.CheckoutRepositoryImpl
import com.sevvanam.android_interview_architect.core.data.repository.PostRepositoryImpl
import com.sevvanam.android_interview_architect.core.data.repository.UserProfileRepositoryImpl
import com.sevvanam.android_interview_architect.domain.repository.CheckoutRepository
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindCheckoutRepository(
        impl: CheckoutRepositoryImpl
    ): CheckoutRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository
}
