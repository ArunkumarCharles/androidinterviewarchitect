package com.sevvanam.android_interview_architect.core.data.repository

import com.sevvanam.android_interview_architect.core.datastore.UserPreferencesDataSource
import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Backs the user profile with DataStore for the fields that are actually user-editable (bio,
 * notifications). userId/username/email are static since this sample has no auth/backend to
 * source a real identity from.
 */
class UserProfileRepositoryImpl @Inject constructor(
    private val preferencesDataSource: UserPreferencesDataSource
) : UserProfileRepository {

    private companion object {
        const val STATIC_USER_ID = "usr_123"
        const val STATIC_USERNAME = "Senior Architect"
        const val STATIC_EMAIL = "architect@sevvanam.com"
    }

    override fun observeProfile(): Flow<UserProfile> =
        combine(
            preferencesDataSource.bio,
            preferencesDataSource.notificationsEnabled,
            preferencesDataSource.themeMode
        ) { bio, notificationsEnabled, themeMode ->
            UserProfile(
                userId = STATIC_USER_ID,
                username = STATIC_USERNAME,
                email = STATIC_EMAIL,
                bio = bio,
                notificationsEnabled = notificationsEnabled,
                themeMode = themeMode
            )
        }

    override suspend fun updateBio(bio: String) {
        preferencesDataSource.setBio(bio)
    }

    override suspend fun setThemeMode(mode: String) {
        preferencesDataSource.setThemeMode(mode)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferencesDataSource.setNotificationsEnabled(enabled)
    }
}
