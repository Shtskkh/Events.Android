package com.events.app.ui.views.eventdetail

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.ParticipantDto
import com.events.app.data.remote.dto.UserDetailDto
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
    val placeEquipment      by viewModel.placeEquipment.collectAsState()
    val equipmentLoading    by viewModel.equipmentLoading.collectAsState()
    val author              by viewModel.author.collectAsState()
    val authorLoading       by viewModel.authorLoading.collectAsState()

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
        isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector        = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
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
            placeEquipment      = placeEquipment,
            equipmentLoading    = equipmentLoading,
            author              = author,
            authorLoading       = authorLoading,
            onDelete            = { viewModel.deleteEvent(event!!.id) },
            onEdit              = { onEdit?.invoke(event!!.id) },
            onRegister          = { viewModel.toggleRegistration() },
            onShowParticipants  = { showParticipantsSheet = true }
        )
        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showParticipantsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showParticipantsSheet = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            ParticipantsSheetContent(
                participants    = participants,
                isLoading       = participantsLoading,
                maxParticipants = event?.maxParticipants
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// EventContent
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
    placeEquipment: List<EquipmentDto>,
    equipmentLoading: Boolean,
    author: UserDetailDto?,
    authorLoading: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onRegister: () -> Unit,
    onShowParticipants: () -> Unit
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
        label       = "titleCopyBg"
    )

    fun copyText(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon  = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(text = "Удалить мероприятие?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    text     = "«${event.title}» будет удалено без возможности восстановления.",
                    fontSize = 14.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text(text = "Удалить", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text(text = "Отмена")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // ── Превью ────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            AsyncImage(
                model              = event.previewUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                        )
                    )
            )
            if (event.isFinished) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(all = 14.dp),
                    shape    = RoundedCornerShape(8.dp),
                    color    = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        text     = "Завершено",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White
                    )
                }
            }
            if (isAdmin) {
                if (!event.isFinished) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(all = 14.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    ) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector        = Icons.Outlined.Edit,
                                contentDescription = "Редактировать",
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(all = 14.dp),
                    shape    = RoundedCornerShape(10.dp),
                    color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
                ) {
                    IconButton(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !isDeleting,
                        modifier = Modifier.size(44.dp)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Outlined.DeleteOutline,
                                contentDescription = "Удалить",
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Заголовок ─────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color    = titleBg,
                shape    = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            copyText(event.title, "Название")
                            scope.launch { titleCopied = true; delay(300); titleCopied = false }
                        }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment     = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text          = event.title,
                        fontSize      = 24.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color         = MaterialTheme.colorScheme.onBackground,
                        lineHeight    = 30.sp,
                        modifier      = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector        = if (titleCopied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint               = if (titleCopied) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier           = Modifier.size(16.dp).padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Дата и место ──────────────────────────────────────
            SectionTitle(text = "Дата и место")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            val placeDisplay: String? = run {
                val titlePart  = event.placeTitle?.takeIf { it.isNotBlank() }
                val numberPart = event.placeNumber?.takeIf { it.isNotBlank() }
                val capPart    = event.placeCapacity?.let { "$it мест" }
                val namePart = when {
                    titlePart != null && numberPart != null -> "$titlePart №$numberPart"
                    titlePart != null                       -> titlePart
                    numberPart != null                      -> "Помещение №$numberPart"
                    else                                    -> null
                }
                when {
                    namePart != null && capPart != null -> "$namePart  $capPart"
                    namePart != null                    -> namePart
                    capPart != null                     -> capPart
                    else                                -> null
                }
            }
            val hasLocation = event.location.isNotBlank()
            val hasPlace    = placeDisplay != null

            if (hasLocation || hasPlace) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasLocation) {
                        CompactInfoCard(
                            icon     = Icons.Outlined.LocationOn,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            label    = "Локация",
                            value    = event.location,
                            modifier = if (hasPlace) Modifier.weight(1f) else Modifier.fillMaxWidth()
                        )
                    }
                    if (hasPlace) {
                        CompactInfoCard(
                            icon     = Icons.Outlined.MeetingRoom,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label    = "Помещение",
                            value    = placeDisplay!!,
                            modifier = if (hasLocation) Modifier.weight(1f) else Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── Оборудование помещения ─────────────────────────────
            if (event.placeId != null && (placeEquipment.isNotEmpty() || equipmentLoading)) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(text = "Оборудование помещения")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    if (equipmentLoading) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(all = 24.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                    } else {
                        Column(modifier = Modifier.padding(all = 4.dp)) {
                            placeEquipment.forEachIndexed { idx, eq ->
                                EquipmentDetailRow(equipment = eq)
                                if (idx < placeEquipment.lastIndex) DetailDivider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── О мероприятии ─────────────────────────────────────
            SectionTitle(text = "О мероприятии")
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(all = 4.dp)) {

                    if (event.type.isNotBlank()) {
                        DetailRow(
                            icon       = Icons.Outlined.Category,
                            label      = "Тип",
                            value      = event.type,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        DetailDivider()
                    }
                    if (event.format.isNotBlank()) {
                        DetailRow(
                            icon       = formatIcon,
                            label      = "Формат",
                            value      = event.format,
                            valueColor = formatColor
                        )
                        DetailDivider()
                    }

                    // ── Автор мероприятия ──────────────────────────
                    // Показываем для всех пользователей.
                    // Ячейка содержит две строки: ФИО автора и его ID (с кнопкой копирования).
                    if (authorLoading) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Person,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text     = "Автор",
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(16.dp).padding(top = 2.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                        DetailDivider()
                    } else if (author != null || !event.userId.isNullOrBlank()) {
                        AuthorDetailRow(
                            author  = author,
                            userId  = event.userId,
                            onCopy  = { copyText(event.userId ?: "", "ID создателя") }
                        )
                        DetailDivider()
                    }

                    if (isAdmin && !event.id.isBlank()) {
                        CopyableDetailRow(
                            icon   = Icons.Outlined.Key,
                            label  = "ID мероприятия",
                            value  = event.id,
                            onCopy = { copyText(event.id, "ID мероприятия") }
                        )
                        DetailDivider()
                    }

                    DetailRow(
                        icon       = if (event.needsRegistration) Icons.Outlined.HowToReg else Icons.Outlined.PersonOff,
                        label      = "Регистрация",
                        value      = if (event.needsRegistration) "Требуется" else "Не требуется",
                        valueColor = if (event.needsRegistration) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (event.maxParticipants != null) {
                        DetailDivider()
                        val registered   = event.participantsCount
                            ?: participants.size.takeIf { participants.isNotEmpty() }
                        val isFull       = registered != null && registered >= event.maxParticipants
                        val isAlmostFull = registered != null &&
                                registered >= (event.maxParticipants * 0.8).toInt()
                        DetailRow(
                            icon   = Icons.Outlined.Group,
                            label  = if (registered != null) "Зарегистрировано / макс." else "Макс. участников",
                            value  = if (registered != null) "$registered из ${event.maxParticipants}"
                            else "${event.maxParticipants}",
                            valueColor = when {
                                isFull       -> RedEnd
                                isAlmostFull -> Color(0xFFF59E0B)
                                else         -> Color.Unspecified
                            }
                        )
                    }

                    if (isAdmin && event.viewsCount != null) {
                        DetailDivider()
                        DetailRow(
                            icon  = Icons.Outlined.Visibility,
                            label = "Просмотры",
                            value = "${event.viewsCount}"
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
                        DetailRow(icon = Icons.Outlined.Person, label = "Организатор", value = event.organizerName)
                    }
                    if (!event.link.isNullOrBlank()) {
                        DetailDivider()
                        DetailRow(icon = Icons.Outlined.Link, label = "Ссылка", value = event.link)
                    }
                }
            }

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(text = "Описание")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text       = event.description,
                        modifier   = Modifier.padding(all = 16.dp),
                        fontSize   = 15.sp,
                        lineHeight = 24.sp,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (event.announcement.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(text = "Анонс")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text       = event.announcement,
                        modifier   = Modifier.padding(all = 16.dp),
                        fontSize   = 14.sp,
                        lineHeight = 22.sp,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Кнопка участников ─────────────────────────────────
            if (event.needsRegistration) {
                OutlinedButton(
                    onClick  = onShowParticipants,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val displayCount = event.participantsCount
                        ?: participants.size.takeIf { participants.isNotEmpty() }
                    val maxStr = event.maxParticipants?.let { " / $it" } ?: ""
                    Text(
                        text     = if (displayCount != null) "Участники ($displayCount$maxStr)"
                        else "Список участников",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ── Кнопка записаться / отменить ──────────────────────
            when {
                !canRegister -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(all = 14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.LockPerson,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text     = "Войдите чтобы записаться",
                                fontSize = 14.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Мероприятие завершено", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                else -> {
                    val isFull = event.needsRegistration &&
                            event.maxParticipants != null &&
                            (event.participantsCount
                                ?: participants.size.takeIf { participants.isNotEmpty() }
                                ?: 0) >= event.maxParticipants
                    val isBlocked = isFull && !isRegistered

                    if (isRegistered) {
                        Button(
                            onClick  = onRegister,
                            enabled  = !registrationLoading,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                contentColor           = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (registrationLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Icon(Icons.Outlined.Close, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Отменить запись", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Button(
                            onClick  = onRegister,
                            enabled  = !registrationLoading && !isBlocked,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(16.dp)
                        ) {
                            if (registrationLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector        = if (isBlocked) Icons.Outlined.EventBusy else Icons.Outlined.HowToReg,
                                    contentDescription = null,
                                    modifier           = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text       = if (isBlocked) "Мест нет" else "Записаться",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Ячейка "Автор" — ФИО + ID с кнопкой копирования
// Показывается всем пользователям, не только администраторам.
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun AuthorDetailRow(
    author: UserDetailDto?,
    userId: String?,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }
    val scope  = rememberCoroutineScope()
    val bgColor by animateColorAsState(
        targetValue = if (copied) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        else Color.Transparent,
        label       = "authorCopyBg"
    )

    // Строим ФИО: Фамилия Имя Отчество
    val fullName = author?.let { u ->
        listOfNotNull(
            u.lastName?.trim(),
            u.firstName?.trim(),
            u.patronymic?.trim()
        ).filter { it.isNotBlank() }.joinToString(" ").takeIf { it.isNotBlank() }
    } ?: "—"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = bgColor,
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!userId.isNullOrBlank()) {
                        onCopy()
                        scope.launch { copied = true; delay(300); copied = false }
                    }
                }
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.Person,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "Автор",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Строка 1: ФИО
                Text(
                    text       = fullName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                // Строка 2: ID
                if (!userId.isNullOrBlank()) {
                    Text(
                        text     = userId,
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!userId.isNullOrBlank()) {
                Icon(
                    imageVector        = if (copied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint               = if (copied) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier           = Modifier.size(15.dp)
                )
            }
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
        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Groups,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
                Text(
                    text       = "Участники",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val countText = if (maxParticipants != null) "${participants.size} / $maxParticipants"
            else "${participants.size}"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text     = countText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color    = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(8.dp))
        when {
            isLoading -> Box(
                modifier         = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            participants.isEmpty() -> Box(
                modifier         = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Outlined.PersonOff,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier           = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text     = "Нет зарегистрированных участников",
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            else -> LazyColumn(
                modifier            = Modifier.fillMaxWidth(),
                contentPadding      = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items = participants, key = { it.id }) { participant ->
                    ParticipantRow(participant = participant)
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: ParticipantDto) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = participant.initials,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = participant.displayName,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (!participant.registrationTime.isNullOrBlank()) {
                val time = try {
                    java.time.OffsetDateTime.parse(participant.registrationTime)
                        .atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                } catch (_: Exception) { participant.registrationTime }
                Text(
                    text     = "Зарегистрирован: $time",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вспомогательные компоненты
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EquipmentDetailRow(equipment: EquipmentDto) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape    = RoundedCornerShape(8.dp),
            color    = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = Icons.Outlined.PersonSearch,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.tertiary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = equipment.title ?: "Оборудование #${equipment.id}",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!equipment.type.isNullOrBlank()) {
                    Text(text = equipment.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                if (!equipment.inventoryNumber.isNullOrBlank()) {
                    Text(
                        text  = "№ ${equipment.inventoryNumber}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text       = value,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun CopyableDetailRow(icon: ImageVector, label: String, value: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    val scope  = rememberCoroutineScope()
    val bgColor by animateColorAsState(
        targetValue = if (copied) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        else Color.Transparent,
        label       = "copyBg"
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
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text       = value,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector        = if (copied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
                contentDescription = null,
                tint               = if (copied) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier           = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text       = value,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
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
        text          = text,
        fontSize      = 13.sp,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color         = MaterialTheme.colorScheme.onSurfaceVariant
    )
}