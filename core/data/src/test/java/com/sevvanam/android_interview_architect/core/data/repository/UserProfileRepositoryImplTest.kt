package com.sevvanam.android_interview_architect.core.data.repository

import app.cash.turbine.test
import com.sevvanam.android_interview_architect.core.datastore.UserPreferencesDataSource
import com.sevvanam.android_interview_architect.core.model.UserProfile
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileRepositoryImplTest {

    private val bio = MutableStateFlow("bio")
    private val notifications = MutableStateFlow(true)
    private val theme = MutableStateFlow("system")

    private val prefs: UserPreferencesDataSource = mockk(relaxUnitFun = true) {
        every { this@mockk.bio } returns this@UserProfileRepositoryImplTest.bio
        every { notificationsEnabled } returns notifications
        every { themeMode } returns theme
    }
    private val repository = UserProfileRepositoryImpl(prefs)

    @Test
    fun `observeProfile combines editable preferences with static identity`() = runTest {
        repository.observeProfile().test {
            assertEquals(
                UserProfile(
                    userId = "usr_123",
                    username = "Senior Architect",
                    email = "architect@sevvanam.com",
                    bio = "bio",
                    notificationsEnabled = true,
                    themeMode = "system"
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `observeProfile re-emits when a preference changes`() = runTest {
        repository.observeProfile().test {
            awaitItem()

            theme.value = "dark"
            assertEquals("dark", awaitItem().themeMode)

            notifications.value = false
            assertEquals(false, awaitItem().notificationsEnabled)
        }
    }

    @Test
    fun `setters delegate to DataStore`() = runTest {
        repository.updateBio("new bio")
        repository.setThemeMode("light")
        repository.setNotificationsEnabled(false)

        coVerify { prefs.setBio("new bio") }
        coVerify { prefs.setThemeMode("light") }
        coVerify { prefs.setNotificationsEnabled(false) }
    }
}
