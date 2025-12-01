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
fun AssignedEventCard(
    event: Event?,
    onCardClick: () -> Unit = {}
) {
    if (event == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCardClick() },  // Клик на всей карточке открывает детали
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),  // Чтобы Box мог заполнить высоту
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Белая область: заполняет всю правую часть (30%), растянута по высоте, с обводкой вокруг карточки
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))  // Закругляем правые углы под карточку
                    .background(color = Color.White)
            )
        }
    }
}