package com.events.app.ui.views.eventdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    viewModel.loadEvent(eventId)
    val event by viewModel.event.collectAsState()

    event?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start // Выравнивание по левому краю
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE0E0E0))
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок
            Text(
                text = it.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Дата
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Дата: ")
                    }
                    append("${it.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))} - ${it.endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Статус
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Статус: ")
                    }
                    append(if (it.isFinished) "Завершено" else "Предстоящее")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Формат
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Формат: ")
                    }
                    append(it.format)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Место
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Мест: ")
                    }
                    append(it.places.toString())
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Локация
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Локация: ")
                    }
                    append(it.location)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Описание
            Text(
                text = it.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Записаться"
            Button(
                onClick = { /* Логика записи */ },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Центрируем кнопку
            ) {
                Text("Записаться")
            }
        }
    } ?: Text("Событие не найдено", color = Color.Black)
}