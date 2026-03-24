package com.events.app.ui.views.eventdetail

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.events.app.domain.models.events.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val GreenStart = Color(0xFF22C55E)
private val RedEnd     = Color(0xFFEF4444)

@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null,
    onEdit: ((String) -> Unit)? = null
) {
    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    val event         by viewModel.event.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()
    val error         by viewModel.error.collectAsState()
    val isAdmin       by viewModel.isAdmin.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val isDeleting    by viewModel.isDeleting.collectAsState()

    LaunchedEffect(deleteSuccess) { if (deleteSuccess) onBack?.invoke() }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.ErrorOutline, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
            }
        }
        event != null -> EventContent(
            event      = event!!,
            isAdmin    = isAdmin,
            isDeleting = isDeleting,
            onDelete   = { viewModel.deleteEvent(event!!.id) },
            onEdit     = { onEdit?.invoke(event!!.id) }
        )
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun EventContent(
    event: Event,
    isAdmin: Boolean,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isOnline = event.format.contains("онлайн", ignoreCase = true) ||
            event.format.contains("online", ignoreCase = true)
    val formatColor = if (isOnline) GreenStart else RedEnd
    val formatIcon  = if (isOnline) Icons.Outlined.Videocam else Icons.Outlined.LocationOn

    val dateFormatter    = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("ru"))
    val scrollState      = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context          = LocalContext.current
    val scope            = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var titleCopied      by remember { mutableStateOf(false) }

    val titleBg by animateColorAsState(
        targetValue = if (titleCopied) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        else Color.Transparent,
        label = "titleCopyBg"
    )

    fun copyText(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon  = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Удалить мероприятие?", fontWeight = FontWeight.Bold) },
            text  = {
                Text("«${event.title}» будет удалено без возможности восстановления.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Удалить", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Превью ────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            AsyncImage(
                model = event.previewUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(110.dp).align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                        )
                    )
            )

            // Бейдж "Завершено"
            if (event.isFinished) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        "Завершено",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White
                    )
                }
            }

            // Кнопки для admin
            if (isAdmin) {
                // Кнопка редактирования — слева сверху (если нет бейджа "Завершено")
                if (!event.isFinished) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    ) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                            Icon(
                                Icons.Outlined.Edit, "Редактировать",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Кнопка удаления — справа сверху
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
                ) {
                    IconButton(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !isDeleting,
                        modifier = Modifier.size(44.dp)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Icon(
                                Icons.Outlined.DeleteOutline, "Удалить",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))

            // ── Заголовок — кликабельный ──────────────────────────
            Surface(modifier = Modifier.fillMaxWidth(), color = titleBg, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            copyText(event.title, "Название")
                            scope.launch { titleCopied = true; delay(300); titleCopied = false }
                        }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = event.title,
                        fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground, lineHeight = 30.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (titleCopied) Icons.Outlined.CheckCircle
                        else Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint = if (titleCopied) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp).padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Дата и время ──────────────────────────────────────
            SectionTitle("Дата и время")
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactInfoCard(
                    icon     = Icons.Outlined.PlayArrow,
                    iconTint = GreenStart,
                    label    = "Начало",
                    value    = event.startDate.format(dateFormatter),
                    modifier = Modifier.weight(1f)
                )
                CompactInfoCard(
                    icon     = Icons.Outlined.Stop,
                    iconTint = RedEnd,
                    label    = "Конец",
                    value    = event.endDate.format(dateFormatter),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── О мероприятии ─────────────────────────────────────
            SectionTitle("О мероприятии")
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    if (isAdmin) {
                        CopyableDetailRow(Icons.Outlined.Key, "ID мероприятия", event.id,
                            onCopy = { copyText(event.id, "ID") })
                        DetailDivider()
                    }
                    if (event.type.isNotBlank()) {
                        DetailRow(Icons.Outlined.Category, "Тип", event.type)
                        DetailDivider()
                    }
                    if (event.format.isNotBlank()) {
                        DetailRow(formatIcon, "Формат", event.format, formatColor)
                        DetailDivider()
                    }
                    if (event.location.isNotBlank()) {
                        DetailRow(Icons.Outlined.LocationOn, "Локация", event.location)
                        DetailDivider()
                    }
                    if (!event.placeNumber.isNullOrBlank()) {
                        DetailRow(Icons.Outlined.MeetingRoom, "Помещение №", event.placeNumber)
                        DetailDivider()
                    }
                    DetailRow(
                        icon       = if (event.needsRegistration) Icons.Outlined.HowToReg else Icons.Outlined.PersonOff,
                        label      = "Регистрация",
                        value      = if (event.needsRegistration) "Требуется" else "Не требуется",
                        valueColor = if (event.needsRegistration) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (event.needsRegistration && event.maxParticipants != null) {
                        DetailDivider()
                        DetailRow(Icons.Outlined.Group, "Макс. участников", event.maxParticipants.toString())
                    }

                    // ── Аналитика — только для admin ───────────────
                    if (isAdmin && event.participantsCount != null) {
                        DetailDivider()
                        DetailRow(
                            icon       = Icons.Outlined.Group,
                            label      = "Записалось",
                            value      = event.participantsCount.toString() +
                                    (event.maxParticipants?.let { " / $it" } ?: ""),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (isAdmin && event.viewsCount != null) {
                        DetailDivider()
                        DetailRow(
                            icon  = Icons.Outlined.Visibility,
                            label = "Просмотры",
                            value = event.viewsCount.toString()
                        )
                    }

                    DetailDivider()
                    DetailRow(
                        icon  = if (event.isPublic) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                        label = "Доступ",
                        value = if (event.isPublic) "Открытое" else "Закрытое"
                    )
                    DetailDivider()
                    DetailRow(
                        icon       = if (event.isFinished) Icons.Outlined.EventBusy else Icons.Outlined.Event,
                        label      = "Статус",
                        value      = if (event.isFinished) "Завершено" else "Предстоящее",
                        valueColor = if (event.isFinished) MaterialTheme.colorScheme.onSurfaceVariant
                        else GreenStart
                    )
                    if (!event.organizerName.isNullOrBlank()) {
                        DetailDivider()
                        DetailRow(Icons.Outlined.Person, "Организатор", event.organizerName)
                    }
                    if (!event.link.isNullOrBlank()) {
                        DetailDivider()
                        DetailRow(Icons.Outlined.Link, "Ссылка", event.link)
                    }
                }
            }

            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("Описание")
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        event.description, modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp, lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (event.announcement.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("Анонс")
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        event.announcement, modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp, lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            if (!isAdmin) {
                Button(
                    onClick  = { },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    enabled  = !event.isFinished,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        if (event.isFinished) Icons.Outlined.EventBusy else Icons.Outlined.HowToReg,
                        null, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (event.isFinished) "Мероприятие завершено" else "Записаться",
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Оригинальная карточка даты с параметром цвета иконки ──────────
@Composable
private fun CompactInfoCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconTint,
                    modifier           = Modifier.size(13.dp)
                )
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp
            )
        }
    }
}

// ── Кликабельная строка с копированием ───────────────────────────
@Composable
private fun CopyableDetailRow(
    icon: ImageVector, label: String, value: String, onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }
    val scope  = rememberCoroutineScope()
    val bgColor by animateColorAsState(
        targetValue = if (copied) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        else Color.Transparent, label = "copyBg"
    )
    Surface(modifier = Modifier.fillMaxWidth(), color = bgColor, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCopy(); scope.launch { copied = true; delay(300); copied = false } }
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (copied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
                contentDescription = null,
                tint = if (copied) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

// ── Обычная строка ────────────────────────────────────────────────
@Composable
private fun DetailRow(
    icon: ImageVector, label: String, value: String, valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}