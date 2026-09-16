package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * UseCase persisting the notifications-enabled preference, mirroring UpdateBioUseCase's
 * one-action-per-use-case granularity.
 */
class ToggleNotificationsUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }
}
