package com.events.app.ui.components.eventscards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.events.app.domain.models.events.Event
import java.time.format.DateTimeFormatter

@Composable
fun UpcomingEventCard(
    event: Event,
    onClick: () -> Unit = {}
) {
    val isOnline = event.format.contains("онлайн", ignoreCase = true) ||
            event.format.contains("online", ignoreCase = true)

    val formatIcon: ImageVector = if (isOnline) Icons.Outlined.Videocam else Icons.Outlined.LocationOn
    val formatColor = if (isOnline) Color(0xFF3B82F6) else Color(0xFF10B981)
    val formatBg   = formatColor.copy(alpha = 0.12f)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),      // ← все 4 угла скруглены
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Фото ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                AsyncImage(
                    model = event.previewUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.2f)),
                                startY = 100f
                            )
                        )
                )
            }

            // ── Текст ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
            ) {
                // Название
                Text(
                    text = event.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Анонс
                if (event.announcement.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = event.announcement,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Нижняя строка: дата · время · тип · формат ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Дата
                    InfoChip(
                        icon = Icons.Outlined.CalendarMonth,
                        text = event.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        iconTint = MaterialTheme.colorScheme.primary,
                        bgColor = MaterialTheme.colorScheme.primary.copy(0.10f),
                        textColor = MaterialTheme.colorScheme.primary
                    )

                    // Время
                    InfoChip(
                        icon = Icons.Outlined.Schedule,
                        text = event.startDate.format(DateTimeFormatter.ofPattern("HH:mm")),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        bgColor = MaterialTheme.colorScheme.secondary.copy(0.10f),
                        textColor = MaterialTheme.colorScheme.secondary
                    )

                    // Тип мероприятия
                    if (event.type.isNotBlank()) {
                        InfoChip(
                            icon = Icons.Outlined.Category,
                            text = event.type,
                            iconTint = Color(0xFFF59E0B),
                            bgColor = Color(0xFFF59E0B).copy(0.12f),
                            textColor = Color(0xFFF59E0B)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Формат (Онлайн / Офлайн)
                    if (event.format.isNotBlank()) {
                        InfoChip(
                            icon = formatIcon,
                            text = event.format,
                            iconTint = formatColor,
                            bgColor = formatBg,
                            textColor = formatColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    bgColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}