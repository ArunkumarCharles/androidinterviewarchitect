package com.sevvanam.android_interview_architect.domain.repository

import com.sevvanam.android_interview_architect.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for reading/updating the user's profile settings.
 */
interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun updateBio(bio: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
