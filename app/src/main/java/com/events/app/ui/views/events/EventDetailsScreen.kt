package com.events.app.ui.views.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailsScreen(
    eventId: Int,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()

    if (event == null) {
        Text("Событие не найдено")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Растянутый светло-серый прямоугольник
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок жирным черным
        Text(
            text = event!!.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Дата с жирным лейблом
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val dateText = "Дата: ${event!!.date.format(formatter)}"
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Дата: ")
                }
                append(dateText.substringAfter("Дата: "))
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Статус с жирным лейблом
        val statusText = "Статус: ${if (event!!.isFinished) "Завершено" else "Предстоящее"}"
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Статус: ")
                }
                append(statusText.substringAfter("Статус: "))
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Формат с жирным лейблом
        val formatText = "Формат: Не указано"
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Формат: ")
                }
                append(formatText.substringAfter("Формат: "))
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Место с жирным лейблом
        val placeText = "Место: Не указано"
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Место: ")
                }
                append(placeText.substringAfter("Место: "))
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Локация с жирным лейблом
        val locationText = "Локация: Не указано"
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Локация: ")
                }
                append(locationText.substringAfter("Локация: "))
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Описание обычным
        Text(
            text = event!!.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка с увеличенным отступом снизу
        Button(
            onClick = { /* TODO: Логика регистрации */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ) {
            Text("Записаться")
        }
    }
}