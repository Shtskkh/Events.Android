package com.events.app.ui.components.eventscards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.events.app.domain.models.events.Event
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventCard(
    event: Event
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { /* Заглушка: клик по карточке, не ведёт никуда */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Меньше elevation для плоского вида как на скрине
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Белый квадрат растянут до краёв сверху (как баннер для будущей картинки)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)  // Удвоенная высота для пропорций как на макете
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )  // Закругление только сверху, плотно к краям карточки
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Тексты с отступами по бокам
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),  // Отступы слева/справа для текста как на макете
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,  // Как на макете, обрезка для компактности
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Row для даты слева и кнопки справа (выравнивание по краям как на макете)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Button(
                        onClick = { /* Заглушка: клик по кнопке, не ведёт никуда */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50) // Круглые углы как на скрине
                    ) {
                        Text("Открыть")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Нижний отступ внутри карточки
        }
    }
}