package com.sevvanam.android_interview_architect.feature.feed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sevvanam.android_interview_architect.core.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedRoute(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedPosts = viewModel.pagedPosts.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                FeedEvent.RefreshFailed ->
                    snackbarHostState.showSnackbar("Couldn't refresh. Showing saved posts.")
            }
        }
    }

    FeedContent(
        uiState = uiState,
        onIntent = { intent -> viewModel.handleIntent(intent) },
        snackbarHostState = snackbarHostState,
        pagedPosts = pagedPosts
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    uiState: FeedUiState,
    onIntent: (FeedIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    // When provided, the list is rendered from Paging (Room-backed, loaded in pages). Loading/Error
    // still come from uiState. Null keeps the simple list path, which previews and UI tests use.
    pagedPosts: LazyPagingItems<Post>? = null
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // derivedStateOf: firstVisibleItemIndex changes on every scroll frame, but this Boolean only flips
    // when the threshold is crossed, so only the FAB visibility (not the whole screen) recomposes.
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showScrollToTop) {
                SmallFloatingActionButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Interview Architect Feed (MVI)") },
                actions = {
                    IconButton(onClick = { onIntent(FeedIntent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh feed")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is FeedUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FeedUiState.Success -> {
                    val isEmpty = if (pagedPosts != null) pagedPosts.itemCount == 0 else state.posts.isEmpty()
                    if (isEmpty) {
                        Text(
                            text = "No posts yet. Tap refresh to sync.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            if (pagedPosts != null) {
                                items(
                                    count = pagedPosts.itemCount,
                                    key = pagedPosts.itemKey { it.id }
                                ) { index ->
                                    // null only while placeholders are enabled; we disable them in the Pager.
                                    pagedPosts[index]?.let { post ->
                                        PostItem(
                                            post = post,
                                            onLikeClick = { onIntent(FeedIntent.ToggleLike(post.id, post.isLiked)) }
                                        )
                                    }
                                }
                            } else {
                                items(state.posts, key = { it.id }) { post ->
                                    PostItem(
                                        post = post,
                                        onLikeClick = { onIntent(FeedIntent.ToggleLike(post.id, post.isLiked)) }
                                    )
                                }
                            }
                        }
                    }
                }
                is FeedUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.titleMedium)
            Text(text = post.content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "By ${post.author}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Button(onClick = onLikeClick) {
                    Text(if (post.isLiked) "Liked ❤️" else "Like 🤍")
                }
            }
        }
    }
}
