package com.events.app.ui.views.createevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CreateEventScreen(onNext: () -> Unit = {}) {
    // Локальное состояние для полей (не сохраняется в базу)
    var title by remember { mutableStateOf("") }
    var announcement by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Поле для названия
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название мероприятия") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите название") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Поле для анонса
        TextField(
            value = announcement,
            onValueChange = { announcement = it },
            label = { Text("Анонс (краткое описание)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Краткий анонс для привлечения внимания") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Полное описание") },
            maxLines = 10,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Подробное описание мероприятия") }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)  // Светло-серый фон, как у полей ввода
                .clip(RoundedCornerShape(8.dp)),  // Закруглённые углы
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Здесь будет картинка (нажмите для загрузки)",
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка "Продолжить"
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Продолжить", color = Color.White)
        }
    }
}