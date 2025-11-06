package com.events.app.ui.viewmodels

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@Composable
fun EventsScreen(viewModel: EventsViewModel = hiltViewModel()) {
    val events = viewModel.events.collectAsState().value

    LazyColumn {  // Один блок — вертикальный список без стилей
        items(events) { event ->
            Text(
                text = "ID: ${event.id}\n" +
                        "Название: ${event.title}\n" +
                        "Описание: ${event.description}\n" +
                        "Дата: ${event.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}\n" +
                        "Завершено: ${if (event.isFinished) "Да" else "Нет"}"
            )
        }
    }
}