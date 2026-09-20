package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * UseCase persisting an edited bio. Kept separate from ToggleNotificationsUseCase (rather than one
 * generic "updateProfile") so each user-facing edit action maps 1:1 to a single, testable intent.
 */
@ViewModelScoped
class UpdateBioUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(bio: String) {
        repository.updateBio(bio)
    }
}
