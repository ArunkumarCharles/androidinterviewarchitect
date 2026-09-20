package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * UseCase exposing the reactive profile stream (bio/notifications persisted via :core:datastore).
 */
@ViewModelScoped
class GetUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.observeProfile()
}
