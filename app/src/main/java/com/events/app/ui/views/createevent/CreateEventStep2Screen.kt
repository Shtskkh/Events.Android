package com.events.app.ui.views.createevent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CreateEventStep2Screen() {
    // Локальное состояние для полей (не сохраняется)
    var address by remember { mutableStateOf("") }
    var onlineLink by remember { mutableStateOf("") }

    // Scroll для экрана
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Бронируемая аудитория / адрес") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите адрес, например: улица Пушкина, дом Колотушкина") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Поле для ссылки на созвон
        TextField(
            value = onlineLink,
            onValueChange = { onlineLink = it },
            label = { Text("Ссылка на созвон") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите ссылку, например: https://zoom.us/meeting") }
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка "Создать мероприятие"
        Button(
            onClick = { /* TODO: Сохранение события позже */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Создать мероприятие", color = Color.White)
        }
    }
}