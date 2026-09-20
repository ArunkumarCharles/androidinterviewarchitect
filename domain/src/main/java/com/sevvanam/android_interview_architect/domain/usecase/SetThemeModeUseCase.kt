package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/** Persists the theme choice ("system" | "light" | "dark"). */
@ViewModelScoped
class SetThemeModeUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(mode: String) {
        repository.setThemeMode(mode)
    }
}
