package com.sevvanam.android_interview_architect.feature.topic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.core.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TopicScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val topic = Topic("room", "Room", "Offline-first storage")
    private val question = Question("room-1", "room", "What is a DAO?", "A data access object.")

    @Test
    fun collapsedTopicHidesAnswersAndClickTogglesIt() {
        composeTestRule.setContent {
            var expanded by remember { mutableStateOf(emptySet<String>()) }
            TopicScreen(
                uiState = TopicUiState.Success(
                    topics = listOf(topic),
                    questionsByTopic = mapOf("room" to listOf(question)),
                    expandedTopicIds = expanded
                ),
                onToggleTopic = { id -> expanded = if (id in expanded) expanded - id else expanded + id }
            )
        }

        composeTestRule.onNodeWithText("What is a DAO?").assertDoesNotExist()

        composeTestRule.onNodeWithText("Room").performClick()
        composeTestRule.onNodeWithText("What is a DAO?").assertExists()
        composeTestRule.onNodeWithText("A data access object.").assertExists()
    }

    @Test
    fun typingInSearchNotifiesCaller() {
        var query = ""
        composeTestRule.setContent {
            TopicScreen(
                uiState = TopicUiState.Success(topics = listOf(topic)),
                onQueryChange = { query = it }
            )
        }

        composeTestRule.onNodeWithText("Search topics and questions").performTextInput("hil")

        assertEquals("hil", query)
    }
}
