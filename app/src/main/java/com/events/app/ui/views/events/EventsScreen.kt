package com.events.app.ui.views.events

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.ui.components.appbar.EventsTopBar
import com.events.app.ui.components.eventscards.UpcomingEventCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    drawerState: DrawerState,
    onNavigationToAccount: () -> Unit,
    onEventClick: (Int) -> Unit
) {
    val displayedEvents by viewModel.displayedEvents.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

    val isCloseToEnd by remember { derivedStateOf { listState.isCloseToEnd() } }

    LaunchedEffect(isCloseToEnd) {
        if (isCloseToEnd && hasMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EventsTopBar(
                title = "Мероприятия",
                scrollBehavior = scrollBehavior,
                drawerState = drawerState,
                onNavigationToAccount = onNavigationToAccount
            ) {
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
}

// Расширение для проверки приближения к концу списка
private fun LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false

// Расширение для проверки конца списка (оставлено для совместимости, если нужно)
private fun LazyListState.isScrolledToEnd() =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1