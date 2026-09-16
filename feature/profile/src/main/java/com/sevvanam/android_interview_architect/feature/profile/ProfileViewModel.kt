package com.sevvanam.android_interview_architect.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.usecase.GetUserProfileUseCase
import com.sevvanam.android_interview_architect.domain.usecase.ToggleNotificationsUseCase
import com.sevvanam.android_interview_architect.domain.usecase.UpdateBioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVVM architecture implementation for User Profile & Settings screen.
 * bio/notificationsEnabled are persisted via :core:datastore (through the domain use cases below),
 * so edits survive process death without needing SavedStateHandle here.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserProfileUseCase: GetUserProfileUseCase,
    private val updateBioUseCase: UpdateBioUseCase,
    private val toggleNotificationsUseCase: ToggleNotificationsUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = getUserProfileUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateBio(newBio: String) {
        viewModelScope.launch {
            updateBioUseCase(newBio)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            toggleNotificationsUseCase(enabled)
        }
    }
}
