package com.sevvanam.android_interview_architect.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing user profile settings.
 */
@Serializable
data class UserProfile(
    val userId: String,
    val username: String,
    val email: String,
    val bio: String,
    val notificationsEnabled: Boolean,
    /** "system" | "light" | "dark". Kept as a string to match what DataStore persists. */
    val themeMode: String = "system"
)
