package com.events.app.ui.views.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    val events = viewModel.events.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val error = viewModel.error.collectAsState().value

    val myEvents = events.filter { !it.isFinished }.sortedBy { it.startDate }
    val upcomingEvents = events.filter { !it.isFinished }.sortedBy { it.startDate }
    val completedEvents = events.filter { it.isFinished }.sortedByDescending { it.startDate }

    when {
        isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null && events.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {

                // ── Созданные вами ─────────────────────────────
                item {
                    SectionLabel(
                        title = "Созданные вами",
                        icon = Icons.Outlined.Edit,
                        accentColor = MaterialTheme.colorScheme.primary,
                        showAll = myEvents.size > 3,
                        onViewAll = onViewAllClick
                    )
                }
                if (myEvents.isEmpty()) {
                    item { EmptySection("Вы ещё не создали ни одного мероприятия") }
                } else {
                    items(myEvents.take(3)) { event ->
                        UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                // ── Ближайшие ──────────────────────────────────
                item {
                    SectionLabel(
                        title = "Ближайшие",
                        icon = Icons.Outlined.Schedule,
                        accentColor = Color(0xFF3B82F6),
                        showAll = upcomingEvents.size > 3,
                        onViewAll = onViewAllClick
                    )
                }
                if (upcomingEvents.isEmpty()) {
                    item { EmptySection("Нет предстоящих мероприятий") }
                } else {
                    items(upcomingEvents.take(3)) { event ->
                        UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── Завершённые ────────────────────────────────
                if (completedEvents.isNotEmpty()) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionLabel(
                            title = "Завершённые",
                            icon = Icons.Outlined.CheckCircle,
                            accentColor = Color(0xFF9E9E9E),
                            showAll = completedEvents.size > 3,
                            onViewAll = onViewAllClick
                        )
                    }
                    items(completedEvents.take(3)) { event ->
                        UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ── Заголовок секции с иконкой и цветом ───────────────────────────
@Composable
private fun SectionLabel(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    showAll: Boolean,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp, top = 28.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Иконка в цветном кружке
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.13f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-0.4).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (showAll) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onViewAll),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Все",
                    fontSize = 14.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Пустая секция ─────────────────────────────────────────────────
@Composable
private fun EmptySection(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Event,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.55f)
            )
        }
    }
}