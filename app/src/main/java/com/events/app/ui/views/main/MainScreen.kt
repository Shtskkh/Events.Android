package com.events.app.ui.views.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.events.app.ui.components.eventscards.UpcomingEventCard

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit = {},
    onViewAllClick: () -> Unit = {}
) {
    val upcomingEvents   = viewModel.events.collectAsState().value
    val completedEvents  = viewModel.completedEvents.collectAsState().value
    val completedLoading = viewModel.completedLoading.collectAsState().value
    val myEvents         = viewModel.myEvents.collectAsState().value
    val myEventsLoading  = viewModel.myEventsLoading.collectAsState().value
    val isLoading        = viewModel.isLoading.collectAsState().value
    val error            = viewModel.error.collectAsState().value
    val recentEvents     = viewModel.recentEvents.collectAsState().value
    val recentLoading    = viewModel.recentLoading.collectAsState().value

    // Открыть карточку + записать просмотр локально
    val openEvent: (String) -> Unit = { id ->
        viewModel.onEventViewed(id)
        onEventClick(id)
    }

    when {
        isLoading && upcomingEvents.isEmpty() && recentEvents.isEmpty() -> {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        error != null && upcomingEvents.isEmpty() -> {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) { Text(error, color = MaterialTheme.colorScheme.error) }
        }
        else -> {
            LazyColumn(
                modifier       = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // ── Недавно просмотренные ──────────────────────────
                if (recentEvents.isNotEmpty() || recentLoading) {
                    item {
                        SectionLabel(
                            title       = "Недавно просмотренные",
                            icon        = Icons.Outlined.History,
                            accentColor = Color(0xFF0EA5E9),
                            showAll     = false,
                            onViewAll   = {}
                        )
                    }
                    if (recentLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    } else {
                        items(recentEvents.take(3)) { event ->
                            UpcomingEventCard(event = event, onClick = { openEvent(event.id) })
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    item { Spacer(Modifier.height(4.dp)) }
                }

                // ── Созданные вами ─────────────────────────────────
                item {
                    SectionLabel(
                        title       = "Созданные вами",
                        icon        = Icons.Outlined.Edit,
                        accentColor = MaterialTheme.colorScheme.primary,
                        showAll     = myEvents.size > 3,
                        onViewAll   = onViewAllClick
                    )
                }
                when {
                    myEventsLoading -> item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    myEvents.isEmpty() -> item { EmptySection("Вы ещё не создали ни одного мероприятия") }
                    else -> items(myEvents.take(3)) { event ->
                        UpcomingEventCard(event = event, onClick = { openEvent(event.id) })
                        Spacer(Modifier.height(16.dp))
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }

                // ── Ближайшие ──────────────────────────────────────
                item {
                    SectionLabel(
                        title       = "Ближайшие",
                        icon        = Icons.Outlined.Schedule,
                        accentColor = Color(0xFF3B82F6),
                        showAll     = upcomingEvents.size > 3,
                        onViewAll   = onViewAllClick
                    )
                }
                if (upcomingEvents.isEmpty() && !isLoading) {
                    item { EmptySection("Нет предстоящих мероприятий") }
                } else {
                    items(upcomingEvents.take(3)) { event ->
                        UpcomingEventCard(event = event, onClick = { openEvent(event.id) })
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── Завершённые ────────────────────────────────────
                if (completedEvents.isNotEmpty() || completedLoading) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionLabel(
                            title       = "Завершённые",
                            icon        = Icons.Outlined.CheckCircle,
                            accentColor = Color(0xFF9E9E9E),
                            showAll     = completedEvents.size > 3,
                            onViewAll   = onViewAllClick
                        )
                    }
                    if (completedLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    } else {
                        items(completedEvents.take(3)) { event ->
                            UpcomingEventCard(event = event, onClick = { openEvent(event.id) })
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(
    title: String, icon: ImageVector, accentColor: Color,
    showAll: Boolean, onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp, top = 28.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = accentColor.copy(alpha = 0.13f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge,
                letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun EmptySection(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Event, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.55f))
        }
    }
}