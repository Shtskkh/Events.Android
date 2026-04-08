package com.events.app.ui.views.locations

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EquipmentTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.PlaceTypeDto
import kotlinx.coroutines.delay

@Composable
fun LocationsScreen(viewModel: LocationsViewModel = hiltViewModel()) {
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
    val isAdmin          by viewModel.isAdminFlow.collectAsState()

    LaunchedEffect(error, successMessage) {
        if (error != null || successMessage != null) {
            delay(4000)
            viewModel.clearMessages()
        }
    }

    var showCreateLocationDialog by remember { mutableStateOf(false) }
    var locationToEdit           by remember { mutableStateOf<LocationDto?>(null) }
    var locationToDelete         by remember { mutableStateOf<LocationDto?>(null) }
    var showCreatePlaceDialog    by remember { mutableStateOf<Int?>(null) }
    var placeToEdit              by remember { mutableStateOf<Pair<Int, PlaceDto>?>(null) }
    var placeToDelete            by remember { mutableStateOf<Pair<Int, PlaceDto>?>(null) }
    var showAddEquipmentDialog   by remember { mutableStateOf(false) }
    var equipmentToDelete        by remember { mutableStateOf<EquipmentDto?>(null) }

    // ── BottomSheet карточки помещения ────────────────────────────
    if (selectedPlace != null && selectedLocation != null) {
        PlaceDetailSheet(
            place            = selectedPlace!!,
            location         = selectedLocation!!,
            isAdmin          = isAdmin,
            equipment        = equipment,
            equipmentLoading = equipmentLoading,
            onEdit           = {
                selectedLocation?.id?.let { locId ->
                    placeToEdit = Pair(locId, selectedPlace!!)
                }
            },
            onDelete         = {
                selectedLocation?.id?.let { locId ->
                    placeToDelete = Pair(locId, selectedPlace!!)
                }
                viewModel.closePlace()
            },
            onDismiss         = { viewModel.closePlace() },
            onAddEquipment    = { showAddEquipmentDialog = true },
            onDeleteEquipment = { eq -> equipmentToDelete = eq }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // ── Уведомление ───────────────────────────────────────────
        AnimatedVisibility(
            visible = error != null || successMessage != null,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            val isError = error != null
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape    = RoundedCornerShape(12.dp),
                color    = if (isError) MaterialTheme.colorScheme.errorContainer
                else Color(0xFF22C55E).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier              = Modifier.padding(all = 12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint               = if (isError) MaterialTheme.colorScheme.error else Color(0xFF22C55E),
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        text  = error ?: successMessage ?: "",
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF166834),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── Шапка ─────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text     = if (locations.isEmpty()) "Нет локаций" else "Локаций: ${locations.size}",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isAdmin) {
                Button(
                    onClick        = { showCreateLocationDialog = true },
                    shape          = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.AddLocation,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Локация", fontSize = 14.sp)
                }
            }
        }

        // ── Список ────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                locationsLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                locations.isEmpty() -> Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.LocationOff,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier           = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text     = "Локации не найдены",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 15.sp
                    )
                }
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = locations, key = { it.id }) { location ->
                        LocationCard(
                            location         = location,
                            isExpanded       = expandedId == location.id,
                            places           = if (expandedId == location.id) places else emptyList(),
                            placesLoading    = expandedId == location.id && placesLoading,
                            isAdmin          = isAdmin,
                            onToggle         = { viewModel.toggleLocation(location.id) },
                            onPlaceClick     = { place -> viewModel.openPlace(location.id, place) },
                            onEditLocation   = { locationToEdit = location },
                            onDeleteLocation = { locationToDelete = location },
                            onAddPlace       = { showCreatePlaceDialog = location.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // ── Диалоги ───────────────────────────────────────────────────
    if (showCreateLocationDialog) {
        CreateLocationDialog(
            isLoading = isActionLoading,
            onDismiss = { showCreateLocationDialog = false },
            onConfirm = { title, address, photoUri ->
                viewModel.createLocation(title, address, photoUri) { showCreateLocationDialog = false }
            }
        )
    }

    locationToEdit?.let { loc ->
        EditLocationDialog(
            location  = loc,
            isLoading = isActionLoading,
            onDismiss = { locationToEdit = null },
            onConfirm = { t, a ->
                viewModel.editLocation(loc, t, a) { locationToEdit = null }
            }
        )
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
        CreatePlaceDialog(
            placeTypes = placeTypes,
            isLoading  = isActionLoading,
            onDismiss  = { showCreatePlaceDialog = null },
            onConfirm  = { number, capacity, typeId, title, uris ->
                viewModel.createPlace(locationId, number, capacity, typeId, title, uris) {
                    showCreatePlaceDialog = null
                }
            }
        )
    }

    placeToEdit?.let { (locationId, place) ->
        PlaceEditDialog(
            place      = place,
            placeTypes = placeTypes,
            isLoading  = isActionLoading,
            onDismiss  = { placeToEdit = null },
            onConfirm  = { newTitle, newCapacity, newTypeId ->
                viewModel.editPlace(locationId, place, newTitle, newCapacity, newTypeId) {
                    placeToEdit = null
                }
            }
        )
    }

    placeToDelete?.let { (locationId, place) ->
        ConfirmDeleteDialog(
            title       = "Удалить помещение?",
            description = "«${place.title ?: "№${place.number}"}» будет удалено безвозвратно.",
            onConfirm   = { viewModel.deletePlace(locationId, place.id); placeToDelete = null },
            onDismiss   = { placeToDelete = null }
        )
    }

    if (showAddEquipmentDialog && selectedPlace != null) {
        AddEquipmentDialog(
            equipmentTypes = equipmentTypes,
            isLoading      = isActionLoading,
            onDismiss      = { showAddEquipmentDialog = false },
            onConfirm      = { t, invNum, typeId ->
                viewModel.createEquipment(t, invNum, typeId, selectedPlace!!.id) {
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
                selectedPlace?.let { p -> viewModel.deleteEquipment(eq.id, p.id) }
                equipmentToDelete = null
            },
            onDismiss   = { equipmentToDelete = null }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// LocationCard — карточка как у мероприятия с фото
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
    onEditLocation: () -> Unit,
    onDeleteLocation: () -> Unit,
    onAddPlace: () -> Unit
) {
    Column {
        // ── Карточка локации как у ивента ─────────────────────────
        Card(
            onClick   = onToggle,
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── Фото ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    val previewUrl = location.buildPreviewUrl()
                    if (previewUrl != null) {
                        AsyncImage(
                            model = previewUrl,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        )
                    } else {
                        Box(
                            modifier         = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Business,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier           = Modifier.size(56.dp)
                            )
                        }
                    }
                    // Градиент как у ивента
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
                                    startY = 80f
                                )
                            )
                    )
                    // Кнопки для админа
                    if (isAdmin) {
                        Row(
                            modifier              = Modifier
                                .align(Alignment.TopEnd)
                                .padding(all = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                            ) {
                                IconButton(
                                    onClick  = onEditLocation,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Outlined.Edit,
                                        contentDescription = "Редактировать",
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
                            ) {
                                IconButton(
                                    onClick  = onDeleteLocation,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Удалить",
                                        tint               = MaterialTheme.colorScheme.error,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Название + адрес + стрелка ────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = location.title ?: "Локация ${location.id}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 18.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        if (!location.address.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.Place,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(12.dp)
                                )
                                Text(
                                    text     = location.address,
                                    fontSize = 13.sp,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector        = if (isExpanded) Icons.Outlined.ExpandLess
                        else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── Список помещений под карточкой ────────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
            ) {
                when {
                    placesLoading -> Box(
                        modifier         = Modifier.fillMaxWidth().padding(all = 24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }

                    places.isEmpty() -> Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier            = Modifier.padding(all = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.MeetingRoom,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier           = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text     = "Нет помещений",
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            if (isAdmin) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick        = onAddPlace,
                                    shape          = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Outlined.Add,
                                        contentDescription = null,
                                        modifier           = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Добавить помещение", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        places.forEach { place ->
                            PlaceRow(place = place, onClick = { onPlaceClick(place) })
                        }
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick        = onAddPlace,
                                modifier       = Modifier.fillMaxWidth(),
                                shape          = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier           = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Добавить помещение", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Строка помещения (оригинальный стиль) ────────────────────────

@Composable
private fun PlaceRow(place: PlaceDto, onClick: () -> Unit) {
    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        color     = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick   = onClick                          // ← вся строка кликабельна
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape    = RoundedCornerShape(8.dp),
                color    = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Outlined.MeetingRoom,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.secondary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        if (!place.title.isNullOrBlank()) append(place.title)
                        else append("Помещение")
                        if (!place.number.isNullOrBlank()) append(" №${place.number}")
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!place.type.isNullOrBlank()) {
                        Text(
                            text     = place.type,
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text     = "${place.capacity} мест",
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Иконка теперь только декоративная, клик на Surface
            Icon(
                imageVector        = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PlaceDetailSheet — BottomSheet карточки помещения (оригинал)
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceDetailSheet(
    place: PlaceDto,
    location: LocationDto,
    isAdmin: Boolean,
    equipment: List<EquipmentDto>,
    equipmentLoading: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onAddEquipment: () -> Unit,
    onDeleteEquipment: (EquipmentDto) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // ── Фото ──────────────────────────────────────────────
            val photoUrl = place.buildPreviewUrl()
            if (photoUrl != null) {
                AsyncImage(
                    model              = photoUrl,
                    contentDescription = "Фото помещения",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .wrapContentHeight()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Заголовок ──────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = Icons.Outlined.MeetingRoom,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.secondary,
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            if (!place.title.isNullOrBlank()) append(place.title)
                            else append("Помещение")
                            if (!place.number.isNullOrBlank()) append(" №${place.number}")
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text     = location.title ?: "Локация ${location.id}",
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isAdmin) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector        = Icons.Outlined.Edit,
                            contentDescription = "Редактировать",
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector        = Icons.Outlined.DeleteOutline,
                            contentDescription = "Удалить",
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Детали ────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape    = RoundedCornerShape(14.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(all = 4.dp)) {
                    PlaceDetailRow(
                        icon  = Icons.Outlined.Tag,
                        label = "Номер",
                        value = place.number ?: "Не указан"
                    )
                    PlaceDetailDivider()
                    PlaceDetailRow(
                        icon  = Icons.Outlined.Category,
                        label = "Тип",
                        value = place.type ?: "Не указан"
                    )
                    PlaceDetailDivider()
                    PlaceDetailRow(
                        icon  = Icons.Outlined.EventSeat,
                        label = "Вместимость",
                        value = "${place.capacity} мест"
                    )
                    PlaceDetailDivider()
                    PlaceDetailRow(
                        icon  = Icons.Outlined.LocationOn,
                        label = "Локация",
                        value = buildString {
                            append(location.title ?: "—")
                            if (!location.address.isNullOrBlank() &&
                                location.address.trim() != location.title?.trim()) {
                                append("\n${location.address}")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Оборудование ──────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Devices,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        text       = "Оборудование",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    if (equipment.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text       = "${equipment.size}",
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                if (isAdmin) {
                    OutlinedButton(
                        onClick        = onAddEquipment,
                        shape          = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Добавить", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                equipmentLoading -> Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }

                equipment.isEmpty() -> Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier              = Modifier.padding(all = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Devices,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier           = Modifier.size(24.dp)
                        )
                        Text(
                            text     = "Оборудование не добавлено",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(all = 4.dp)) {
                        equipment.forEachIndexed { idx, eq ->
                            EquipmentRow(
                                equipment = eq,
                                isAdmin   = isAdmin,
                                onDelete  = { onDeleteEquipment(eq) }
                            )
                            if (idx < equipment.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Вспомогательные компоненты ────────────────────────────────────

@Composable
private fun PlaceDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlaceDetailDivider() = HorizontalDivider(
    modifier = Modifier.padding(horizontal = 14.dp),
    color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
)

@Composable
private fun EquipmentRow(equipment: EquipmentDto, isAdmin: Boolean, onDelete: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape    = RoundedCornerShape(8.dp),
            color    = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = Icons.Outlined.Devices,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.tertiary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = equipment.title ?: "Оборудование #${equipment.id}",
                fontWeight = FontWeight.Medium,
                fontSize   = 13.sp,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!equipment.type.isNullOrBlank())
                    Text(text = equipment.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                if (!equipment.inventoryNumber.isNullOrBlank())
                    Text(
                        text     = "№ ${equipment.inventoryNumber}",
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
        }
        if (isAdmin) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector        = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Диалог создания локации (с фото)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun CreateLocationDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, address: String, photoUri: Uri?) -> Unit
) {
    var title       by remember { mutableStateOf("") }
    var address     by remember { mutableStateOf("") }
    var photoUri    by remember { mutableStateOf<Uri?>(null) }
    val isValid     = title.trim().isNotBlank() && address.trim().isNotBlank()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> photoUri = uri }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 24.dp)
            ) {
                Row(
                    modifier              = Modifier.padding(bottom = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.AddLocation,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(24.dp)
                    )
                    Text(
                        text       = "Новая локация",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp
                    )
                }

                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text(text = "Название *") },
                    leadingIcon   = { Icon(Icons.Outlined.Business, null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    enabled       = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value         = address,
                    onValueChange = { address = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text(text = "Адрес *") },
                    leadingIcon   = { Icon(Icons.Outlined.Place, null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    enabled       = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Фото ──────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(18.dp)
                        )
                        Text(
                            text       = "Фото локации",
                            fontWeight = FontWeight.Medium,
                            fontSize   = 14.sp
                        )
                        if (photoUri != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text       = "1",
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick        = { photoPicker.launch("image/*") },
                        enabled        = !isLoading,
                        shape          = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text     = if (photoUri == null) "Добавить" else "Изменить",
                            fontSize = 12.sp
                        )
                    }
                }

                if (photoUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        AsyncImage(
                            model              = photoUri,
                            contentDescription = "Превью",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    TextButton(
                        onClick  = { photoUri = null },
                        enabled  = !isLoading,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text     = "Удалить фото",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = !isLoading
                    ) { Text(text = "Отмена") }
                    Button(
                        onClick  = { if (isValid) onConfirm(title.trim(), address.trim(), photoUri) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = isValid && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        else Text(text = "Создать", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Диалог редактирования локации (только Title + Address, API PATCH
// не поддерживает изменение фото)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EditLocationDialog(
    location: LocationDto,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, address: String) -> Unit
) {
    var title   by remember { mutableStateOf(location.title ?: "") }
    var address by remember { mutableStateOf(location.address ?: "") }
    val isValid = title.trim().isNotBlank() && address.trim().isNotBlank()

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(all = 24.dp)) {
                Row(
                    modifier              = Modifier.padding(bottom = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(24.dp)
                    )
                    Text(
                        text       = "Редактировать локацию",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp
                    )
                }
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text(text = "Название *") },
                    leadingIcon   = { Icon(Icons.Outlined.Business, null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    enabled       = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value         = address,
                    onValueChange = { address = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text(text = "Адрес *") },
                    leadingIcon   = { Icon(Icons.Outlined.Place, null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    enabled       = !isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = !isLoading
                    ) { Text(text = "Отмена") }
                    Button(
                        onClick  = { if (isValid) onConfirm(title.trim(), address.trim()) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = isValid && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        else Text(text = "Сохранить", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Диалог редактирования помещения
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceEditDialog(
    place: PlaceDto,
    placeTypes: List<PlaceTypeDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newTitle: String?, newCapacity: Int, newTypeId: Int) -> Unit
) {
    var editTitle    by remember { mutableStateOf(place.title ?: "") }
    var editCapacity by remember { mutableStateOf(place.capacity.toString()) }
    var selectedTypeId by remember {
        mutableStateOf(placeTypes.find { it.title == place.type }?.id ?: placeTypes.firstOrNull()?.id)
    }
    var typeExpanded by remember { mutableStateOf(false) }
    val capacityInt  = editCapacity.trim().toIntOrNull()
    val isValid      = capacityInt != null && capacityInt > 0 && selectedTypeId != null

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 24.dp)
            ) {
                Row(
                    modifier              = Modifier.padding(bottom = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(24.dp)
                    )
                    Text(
                        text       = "Редактировать помещение",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Tag,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text     = "Номер (не изменяется)",
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text       = place.number ?: "—",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value         = editTitle,
                    onValueChange = { editTitle = it },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text(text = "Название") },
                    leadingIcon   = { Icon(Icons.Outlined.Label, null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    enabled       = !isLoading
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value           = editCapacity,
                    onValueChange   = { editCapacity = it.filter { c -> c.isDigit() } },
                    modifier        = Modifier.fillMaxWidth(),
                    label           = { Text(text = "Вместимость *") },
                    leadingIcon     = { Icon(Icons.Outlined.EventSeat, null) },
                    singleLine      = true,
                    shape           = RoundedCornerShape(12.dp),
                    enabled         = !isLoading,
                    isError         = editCapacity.isNotEmpty() && (capacityInt == null || capacityInt <= 0),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(
                    expanded         = typeExpanded && !isLoading,
                    onExpandedChange = { if (!isLoading) typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = placeTypes.find { it.id == selectedTypeId }?.title ?: "",
                        onValueChange = {},
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly      = true,
                        label         = { Text(text = "Тип помещения *") },
                        leadingIcon   = { Icon(Icons.Outlined.Category, null) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        shape         = RoundedCornerShape(12.dp),
                        enabled       = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded         = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        placeTypes.forEach { type ->
                            DropdownMenuItem(
                                text    = { Text(text = type.title ?: "Тип ${type.id}") },
                                onClick = { selectedTypeId = type.id; typeExpanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = !isLoading
                    ) { Text(text = "Отмена") }
                    Button(
                        onClick  = {
                            if (isValid) onConfirm(
                                editTitle.trim().takeIf { it.isNotBlank() },
                                capacityInt!!,
                                selectedTypeId!!
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = isValid && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        else Text(text = "Сохранить", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Диалог создания помещения
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePlaceDialog(
    placeTypes: List<PlaceTypeDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (number: String, capacity: Int, typeId: Int, title: String?, photoUris: List<Uri>) -> Unit
) {
    var number         by remember { mutableStateOf("") }
    var capacity       by remember { mutableStateOf("") }
    var title          by remember { mutableStateOf("") }
    var selectedTypeId by remember { mutableStateOf<Int?>(null) }
    var typeExpanded   by remember { mutableStateOf(false) }
    var selectedUris   by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isSubmitting   by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) { if (!isLoading) isSubmitting = false }
    val capacityInt   = capacity.trim().toIntOrNull()
    val isValid       = number.trim().isNotBlank() && capacityInt != null && capacityInt > 0 && selectedTypeId != null
    val buttonEnabled = isValid && !isLoading && !isSubmitting
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> selectedUris = uris }

    Dialog(onDismissRequest = { if (!isLoading && !isSubmitting) onDismiss() }) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 24.dp)
            ) {
                Row(
                    modifier              = Modifier.padding(bottom = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.MeetingRoom, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text(text = "Новое помещение", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название") }, leadingIcon = { Icon(Icons.Outlined.Label, null) },
                    singleLine = true, shape = RoundedCornerShape(12.dp), enabled = !isLoading && !isSubmitting)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = number, onValueChange = { if (it.length <= 4) number = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Номер *") },
                    leadingIcon = { Icon(Icons.Outlined.Tag, null) }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), enabled = !isLoading && !isSubmitting,
                    supportingText = { Text("Макс. 4 символа", fontSize = 10.sp) })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = capacity, onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Вместимость *") },
                    leadingIcon = { Icon(Icons.Outlined.EventSeat, null) }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), enabled = !isLoading && !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = typeExpanded && !isLoading && !isSubmitting,
                    onExpandedChange = { if (!isLoading && !isSubmitting) typeExpanded = it }) {
                    OutlinedTextField(value = placeTypes.find { it.id == selectedTypeId }?.title ?: "",
                        onValueChange = {}, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true, label = { Text("Тип помещения *") },
                        leadingIcon = { Icon(Icons.Outlined.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading && !isSubmitting)
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        if (placeTypes.isEmpty()) DropdownMenuItem(text = { Text("Загрузка...") }, onClick = { typeExpanded = false })
                        else placeTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.title ?: "Тип ${type.id}") },
                                onClick = { selectedTypeId = type.id; typeExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("Фотографии", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        if (selectedUris.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text("${selectedUris.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    OutlinedButton(onClick = { photoPicker.launch("image/*") }, enabled = !isLoading && !isSubmitting,
                        shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Outlined.AddPhotoAlternate, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (selectedUris.isEmpty()) "Добавить" else "Изменить", fontSize = 12.sp)
                    }
                }
                if (selectedUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedUris.take(4).forEachIndexed { idx, uri ->
                            Box(modifier = Modifier.size(64.dp)) {
                                AsyncImage(model = uri, contentDescription = "Фото ${idx + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                                if (idx == 3 && selectedUris.size > 4) {
                                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                                        Text("+${selectedUris.size - 4}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = { selectedUris = emptyList() }, enabled = !isLoading && !isSubmitting, modifier = Modifier.padding(top = 2.dp)) {
                        Text("Очистить фото", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading && !isSubmitting) { Text("Отмена") }
                    Button(onClick = {
                        if (buttonEnabled) {
                            isSubmitting = true
                            onConfirm(number.trim(), capacityInt!!, selectedTypeId!!,
                                title.trim().takeIf { it.isNotBlank() }, selectedUris)
                        }
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = buttonEnabled) {
                        if (isLoading || isSubmitting) CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Создать", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Диалог добавления оборудования
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEquipmentDialog(
    equipmentTypes: List<EquipmentTypeDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, inventoryNumber: String, typeId: Int) -> Unit
) {
    var title           by remember { mutableStateOf("") }
    var inventoryNumber by remember { mutableStateOf("") }
    var selectedTypeId  by remember { mutableStateOf<Int?>(null) }
    var typeExpanded    by remember { mutableStateOf(false) }
    val isValid = title.trim().isNotBlank() && inventoryNumber.trim().isNotBlank() && selectedTypeId != null

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(all = 24.dp)) {
                Row(modifier = Modifier.padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Devices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text("Добавить оборудование", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название *") }, leadingIcon = { Icon(Icons.Outlined.Devices, null) },
                    singleLine = true, shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = inventoryNumber, onValueChange = { inventoryNumber = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Инвентарный номер *") }, leadingIcon = { Icon(Icons.Outlined.Tag, null) },
                    singleLine = true, shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(value = equipmentTypes.find { it.id == selectedTypeId }?.title ?: "",
                        onValueChange = {}, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true, label = { Text("Тип оборудования *") },
                        leadingIcon = { Icon(Icons.Outlined.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading)
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        if (equipmentTypes.isEmpty()) DropdownMenuItem(text = { Text("Загрузка...") }, onClick = { typeExpanded = false })
                        else equipmentTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.title ?: "Тип ${type.id}") },
                                onClick = { selectedTypeId = type.id; typeExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading) { Text("Отмена") }
                    Button(onClick = { if (isValid) onConfirm(title.trim(), inventoryNumber.trim(), selectedTypeId!!) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = isValid && !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Добавить", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Диалог подтверждения удаления ────────────────────────────────

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    description: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon             = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
        title            = { Text(text = title, fontWeight = FontWeight.Bold) },
        text             = { Text(text = description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton    = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape   = RoundedCornerShape(10.dp)
            ) { Text(text = "Удалить", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton    = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text(text = "Отмена") }
        }
    )
}