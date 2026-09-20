package com.sevvanam.android_interview_architect.feature.topic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sevvanam.android_interview_architect.core.model.Question
import com.sevvanam.android_interview_architect.core.model.Topic

@Composable
fun TopicRoute(
    modifier: Modifier = Modifier,
    viewModel: TopicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TopicEvent.SyncFailed ->
                    snackbarHostState.showSnackbar("Couldn't refresh topics. Showing saved content.")
            }
        }
    }

    TopicScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onToggleTopic = viewModel::toggleTopic,
        onQueryChange = viewModel::onQueryChange,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(
    uiState: TopicUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onToggleTopic: (String) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Interview Topics") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is TopicUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TopicUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TopicUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchField(
                            query = uiState.query,
                            onQueryChange = onQueryChange
                        )
                        if (uiState.topics.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = if (uiState.query.isBlank()) "No topics available yet." else "No matches.",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(uiState.topics, key = { it.id }) { topic ->
                                    TopicItem(
                                        topic = topic,
                                        questions = uiState.questionsByTopic[topic.id].orEmpty(),
                                        expanded = topic.id in uiState.expandedTopicIds,
                                        onClick = { onToggleTopic(topic.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // The ViewModel's `query` is debounced before it comes back in state, so echoing it into the field
    // would drop keystrokes. Keep the raw text local and push changes up.
    var text by rememberSaveable { mutableStateOf(query) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        singleLine = true,
        label = { Text("Search topics and questions") },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(
    topic: Topic,
    modifier: Modifier = Modifier,
    questions: List<Question> = emptyList(),
    expanded: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = topic.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (topic.description.isNotBlank()) {
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (questions.isNotEmpty()) {
                Text(
                    text = if (expanded) "Hide questions" else "${questions.size} questions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (expanded) {
                questions.forEach { QuestionItem(it) }
            }
        }
    }
}

@Composable
private fun QuestionItem(question: Question, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 12.dp)) {
        HorizontalDivider()
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = question.answer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
