package com.sevvanam.android_interview_architect.feature.feed

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sevvanam.android_interview_architect.core.model.Post
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FeedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun feedContent_displaysPostsAndHandlesLike() {
        val posts = listOf(
            Post("1", "Compose Architecture", "Detailed explanation of MVI and Compose", "Author", 1000L, false)
        )

        var likedPostId: String? = null

        composeTestRule.setContent {
            FeedContent(
                uiState = FeedUiState.Success(posts),
                onIntent = { intent ->
                    if (intent is FeedIntent.ToggleLike) {
                        likedPostId = intent.postId
                    }
                }
            )
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText("Compose Architecture").assertExists()

        // Verify clicking the like button dispatches ToggleLike for the right post
        composeTestRule.onNodeWithText("Like 🤍").performClick()
        assertEquals("1", likedPostId)
    }
}
