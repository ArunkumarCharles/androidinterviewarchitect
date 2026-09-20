package com.sevvanam.android_interview_architect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevvanam.android_interview_architect.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Activity-level state: only the theme choice, read through the same use case the Profile screen uses. */
@HiltViewModel
class MainViewModel @Inject constructor(
    getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {
    // "system" until DataStore emits, so first frame follows the OS setting rather than flashing.
    val themeMode: StateFlow<String> = getUserProfileUseCase()
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")
}
