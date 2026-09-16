package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase exposing the reactive profile stream (bio/notifications persisted via :core:datastore).
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.observeProfile()
}
