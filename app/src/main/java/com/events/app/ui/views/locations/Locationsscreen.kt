package com.events.app.ui.views.locations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EquipmentTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.PlaceTypeDto
import kotlinx.coroutines.delay

@Composable
fun LocationsScreen(
    viewModel: LocationsViewModel = hiltViewModel()
) {
    val locations        by viewModel.locations.collectAsState()
    val locationsLoading by viewModel.locationsLoading.collectAsState()
    val places           by viewModel.places.collectAsState()
    val placesLoading    by viewModel.placesLoading.collectAsState()
    val expandedId       by viewModel.expandedLocationId.collectAsState()
    val selectedPlace    by viewModel.selectedPlace.collectAsState()
    val selectedLocation by viewModel.selectedLocationForPlace.collectAsState()
    val placeTypes       by viewModel.placeTypes.collectAsState()
    val equipment        by viewModel.equipment.collectAsState()
    val equipmentLoading by viewModel.equipmentLoading.collectAsState()
    val equipmentTypes   by viewModel.equipmentTypes.collectAsState()
    val error            by viewModel.error.collectAsState()
    val successMessage   by viewModel.successMessage.collectAsState()
    val isActionLoading  by viewModel.isActionLoading.collectAsState()
    val isAdmin          = viewModel.isAdmin

    // Авто-скрытие уведомлений
    LaunchedEffect(error, successMessage) {
        if (error != null || successMessage != null) {
            delay(4000); viewModel.clearMessages()
        }
    }

    // Диалоги
    var showCreateLocationDialog  by remember { mutableStateOf(false) }
    var locationToDelete          by remember { mutableStateOf<LocationDto?>(null) }
    var showCreatePlaceDialog     by remember { mutableStateOf<Int?>(null) }
    var placeToDelete             by remember { mutableStateOf<Pair<Int, PlaceDto>?>(null) }
    var showAddEquipmentDialog    by remember { mutableStateOf(false) }
    var equipmentToDelete         by remember { mutableStateOf<EquipmentDto?>(null) }

    // ── BottomSheet карточки помещения ────────────────────────────
    if (selectedPlace != null && selectedLocation != null) {
        PlaceDetailSheet(
            place            = selectedPlace!!,
            location         = selectedLocation!!,
            isAdmin          = isAdmin,
            equipment        = equipment,
            equipmentLoading = equipmentLoading,
            onDelete = {
                selectedLocation?.id?.let { locId ->
                    placeToDelete = Pair(locId, selectedPlace!!)
                }
                viewModel.closePlace()
            },
            onDismiss           = { viewModel.closePlace() },
            onAddEquipment      = { showAddEquipmentDialog = true },
            onDeleteEquipment   = { eq -> equipmentToDelete = eq }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Уведомление ──────────────────────────────────────────
        AnimatedVisibility(visible = error != null || successMessage != null,
            enter = fadeIn(), exit = fadeOut()) {
            val isError = error != null
            Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isError) MaterialTheme.colorScheme.errorContainer
                else Color(0xFF22C55E).copy(alpha = 0.15f)) {
                Row(modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                        null,
                        tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp))
                    Text(error ?: successMessage ?: "",
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF166534),
                        fontSize = 13.sp)
                }
            }
        }

        // ── Шапка ─────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(if (locations.isEmpty()) "Нет локаций" else "Локаций: ${locations.size}",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isAdmin) {
                Button(onClick = { showCreateLocationDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    Icon(Icons.Outlined.AddLocation, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Локация", fontSize = 14.sp)
                }
            }
        }

        // ── Список локаций ────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                locationsLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                locations.isEmpty() -> Column(modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.LocationOff, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                        modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Локации не найдены",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f), fontSize = 15.sp)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(locations, key = { it.id }) { location ->
                        LocationCard(
                            location         = location,
                            isExpanded       = expandedId == location.id,
                            places           = if (expandedId == location.id) places else emptyList(),
                            placesLoading    = expandedId == location.id && placesLoading,
                            isAdmin          = isAdmin,
                            onToggle         = { viewModel.toggleLocation(location.id) },
                            onPlaceClick     = { place -> viewModel.openPlace(location.id, place) },
                            onDeleteLocation = { locationToDelete = location },
                            onAddPlace       = { showCreatePlaceDialog = location.id }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    // ── Диалоги ───────────────────────────────────────────────────

    if (showCreateLocationDialog) {
        CreateLocationDialog(isLoading = isActionLoading,
            onDismiss = { showCreateLocationDialog = false },
            onConfirm = { title, address ->
                viewModel.createLocation(title, address) { showCreateLocationDialog = false }
            })
    }

    locationToDelete?.let { loc ->
        ConfirmDeleteDialog(
            title       = "Удалить локацию?",
            description = "«${loc.title ?: "ID ${loc.id}"}» и все её помещения будут удалены.",
            onConfirm   = { viewModel.deleteLocation(loc.id); locationToDelete = null },
            onDismiss   = { locationToDelete = null }
        )
    }

    showCreatePlaceDialog?.let { locationId ->
        CreatePlaceDialog(placeTypes = placeTypes, isLoading = isActionLoading,
            onDismiss = { showCreatePlaceDialog = null },
            onConfirm = { number, capacity, typeId, title ->
                viewModel.createPlace(locationId, number, capacity, typeId, title) {
                    showCreatePlaceDialog = null
                }
            })
    }

    placeToDelete?.let { (locationId, place) ->
        ConfirmDeleteDialog(
            title       = "Удалить помещение?",
            description = "«${place.title ?: "№${place.number}"}» будет удалено безвозвратно.",
            onConfirm   = { viewModel.deletePlace(locationId, place.id); placeToDelete = null },
            onDismiss   = { placeToDelete = null }
        )
    }

    // Диалог добавления оборудования — только admin
    if (showAddEquipmentDialog && selectedPlace != null) {
        AddEquipmentDialog(
            equipmentTypes = equipmentTypes,
            isLoading      = isActionLoading,
            onDismiss      = { showAddEquipmentDialog = false },
            onConfirm      = { title, invNum, typeId ->
                viewModel.createEquipment(title, invNum, typeId, selectedPlace!!.id) {
                    showAddEquipmentDialog = false
                }
            }
        )
    }

    equipmentToDelete?.let { eq ->
        ConfirmDeleteDialog(
            title       = "Удалить оборудование?",
            description = "«${eq.title ?: "ID ${eq.id}"}» будет удалено безвозвратно.",
            onConfirm   = {
                selectedPlace?.let { place ->
                    viewModel.deleteEquipment(eq.id, place.id)
                }
                equipmentToDelete = null
            },
            onDismiss   = { equipmentToDelete = null }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// Карточка локации
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun LocationCard(
    location: LocationDto,
    isExpanded: Boolean,
    places: List<PlaceDto>,
    placesLoading: Boolean,
    isAdmin: Boolean,
    onToggle: () -> Unit,
    onPlaceClick: (PlaceDto) -> Unit,
    onDeleteLocation: () -> Unit,
    onAddPlace: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(0.25f)
        else MaterialTheme.colorScheme.surface, label = "locationBg"
    )
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = bgColor, tonalElevation = if (isExpanded) 0.dp else 1.dp) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(0.12f),
                    modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Business, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(location.title ?: "Локация ${location.id}",
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!location.address.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Place, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp))
                            Text(location.address, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (isAdmin) {
                    IconButton(onClick = onDeleteLocation, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, null,
                            tint = MaterialTheme.colorScheme.error.copy(0.7f),
                            modifier = Modifier.size(18.dp))
                    }
                }
                Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp))
            }

            AnimatedVisibility(visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit  = shrinkVertically() + fadeOut()) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.15f))
                    when {
                        placesLoading -> Box(modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                        places.isEmpty() -> Column(modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.MeetingRoom, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                                modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Нет помещений", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                            if (isAdmin) {
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(onClick = onAddPlace, shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Добавить помещение", fontSize = 12.sp)
                                }
                            }
                        }
                        else -> Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            places.forEach { place ->
                                PlaceRow(place = place, onClick = { onPlaceClick(place) })
                            }
                            if (isAdmin) {
                                Spacer(Modifier.height(4.dp))
                                OutlinedButton(onClick = onAddPlace,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Добавить помещение", fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Строка помещения ──────────────────────────────────────────────
@Composable
private fun PlaceRow(place: PlaceDto, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(0.6f),
                modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.MeetingRoom, null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(buildString {
                    if (!place.title.isNullOrBlank()) append(place.title)
                    else append("Помещение")
                    if (!place.number.isNullOrBlank()) append(" №${place.number}")
                }, fontWeight = FontWeight.Medium, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!place.type.isNullOrBlank()) {
                        Text(place.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("${place.capacity} мест", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Outlined.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                modifier = Modifier.size(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// BottomSheet карточки помещения — с оборудованием
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceDetailSheet(
    place: PlaceDto,
    location: LocationDto,
    isAdmin: Boolean,
    equipment: List<EquipmentDto>,
    equipmentLoading: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onAddEquipment: () -> Unit,
    onDeleteEquipment: (EquipmentDto) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(modifier = Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)) {

            // ── Заголовок ─────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.MeetingRoom, null,
                            tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(buildString {
                        if (!place.title.isNullOrBlank()) append(place.title)
                        else append("Помещение")
                        if (!place.number.isNullOrBlank()) append(" №${place.number}")
                    }, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(location.title ?: "Локация ${location.id}",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isAdmin) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.DeleteOutline, "Удалить",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Детали помещения ──────────────────────────────────
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.6f)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    PlaceDetailRow(Icons.Outlined.Tag, "Номер", place.number ?: "Не указан")
                    PlaceDetailDivider()
                    PlaceDetailRow(Icons.Outlined.Category, "Тип", place.type ?: "Не указан")
                    PlaceDetailDivider()
                    PlaceDetailRow(Icons.Outlined.EventSeat, "Вместимость", "${place.capacity} мест")
                    PlaceDetailDivider()
                    PlaceDetailRow(Icons.Outlined.LocationOn, "Локация",
                        "${location.title ?: "—"}${if (!location.address.isNullOrBlank()) "\n${location.address}" else ""}")
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Оборудование ──────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Devices, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Оборудование", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                    if (equipment.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${equipment.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                // Кнопка добавить — только admin
                if (isAdmin) {
                    OutlinedButton(onClick = onAddEquipment, shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Outlined.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Добавить", fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            when {
                equipmentLoading -> Box(modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                equipment.isEmpty() -> Surface(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Devices, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                            modifier = Modifier.size(24.dp))
                        Text("Оборудование не добавлено", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                    }
                }
                else -> Surface(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.6f)) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        equipment.forEachIndexed { idx, eq ->
                            EquipmentRow(
                                equipment = eq,
                                isAdmin   = isAdmin,
                                onDelete  = { onDeleteEquipment(eq) }
                            )
                            if (idx < equipment.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(0.12f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Строка оборудования ───────────────────────────────────────────
@Composable
private fun EquipmentRow(
    equipment: EquipmentDto,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(0.5f),
            modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Devices, null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(equipment.title ?: "Оборудование #${equipment.id}",
                fontWeight = FontWeight.Medium, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!equipment.type.isNullOrBlank()) {
                    Text(equipment.type, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
                if (!equipment.inventoryNumber.isNullOrBlank()) {
                    Text("№ ${equipment.inventoryNumber}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // Кнопка удалить — только admin
        if (isAdmin) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.DeleteOutline, null,
                    tint = MaterialTheme.colorScheme.error.copy(0.7f),
                    modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Детали помещения ──────────────────────────────────────────────
@Composable
private fun PlaceDetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PlaceDetailDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp),
        color = MaterialTheme.colorScheme.outline.copy(0.12f))
}

// ═══════════════════════════════════════════════════════════════════
// Диалог добавления оборудования (только admin)
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEquipmentDialog(
    equipmentTypes: List<EquipmentTypeDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, inventoryNumber: String, typeId: Int) -> Unit
) {
    var title          by remember { mutableStateOf("") }
    var inventoryNumber by remember { mutableStateOf("") }
    var selectedTypeId by remember { mutableStateOf<Int?>(null) }
    var typeExpanded   by remember { mutableStateOf(false) }

    val isValid = title.trim().isNotBlank()
            && inventoryNumber.trim().isNotBlank()
            && selectedTypeId != null

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Devices, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text("Добавить оборудование", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
                Spacer(Modifier.height(20.dp))

                // Название
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Название *") },
                    leadingIcon = { Icon(Icons.Outlined.Devices, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(Modifier.height(10.dp))

                // Инвентарный номер
                OutlinedTextField(value = inventoryNumber, onValueChange = { inventoryNumber = it },
                    label = { Text("Инвентарный номер *") },
                    leadingIcon = { Icon(Icons.Outlined.Tag, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(Modifier.height(10.dp))

                // Тип оборудования
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = equipmentTypes.find { it.id == selectedTypeId }?.title ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Тип оборудования *") },
                        leadingIcon = { Icon(Icons.Outlined.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        if (equipmentTypes.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Загрузка...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { typeExpanded = false })
                        } else {
                            equipmentTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.title ?: "Тип ${type.id}") },
                                    onClick = { selectedTypeId = type.id; typeExpanded = false })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading) { Text("Отмена") }
                    Button(onClick = {
                        if (isValid) onConfirm(title.trim(), inventoryNumber.trim(), selectedTypeId!!)
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        enabled = isValid && !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Добавить", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Остальные диалоги ─────────────────────────────────────────────

@Composable
private fun CreateLocationDialog(isLoading: Boolean, onDismiss: () -> Unit,
                                 onConfirm: (title: String, address: String) -> Unit) {
    var title   by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val isValid = title.trim().isNotBlank() && address.trim().isNotBlank()
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.AddLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text("Новая локация", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название *") },
                    leadingIcon = { Icon(Icons.Outlined.Business, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Адрес *") },
                    leadingIcon = { Icon(Icons.Outlined.Place, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isLoading) { Text("Отмена") }
                    Button(onClick = { if (isValid) onConfirm(title.trim(), address.trim()) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = isValid && !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Создать", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePlaceDialog(placeTypes: List<PlaceTypeDto>, isLoading: Boolean, onDismiss: () -> Unit,
                              onConfirm: (number: String, capacity: Int, typeId: Int, title: String?) -> Unit) {
    var number by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedTypeId by remember { mutableStateOf<Int?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }
    val capacityInt = capacity.trim().toIntOrNull()
    val isValid = number.trim().isNotBlank() && capacityInt != null && capacityInt > 0 && selectedTypeId != null
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.MeetingRoom, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text("Новое помещение", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") },
                    leadingIcon = { Icon(Icons.Outlined.Label, null) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = number, onValueChange = { if (it.length <= 4) number = it }, label = { Text("Номер *") },
                    leadingIcon = { Icon(Icons.Outlined.Tag, null) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isLoading,
                    supportingText = { Text("Макс. 4 символа", fontSize = 10.sp) })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = capacity, onValueChange = { capacity = it.filter { c -> c.isDigit() } }, label = { Text("Вместимость *") },
                    leadingIcon = { Icon(Icons.Outlined.EventSeat, null) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(value = placeTypes.find { it.id == selectedTypeId }?.title ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Тип помещения *") },
                        leadingIcon = { Icon(Icons.Outlined.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        if (placeTypes.isEmpty()) DropdownMenuItem(text = { Text("Загрузка...") }, onClick = { typeExpanded = false })
                        else placeTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.title ?: "Тип ${type.id}") },
                                onClick = { selectedTypeId = type.id; typeExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isLoading) { Text("Отмена") }
                    Button(onClick = { if (isValid) onConfirm(number.trim(), capacityInt!!, selectedTypeId!!, title.trim().takeIf { it.isNotBlank() }) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = isValid && !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Создать", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(title: String, description: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text  = { Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            Button(onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape  = RoundedCornerShape(10.dp)) { Text("Удалить", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Отмена") }
        })
}