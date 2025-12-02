package com.events.app.ui.views.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.ui.components.eventscards.UpcomingEventCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (Int) -> Unit,
) {
    val displayedEvents by viewModel.displayedEvents.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val listState = rememberLazyListState()

    val isCloseToEnd by remember { derivedStateOf { listState.isCloseToEnd() } }

    LaunchedEffect(isCloseToEnd) {
        if (isCloseToEnd && hasMore) {
            viewModel.loadMore()
        }
    }

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
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        }
    }

}

private fun LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false