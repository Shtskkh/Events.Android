package com.events.app.ui.views.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.events.app.domain.models.events.Event
import com.events.app.ui.components.eventscards.AssignedEventCard
import com.events.app.ui.components.eventscards.UpcomingEventCard

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val events = viewModel.events.collectAsState().value

    // Предстоящее назначенное (одно)
    val assignedEvent = events
        .filter { !it.isFinished }
        .minByOrNull { it.date }

    // Ближайшие предстоящие
    val upcomingEvents = events
        .filter { !it.isFinished }
        .sortedBy { it.date }

    // Завершённые (descending для недавних сверху)
    val completedEvents = events
        .filter { it.isFinished }
        .sortedByDescending { it.date }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Блок "Назначенные вам" (использует AssignedEventCard, но только одну)
        item {
            EventSection(
                title = "Назначенные вам",
                events = listOfNotNull(assignedEvent),
                cardComposable = { event -> AssignedEventCard(event = event, onCardClick = {}) },
                onViewAllClick = { /* TODO: переход ко всем назначенным */ }
            )
        }

        // Блок "Ближайшие"
        item {
            EventSection(
                title = "Ближайшие",
                events = upcomingEvents.take(5),
                cardComposable = { event -> UpcomingEventCard(event = event) },
                onViewAllClick = { /* TODO: переход ко всем предстоящим */ }
            )
        }

        // Блок "Завершённые"
        item {
            EventSection(
                title = "Завершённые",
                events = completedEvents.take(5),
                cardComposable = { event -> AssignedEventCard(event = event, onCardClick = {}) },
                onViewAllClick = { /* TODO: переход ко всем завершённым */ }
            )
        }
    }
}

@Composable
private fun EventSection(
    title: String,
    events: List<Event>,
    cardComposable: @Composable (Event) -> Unit,
    onViewAllClick: () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )

    events.forEach { event ->
        cardComposable(event)
        Spacer(modifier = Modifier.height(8.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = onViewAllClick,
            modifier = Modifier.padding(end = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text("Просмотреть все")
        }
    }
}