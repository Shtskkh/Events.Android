package com.events.app.ui.components.eventscards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MeetingRoom
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
    val isOnline    = event.format.contains("онлайн", ignoreCase = true) ||
            event.format.contains("online", ignoreCase = true)
    val formatIcon  = if (isOnline) Icons.Outlined.Videocam else Icons.Outlined.LocationOn
    val formatColor = if (isOnline) Color(0xFF22C55E) else Color(0xFFEF4444)

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Фото (без бейджа формата на картинке) ─────────────
            Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                AsyncImage(
                    model            = event.previewUrl,
                    contentDescription = null,
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
                // Лёгкий градиент снизу
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.22f)),
                                startY = 80f
                            )
                        )
                )
            }

            // ── Название и анонс ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
            ) {
                Text(
                    text          = event.title,
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 18.sp,
                    letterSpacing = (-0.3).sp,
                    maxLines      = 1,
                    overflow      = TextOverflow.Ellipsis,
                    color         = MaterialTheme.colorScheme.onSurface
                )
                if (event.announcement.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = event.announcement,
                        fontSize  = 13.sp,
                        lineHeight = 18.sp,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines  = 2,
                        overflow  = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Строка 1: Дата / Время / Формат ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                InfoChip(
                    icon     = Icons.Outlined.CalendarMonth,
                    value    = event.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    icon     = Icons.Outlined.Schedule,
                    value    = event.startDate.format(DateTimeFormatter.ofPattern("HH:mm")),
                    modifier = Modifier.weight(1f)
                )
                if (event.format.isNotBlank()) {
                    InfoChip(
                        icon      = formatIcon,
                        value     = event.format,
                        tint      = formatColor,
                        bgAlpha   = 0.12f,
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            // ── Строка 2: Тип / Локация / Помещение ───────────────
            val hasLocation = event.location.isNotBlank()
            val hasPlace    = !event.placeNumber.isNullOrBlank()
            val hasType     = event.type.isNotBlank()

            if (hasType || hasLocation || hasPlace) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (hasType) {
                        InfoChip(
                            icon     = Icons.Outlined.Category,
                            value    = event.type,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (hasLocation) {
                        InfoChip(
                            icon     = Icons.Outlined.LocationOn,
                            value    = event.location,
                            tint     = MaterialTheme.colorScheme.tertiary,
                            bgAlpha  = 0.12f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (hasPlace) {
                        InfoChip(
                            icon     = Icons.Outlined.MeetingRoom,
                            value    = "№${event.placeNumber}",
                            tint     = MaterialTheme.colorScheme.secondary,
                            bgAlpha  = 0.12f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    value: String,
    tint: Color = Color.Unspecified,
    bgAlpha: Float = 0.15f,
    modifier: Modifier = Modifier
) {
    val resolvedTint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.primary else tint
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = resolvedTint.copy(alpha = bgAlpha),
        modifier = modifier
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = resolvedTint,
                modifier           = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text      = value,
                fontSize  = 12.sp,
                fontWeight = FontWeight.Bold,
                color     = resolvedTint,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}