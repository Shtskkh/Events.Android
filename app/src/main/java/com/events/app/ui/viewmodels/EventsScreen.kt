package com.events.app.ui.viewmodels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.events.app.ui.components.AssignedEventCard

@Composable
fun EventsScreen(
    innerPadding: PaddingValues,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val events = viewModel.events.collectAsState().value

    // Берём самое первое предстоящее мероприятие (по дате, как в базе)
    val assignedEvent = events
        .filter { !it.isFinished }
        .minByOrNull { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding
    ) {
        item {
            Text(
                text = "Назначенные вам",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        // Карточка мероприятия кликабельная
        item {
            AssignedEventCard(
                event = assignedEvent,
                onCardClick = { /* TODO: переход к деталям мероприятия */ }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { /* TODO: переход ко всем назначенным */ },
                    modifier = Modifier.padding(end = 16.dp),  // Небольшой отступ от правого края
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Просмотреть все")
                }
            }
        }

        // Обычный список всех мероприятий
        items(events) { event ->
            Text(
                text = "ID: ${event.id}\n" +
                        "Название: ${event.title}\n" +
                        "Описание: ${event.description}\n" +
                        "Дата: ${event.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}\n" +
                        "Завершено: ${if (event.isFinished) "Да" else "Нет"}",
                modifier = Modifier.padding(16.dp)
            )
            Divider()
        }
    }
}