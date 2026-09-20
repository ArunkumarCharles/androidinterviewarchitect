package com.sevvanam.android_interview_architect

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.repository.UserProfileRepository
import com.sevvanam.android_interview_architect.domain.usecase.GetUserProfileUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val profiles = MutableSharedFlow<UserProfile>()
    private val repository: UserProfileRepository = mockk {
        every { observeProfile() } returns profiles
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun profile(theme: String) = UserProfile("id", "name", "e@x.com", "bio", true, theme)

    @Test
    fun `themeMode starts as system before DataStore emits`() = runTest {
        val viewModel = MainViewModel(GetUserProfileUseCase(repository))

        assertEquals("system", viewModel.themeMode.value)
    }

    @Test
    fun `themeMode follows the profile stream`() = runTest {
        val viewModel = MainViewModel(GetUserProfileUseCase(repository))

        viewModel.themeMode.test {
            assertEquals("system", awaitItem())

            profiles.emit(profile("dark"))
            assertEquals("dark", awaitItem())

            profiles.emit(profile("light"))
            assertEquals("light", awaitItem())
        }
    }
}
