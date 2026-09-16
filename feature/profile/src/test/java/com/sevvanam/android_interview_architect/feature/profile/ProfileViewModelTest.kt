package com.sevvanam.android_interview_architect.feature.profile

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.model.UserProfile
import com.sevvanam.android_interview_architect.domain.usecase.GetUserProfileUseCase
import com.sevvanam.android_interview_architect.domain.usecase.ToggleNotificationsUseCase
import com.sevvanam.android_interview_architect.domain.usecase.UpdateBioUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val updateBioUseCase: UpdateBioUseCase = mockk(relaxUnitFun = true)
    private val toggleNotificationsUseCase: ToggleNotificationsUseCase = mockk(relaxUnitFun = true)
    private val testDispatcher = StandardTestDispatcher()

    private val profile = UserProfile(
        userId = "usr_123",
        username = "Senior Architect",
        email = "architect@sevvanam.com",
        bio = "Staff Android Engineer & Tech Interview Coach",
        notificationsEnabled = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `userProfile emits the value from GetUserProfileUseCase`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(profile)
        val viewModel = ProfileViewModel(getUserProfileUseCase, updateBioUseCase, toggleNotificationsUseCase)

        viewModel.userProfile.test {
            assertEquals(null, awaitItem())
            assertEquals(profile, awaitItem())
        }
    }

    @Test
    fun `updateBio delegates to UpdateBioUseCase`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(profile)
        coEvery { updateBioUseCase(any()) } returns Unit
        val viewModel = ProfileViewModel(getUserProfileUseCase, updateBioUseCase, toggleNotificationsUseCase)

        viewModel.updateBio("New bio")
        advanceUntilIdle()

        coVerify { updateBioUseCase("New bio") }
    }

    @Test
    fun `toggleNotifications delegates to ToggleNotificationsUseCase`() = runTest {
        every { getUserProfileUseCase() } returns flowOf(profile)
        coEvery { toggleNotificationsUseCase(any()) } returns Unit
        val viewModel = ProfileViewModel(getUserProfileUseCase, updateBioUseCase, toggleNotificationsUseCase)

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        coVerify { toggleNotificationsUseCase(false) }
    }
}
