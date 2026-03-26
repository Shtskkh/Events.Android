package com.events.app.ui.views.eventdetail

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.events.app.data.remote.dto.ParticipantDto
import com.events.app.domain.models.events.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val GreenStart = Color(0xFF22C55E)
private val RedEnd     = Color(0xFFEF4444)

// ═══════════════════════════════════════════════════════════════════
// EventDetailsScreen
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null,
    onEdit: ((String) -> Unit)? = null
) {
    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    val event               by viewModel.event.collectAsState()
    val isLoading           by viewModel.isLoading.collectAsState()
    val error               by viewModel.error.collectAsState()
    val isAdmin             by viewModel.isAdmin.collectAsState()
    val canRegister         by viewModel.canRegister.collectAsState()
    val deleteSuccess       by viewModel.deleteSuccess.collectAsState()
    val isDeleting          by viewModel.isDeleting.collectAsState()
    val participants        by viewModel.participants.collectAsState()
    val participantsLoading by viewModel.participantsLoading.collectAsState()
    val isRegistered        by viewModel.isRegistered.collectAsState()
    val registrationLoading by viewModel.registrationLoading.collectAsState()
    val registrationError   by viewModel.registrationError.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(deleteSuccess) { if (deleteSuccess) onBack?.invoke() }

    LaunchedEffect(registrationError) {
        registrationError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearRegistrationError()
        }
    }

    var showParticipantsSheet by remember { mutableStateOf(false) }

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
            event               = event!!,
            isAdmin             = isAdmin,
            canRegister         = canRegister,
            isDeleting          = isDeleting,
            isRegistered        = isRegistered,
            registrationLoading = registrationLoading,
            participants        = participants,
            onDelete            = { viewModel.deleteEvent(event!!.id) },
            onEdit              = { onEdit?.invoke(event!!.id) },
            onRegister          = { viewModel.toggleRegistration() },
            onShowParticipants  = { showParticipantsSheet = true }
        )
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // ── Шторка участников ─────────────────────────────────────────
    if (showParticipantsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showParticipantsSheet = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            ParticipantsSheetContent(
                participants = participants,
                isLoading    = participantsLoading,
                maxParticipants = event?.maxParticipants
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// EventContent — основной контент карточки
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EventContent(
    event: Event,
    isAdmin: Boolean,
    canRegister: Boolean,
    isDeleting: Boolean,
    isRegistered: Boolean,
    registrationLoading: Boolean,
    participants: List<ParticipantDto>,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onRegister: () -> Unit,
    onShowParticipants: () -> Unit
) {
    val isOnline    = event.format.contains("онлайн", ignoreCase = true) ||
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
        else Color.Transparent, label = "titleCopyBg"
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
                Button(onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape  = RoundedCornerShape(10.dp)) {
                    Text("Удалить", fontWeight = FontWeight.SemiBold)
                }
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
            AsyncImage(model = event.previewUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier
                .fillMaxWidth().height(110.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.background))))

            if (event.isFinished) {
                Surface(modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                    shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.55f)) {
                    Text("Завершено",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            if (isAdmin) {
                // Кнопка редактирования — слева (если не завершено)
                if (!event.isFinished) {
                    Surface(modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Outlined.Edit, "Редактировать",
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                // Кнопка удаления — справа
                Surface(modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)) {
                    IconButton(onClick = { showDeleteDialog = true }, enabled = !isDeleting,
                        modifier = Modifier.size(44.dp)) {
                        if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.error)
                        else Icon(Icons.Outlined.DeleteOutline, "Удалить",
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))

            // ── Заголовок — кликабельный ──────────────────────────
            Surface(modifier = Modifier.fillMaxWidth(), color = titleBg,
                shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            copyText(event.title, "Название")
                            scope.launch { titleCopied = true; delay(300); titleCopied = false }
                        }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(event.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground, lineHeight = 30.sp,
                        modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (titleCopied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
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
                CompactInfoCard(Icons.Outlined.PlayArrow, GreenStart, "Начало",
                    event.startDate.format(dateFormatter), Modifier.weight(1f))
                CompactInfoCard(Icons.Outlined.Stop, RedEnd, "Конец",
                    event.endDate.format(dateFormatter), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // ── О мероприятии ─────────────────────────────────────
            SectionTitle("О мероприятии")
            Spacer(Modifier.height(8.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                Column(modifier = Modifier.padding(4.dp)) {

                    // ID мероприятия — только admin
                    if (isAdmin) {
                        CopyableDetailRow(Icons.Outlined.Key, "ID мероприятия", event.id,
                            onCopy = { copyText(event.id, "ID мероприятия") })
                        DetailDivider()
                    }

                    // ID создателя — только admin
                    if (isAdmin && !event.userId.isNullOrBlank()) {
                        CopyableDetailRow(Icons.Outlined.PersonSearch, "ID создателя", event.userId,
                            onCopy = { copyText(event.userId, "ID создателя") })
                        DetailDivider()
                    }

                    if (event.type.isNotBlank()) {
                        DetailRow(Icons.Outlined.Category, "Тип", event.type); DetailDivider()
                    }
                    if (event.format.isNotBlank()) {
                        DetailRow(formatIcon, "Формат", event.format, formatColor); DetailDivider()
                    }
                    if (event.location.isNotBlank()) {
                        DetailRow(Icons.Outlined.LocationOn, "Локация", event.location); DetailDivider()
                    }
                    if (!event.placeNumber.isNullOrBlank()) {
                        DetailRow(Icons.Outlined.MeetingRoom, "Помещение №", event.placeNumber); DetailDivider()
                    }

                    // Регистрация
                    DetailRow(
                        icon       = if (event.needsRegistration) Icons.Outlined.HowToReg else Icons.Outlined.PersonOff,
                        label      = "Регистрация",
                        value      = if (event.needsRegistration) "Требуется" else "Не требуется",
                        valueColor = if (event.needsRegistration) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Участники / макс. — показываем всегда когда maxParticipants != null
                    if (event.maxParticipants != null) {
                        DetailDivider()
                        // Берём из аналитики или из загруженного списка участников
                        val registered = event.participantsCount
                            ?: participants.size.takeIf { participants.isNotEmpty() }
                        val isFull = registered != null && registered >= event.maxParticipants
                        val isAlmostFull = registered != null &&
                                registered >= (event.maxParticipants * 0.8).toInt()
                        // Если зарегистрированных нет — показываем просто максимум
                        val participantsText = when {
                            registered != null -> "$registered из ${event.maxParticipants}"
                            event.needsRegistration -> "до ${event.maxParticipants}"
                            else -> "${event.maxParticipants}"
                        }
                        DetailRow(
                            icon       = Icons.Outlined.Group,
                            label      = when {
                                registered != null -> "Зарегистрировано / макс."
                                event.needsRegistration -> "Макс. участников"
                                else -> "Макс. участников"
                            },
                            value      = participantsText,
                            valueColor = when {
                                isFull       -> RedEnd
                                isAlmostFull -> Color(0xFFF59E0B)
                                else         -> Color.Unspecified
                            }
                        )
                    }

                    // Просмотры — только admin
                    if (isAdmin && event.viewsCount != null) {
                        DetailDivider()
                        DetailRow(Icons.Outlined.Visibility, "Просмотры", "${event.viewsCount}")
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
                        valueColor = if (event.isFinished) MaterialTheme.colorScheme.onSurfaceVariant else GreenStart
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
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                    Text(event.description, modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp, lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (event.announcement.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("Анонс")
                Spacer(Modifier.height(8.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                    Text(event.announcement, modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp, lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Кнопка участников ─────────────────────────────────
            // Показываем всем если регистрация требуется
            if (event.needsRegistration) {
                OutlinedButton(
                    onClick  = onShowParticipants,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Outlined.Groups, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    val displayCount = event.participantsCount
                        ?: participants.size.takeIf { participants.isNotEmpty() }
                    val maxStr = event.maxParticipants?.let { " / $it" } ?: ""
                    Text(
                        if (displayCount != null) "Участники ($displayCount$maxStr)"
                        else "Список участников",
                        fontSize = 15.sp, fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Кнопка записаться / отменить / гость ─────────────
            when {
                // Гость — не может регистрироваться
                !canRegister -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.LockPerson, null,
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Войдите чтобы записаться",
                                fontSize = 14.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Авторизован, мероприятие завершено
                event.isFinished -> {
                    Button(
                        onClick  = {},
                        enabled  = false,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Outlined.EventBusy, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Мероприятие завершено", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Авторизован, не завершено — показываем кнопку регистрации
                else -> {
                    // Мест нет — заблокировать если не зарегистрирован
                    val isFull = event.needsRegistration &&
                            event.maxParticipants != null &&
                            (event.participantsCount
                                ?: participants.size.takeIf { participants.isNotEmpty() }
                                ?: 0) >= event.maxParticipants
                    val isBlocked = isFull && !isRegistered

                    if (isRegistered) {
                        // Уже записан — красноватая кнопка с иконкой отмены
                        Button(
                            onClick  = onRegister,
                            enabled  = !registrationLoading,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                contentColor   = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (registrationLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Icon(Icons.Outlined.PersonRemove, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Отменить запись",
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        // Не записан — синяя главная кнопка
                        Button(
                            onClick  = onRegister,
                            enabled  = !registrationLoading && !isBlocked,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = MaterialTheme.colorScheme.primary,
                                contentColor           = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (registrationLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(
                                    if (isBlocked) Icons.Outlined.EventBusy else Icons.Outlined.HowToReg,
                                    null, modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isBlocked) "Мест нет" else "Записаться",
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка участников
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ParticipantsSheetContent(
    participants: List<ParticipantDto>,
    isLoading: Boolean,
    maxParticipants: Int?
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Groups, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text("Участники", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            // Счётчик X / max
            val countText = if (maxParticipants != null) "${participants.size} / $maxParticipants"
            else "${participants.size}"
            Surface(shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer) {
                Text(countText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> Box(Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            participants.isEmpty() -> Box(
                Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.PersonOff, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                        modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Нет зарегистрированных участников",
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                }
            }

            else -> LazyColumn(
                modifier            = Modifier.fillMaxWidth(),
                contentPadding      = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(participants, key = { it.id }) { participant ->
                    ParticipantRow(participant)
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: ParticipantDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Аватар с инициалами
        Box(
            modifier        = Modifier.size(40.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(participant.initials, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(participant.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!participant.registrationTime.isNullOrBlank()) {
                val time = try {
                    java.time.OffsetDateTime.parse(participant.registrationTime)
                        .atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                } catch (_: Exception) { participant.registrationTime }
                Text("Зарегистрирован: $time", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вспомогательные компоненты
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun CompactInfoCard(
    icon: ImageVector, iconTint: Color, label: String, value: String, modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
        }
    }
}

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
            modifier = Modifier.fillMaxWidth()
                .clickable { onCopy(); scope.launch { copied = true; delay(300); copied = false } }
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(
                if (copied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
                null,
                tint = if (copied) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector, label: String, value: String, valueColor: Color = Color.Unspecified
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface
                else valueColor,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}