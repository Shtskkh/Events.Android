package com.events.app.ui.views.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.ui.components.eventscards.UpcomingEventCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit,
) {
    val displayedEvents by viewModel.displayedEvents.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isCloseToEnd by remember { derivedStateOf { listState.isCloseToEnd() } }
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }

    LaunchedEffect(isCloseToEnd) {
        if (isCloseToEnd && hasMore) {
            viewModel.loadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Показываем индикатор загрузки при первом запуске
        if (isLoading && displayedEvents.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // Показываем ошибку
        else if (error != null && displayedEvents.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: "Неизвестная ошибка",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        // Показываем пустой список
        else if (displayedEvents.isEmpty() && !isLoading) {
            Text(
                text = "Мероприятий пока нет",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Показываем список
        else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp),
            ) {
                items(displayedEvents) { event ->
                    UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (hasMore) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        if (showScrollToTop) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 15.dp, bottom = 80.dp)
                    .alpha(0.7f),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Вверх"
                )
            }
        }
    }
}

private fun LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false