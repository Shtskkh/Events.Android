package com.events.app.ui.views.eventdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import coil.compose.AsyncImage
import com.events.app.domain.models.events.Event
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    viewModel.loadEvent(eventId)
    val event by viewModel.event.collectAsState()

    event?.let { EventContent(it) } ?: Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EventContent(event: Event) {
    val isOnline = event.format.contains("онлайн", ignoreCase = true) ||
            event.format.contains("online", ignoreCase = true)
    val formatColor = if (isOnline) Color(0xFF22C55E) else Color(0xFFEF4444)
    val formatIcon = if (isOnline) Icons.Outlined.Videocam else Icons.Outlined.LocationOn

    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("ru"))

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Hero-изображение — полностью видно, лёгкий градиент только в самом низу ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = event.previewUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Тонкий градиент — только последние 25% высоты
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Бейдж «Завершено» если нужен
            if (event.isFinished) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = "Завершено",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // ── Контент ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {

            // Название
            Text(
                text = event.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 30.sp
            )

            Spacer(Modifier.height(16.dp))

            // ── Секция: Дата и время (компактная) ─────────────
            SectionTitle("Дата и время")
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactInfoCard(
                    icon = Icons.Outlined.PlayArrow,
                    label = "Начало",
                    value = event.startDate.format(dateFormatter),
                    modifier = Modifier.weight(1f)
                )
                CompactInfoCard(
                    icon = Icons.Outlined.Stop,
                    label = "Конец",
                    value = event.endDate.format(dateFormatter),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Секция: Информация о мероприятии ──────────────
            SectionTitle("О мероприятии")
            Spacer(Modifier.height(8.dp))

            // Тип
            if (event.type.isNotBlank()) {
                InfoRow(
                    icon = Icons.Outlined.Category,
                    label = "Тип",
                    value = event.type
                )
                Spacer(Modifier.height(6.dp))
            }

            // Формат (онлайн/офлайн) — теперь в инфо-секции
            if (event.format.isNotBlank()) {
                InfoRow(
                    icon = formatIcon,
                    label = "Формат",
                    value = event.format,
                    valueColor = formatColor
                )
                Spacer(Modifier.height(6.dp))
            }

            // Локация
            if (event.location.isNotBlank()) {
                InfoRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Место проведения",
                    value = event.location
                )
                Spacer(Modifier.height(6.dp))
            }

            // Количество мест
            if (event.places > 0) {
                InfoRow(
                    icon = Icons.Outlined.Group,
                    label = "Количество мест",
                    value = event.places.toString()
                )
                Spacer(Modifier.height(6.dp))
            }

            // Нужна регистрация
            InfoRow(
                icon = if (event.isPublic) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                label = "Доступ",
                value = if (event.isPublic) "Открытое" else "Закрытое"
            )
            Spacer(Modifier.height(6.dp))

            // Статус
            InfoRow(
                icon = if (event.isFinished) Icons.Outlined.EventBusy else Icons.Outlined.Event,
                label = "Статус",
                value = if (event.isFinished) "Завершено" else "Предстоящее",
                valueColor = if (event.isFinished)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    Color(0xFF22C55E)
            )

            // Ссылка
            if (!event.link.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                InfoRow(
                    icon = Icons.Outlined.Link,
                    label = "Ссылка",
                    value = event.link
                )
            }

            // ── Описание ───────────────────────────────────────
            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("Описание")
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = event.description,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Анонс ──────────────────────────────────────────
            if (event.announcement.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("Анонс")
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = event.announcement,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Кнопка ─────────────────────────────────────────
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !event.isFinished,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (event.isFinished) Icons.Outlined.EventBusy
                    else Icons.Outlined.HowToReg,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (event.isFinished) "Мероприятие завершено" else "Записаться",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// Компактная карточка для дат — две в ряд, небольшая
@Composable
private fun CompactInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

// Строка с иконкой — для остальных полей
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (valueColor == Color.Unspecified)
                        MaterialTheme.colorScheme.onSurface else valueColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}