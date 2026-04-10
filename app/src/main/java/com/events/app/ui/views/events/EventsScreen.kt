package com.events.app.ui.views.events

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.ui.components.eventscards.UpcomingEventCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DISPLAY_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val ISO_FMT     = DateTimeFormatter.ISO_LOCAL_DATE_TIME

private fun millisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.toStartIso(): String = atStartOfDay().format(ISO_FMT)
private fun LocalDate.toEndIso(): String   = atTime(23, 59, 59).format(ISO_FMT)

private fun isoToDisplay(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try { LocalDate.parse(iso.substringBefore("T")).format(DISPLAY_FMT) }
    catch (_: Exception) { null }
}

private fun parseDisplayDate(input: String): LocalDate? {
    val cleaned = input.trim()
    return try { LocalDate.parse(cleaned, DISPLAY_FMT) }
    catch (_: DateTimeParseException) { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit
) {
    val displayedEvents by viewModel.displayedEvents.collectAsState()
    val hasMore         by viewModel.hasMore.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val error           by viewModel.error.collectAsState()
    val eventTypes      by viewModel.eventTypes.collectAsState()
    val eventFormats    by viewModel.eventFormats.collectAsState()
    val locations       by viewModel.locations.collectAsState()
    val places          by viewModel.places.collectAsState()
    val placesLoading   by viewModel.placesLoading.collectAsState()

    val vmStartIso   by viewModel.filterStartDate.collectAsState()
    val vmEndIso     by viewModel.filterEndDate.collectAsState()
    val vmTypeId     by viewModel.filterTypeId.collectAsState()
    val vmFormatId   by viewModel.filterFormatId.collectAsState()
    val vmLocationId by viewModel.filterLocationId.collectAsState()
    val vmPlaceId    by viewModel.filterPlaceId.collectAsState()

    val startDisplay = isoToDisplay(vmStartIso)
    val endDisplay   = isoToDisplay(vmEndIso)
    val dateActive   = vmStartIso != null || vmEndIso != null
    val anyActive    = dateActive || vmTypeId != null || vmFormatId != null ||
            vmLocationId != null || vmPlaceId != null

    val listState      = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isCloseToEnd    by remember { derivedStateOf { listState.isCloseToEnd() } }
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }

    LaunchedEffect(isCloseToEnd) {
        if (isCloseToEnd && hasMore) viewModel.loadMore()
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchJob   by remember { mutableStateOf<Job?>(null) }

    var showDateSheet       by remember { mutableStateOf(false) }
    var showTypeSheet       by remember { mutableStateOf(false) }
    var showFormatSheet     by remember { mutableStateOf(false) }
    var showAllFiltersSheet by remember { mutableStateOf(false) }

    val selectedTypeName   = eventTypes.find { it.id == vmTypeId }?.title
    val selectedFormatName = eventFormats.find { it.id == vmFormatId }?.title

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(500)
                        viewModel.setSearchText(q.trim().ifBlank { null })
                    }
                },
                placeholder  = { Text(text = "Поиск по названию или ID...") },
                leadingIcon  = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "Поиск",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchJob?.cancel()
                            viewModel.setSearchText(null)
                        }) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Очистить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(16.dp),
                modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = dateActive, onClick = { showDateSheet = true },
                    label = { Text(text = when {
                        startDisplay != null && endDisplay != null -> "$startDisplay – $endDisplay"
                        startDisplay != null -> "от $startDisplay"
                        endDisplay != null   -> "до $endDisplay"
                        else                 -> "Дата"
                    }, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    leadingIcon  = { Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(15.dp)) },
                    trailingIcon = if (dateActive) { {
                        IconButton(modifier = Modifier.size(15.dp), onClick = { viewModel.setDateFilter(null, null) }) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(11.dp)) }
                    } } else null,
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = vmTypeId != null, onClick = { showTypeSheet = true },
                    label = { Text(text = selectedTypeName ?: "Тип", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    leadingIcon  = { Icon(Icons.Outlined.Category, null, modifier = Modifier.size(15.dp)) },
                    trailingIcon = if (vmTypeId != null) { {
                        IconButton(modifier = Modifier.size(15.dp), onClick = { viewModel.setTypeFilter(null) }) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(11.dp)) }
                    } } else null,
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = vmFormatId != null, onClick = { showFormatSheet = true },
                    label = { Text(text = selectedFormatName ?: "Формат", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    leadingIcon  = { Icon(Icons.Outlined.Tv, null, modifier = Modifier.size(15.dp)) },
                    trailingIcon = if (vmFormatId != null) { {
                        IconButton(modifier = Modifier.size(15.dp), onClick = { viewModel.setFormatFilter(null) }) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(11.dp)) }
                    } } else null,
                    shape = RoundedCornerShape(12.dp)
                )
                AssistChip(
                    onClick     = { showAllFiltersSheet = true },
                    label       = { Text(text = "Фильтры", maxLines = 1) },
                    leadingIcon = { Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(15.dp)) },
                    colors      = if (anyActive) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                    ) else AssistChipDefaults.assistChipColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && displayedEvents.isEmpty() ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    error != null && displayedEvents.isEmpty() ->
                        Text(text = error ?: "", modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    displayedEvents.isEmpty() ->
                        Text(text = "Мероприятия не найдены", modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> {
                        LazyColumn(
                            state               = listState,
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = displayedEvents, key = { it.id }) { event ->
                                UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                            }
                            if (isLoading) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }

                if (showScrollToTop) {
                    SmallFloatingActionButton(
                        onClick        = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                        modifier       = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                        shape          = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Наверх",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (showDateSheet) {
            ModalBottomSheet(onDismissRequest = { showDateSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                DateRangeSheetContent(
                    sheetTitle      = "Дата мероприятия",
                    currentStartIso = vmStartIso, currentEndIso = vmEndIso,
                    onApply = { s, e -> viewModel.setDateFilter(s, e); showDateSheet = false },
                    onReset = { viewModel.setDateFilter(null, null); showDateSheet = false }
                )
            }
        }

        if (showTypeSheet) {
            ModalBottomSheet(onDismissRequest = { showTypeSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)) {
                GridPickSheetContent(title = "Тип мероприятия", icon = Icons.Outlined.Category,
                    items = eventTypes.map { Pair(it.id, it.title ?: "Тип ${it.id}") }, selectedId = vmTypeId,
                    onSelect = { id -> viewModel.setTypeFilter(id); showTypeSheet = false },
                    onReset  = { viewModel.setTypeFilter(null); showTypeSheet = false })
            }
        }

        if (showFormatSheet) {
            ModalBottomSheet(onDismissRequest = { showFormatSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)) {
                GridPickSheetContent(title = "Формат мероприятия", icon = Icons.Outlined.Tv,
                    items = eventFormats.map { Pair(it.id, it.title ?: "Формат ${it.id}") }, selectedId = vmFormatId,
                    onSelect = { id -> viewModel.setFormatFilter(id); showFormatSheet = false },
                    onReset  = { viewModel.setFormatFilter(null); showFormatSheet = false })
            }
        }

        if (showAllFiltersSheet) {
            ModalBottomSheet(onDismissRequest = { showAllFiltersSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                AllFiltersSheetContent(
                    eventTypes        = eventTypes,
                    eventFormats      = eventFormats,
                    locations         = locations,
                    places            = places,
                    placesLoading     = placesLoading,
                    currentStartIso   = vmStartIso, currentEndIso = vmEndIso,
                    currentTypeId     = vmTypeId, currentFormatId = vmFormatId,
                    currentLocationId = vmLocationId, currentPlaceId = vmPlaceId,
                    onLocationSelected = { locId -> viewModel.setLocationFilter(locId) },
                    onReset = { viewModel.resetAllFilters(); searchQuery = ""; showAllFiltersSheet = false },
                    onApply = { startIso, endIso, typeId, formatId, locationId, placeId ->
                        viewModel.applyAllFilters(
                            text = searchQuery.trim().ifBlank { null },
                            startDate = startIso, endDate = endIso,
                            createdAfter = null, createdBefore = null,
                            typeId = typeId, formatId = formatId,
                            locationId = locationId, placeId = placeId
                        )
                        showAllFiltersSheet = false
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка «Все фильтры»
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllFiltersSheetContent(
    eventTypes: List<EventTypeDto>, eventFormats: List<EventFormatDto>,
    locations: List<LocationDto>, places: List<PlaceDto>, placesLoading: Boolean,
    currentStartIso: String?, currentEndIso: String?,
    currentTypeId: Int?, currentFormatId: Int?, currentLocationId: Int?, currentPlaceId: Int?,
    onLocationSelected: (Int?) -> Unit, onReset: () -> Unit,
    onApply: (startIso: String?, endIso: String?, typeId: Int?, formatId: Int?, locationId: Int?, placeId: Int?) -> Unit
) {
    var typeSelected     by remember { mutableStateOf(currentTypeId) }
    var formatSelected   by remember { mutableStateOf(currentFormatId) }
    var locationSelected by remember { mutableStateOf(currentLocationId) }
    var placeSelected    by remember { mutableStateOf(currentPlaceId) }

    // Шторки выбора локации и помещения
    var showLocationPicker by remember { mutableStateOf(false) }
    var showPlacePicker    by remember { mutableStateOf(false) }

    var startMillis by remember { mutableStateOf(currentStartIso?.let {
        try { LocalDate.parse(it.substringBefore("T")).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        catch (_: Exception) { null }
    }) }
    var endMillis by remember { mutableStateOf(currentEndIso?.let {
        try { LocalDate.parse(it.substringBefore("T")).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        catch (_: Exception) { null }
    }) }
    var startText    by remember { mutableStateOf(isoToDisplay(currentStartIso) ?: "") }
    var endText      by remember { mutableStateOf(isoToDisplay(currentEndIso) ?: "") }
    var startError   by remember { mutableStateOf(false) }
    var endError     by remember { mutableStateOf(false) }
    var editingStart by remember { mutableStateOf(true) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (editingStart) startMillis else endMillis)

    LaunchedEffect(datePickerState.selectedDateMillis) {
        val selected = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        val date = millisToLocalDate(selected)
        if (editingStart) { startMillis = selected; startText = date.format(DISPLAY_FMT); startError = false }
        else              { endMillis   = selected; endText   = date.format(DISPLAY_FMT); endError   = false }
    }
    LaunchedEffect(editingStart) {
        datePickerState.selectedDateMillis = if (editingStart) startMillis else endMillis
    }

    val selectedLocationName = locations.find { it.id == locationSelected }?.title
    val selectedPlaceName    = places.find { it.id == placeSelected }?.let { place ->
        buildString {
            if (!place.title.isNullOrBlank()) append(place.title) else append("Помещение")
            if (!place.number.isNullOrBlank()) append(" №${place.number}")
        }
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

        // Заголовок
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Tune, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Все фильтры", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onReset) { Text("Сбросить все") }
        }

        // Дата
        FilterSectionHeader(icon = Icons.Outlined.CalendarMonth, title = "Дата мероприятия")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateInputField("Начало", startText, editingStart, startError, { editingStart = true },
                { input -> startText = formatDateInput(input); startError = false
                    val date = parseDisplayDate(startText)
                    if (date != null) { startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); datePickerState.selectedDateMillis = startMillis }
                    else if (startText.length == 10) { startError = true; startMillis = null } else startMillis = null },
                { startText = ""; startMillis = null; startError = false; if (editingStart) datePickerState.selectedDateMillis = null },
                Modifier.weight(1f))
            DateInputField("Конец", endText, !editingStart, endError, { editingStart = false },
                { input -> endText = formatDateInput(input); endError = false
                    val date = parseDisplayDate(endText)
                    if (date != null) { endMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); datePickerState.selectedDateMillis = endMillis }
                    else if (endText.length == 10) { endError = true; endMillis = null } else endMillis = null },
                { endText = ""; endMillis = null; endError = false; if (!editingStart) datePickerState.selectedDateMillis = null },
                Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth(),
            title = null, headline = null, showModeToggle = false,
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Тип
        FilterSectionHeader(icon = Icons.Outlined.Category, title = "Тип мероприятия")
        Spacer(modifier = Modifier.height(8.dp))
        FilterGrid(items = eventTypes.map { Pair(it.id, it.title ?: "Тип ${it.id}") }, selectedId = typeSelected,
            onSelect = { typeSelected = if (typeSelected == it) null else it })

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Формат
        FilterSectionHeader(icon = Icons.Outlined.Tv, title = "Формат")
        Spacer(modifier = Modifier.height(8.dp))
        FilterGrid(items = eventFormats.map { Pair(it.id, it.title ?: "Формат ${it.id}") }, selectedId = formatSelected,
            onSelect = { formatSelected = if (formatSelected == it) null else it })

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Локация — кнопка-пикер
        FilterSectionHeader(icon = Icons.Outlined.LocationOn, title = "Локация")
        Spacer(modifier = Modifier.height(8.dp))
        PickerButton(
            icon        = Icons.Outlined.LocationOn,
            label       = selectedLocationName ?: "Выбрать локацию",
            isSelected  = locationSelected != null,
            onClick     = { showLocationPicker = true },
            onClear     = { locationSelected = null; placeSelected = null; onLocationSelected(null) }
        )

        // Помещение — появляется только если выбрана локация
        if (locationSelected != null) {
            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionHeader(icon = Icons.Outlined.MeetingRoom, title = "Помещение")
            Spacer(modifier = Modifier.height(8.dp))
            if (placesLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                PickerButton(
                    icon       = Icons.Outlined.MeetingRoom,
                    label      = selectedPlaceName ?: "Выбрать помещение",
                    isSelected = placeSelected != null,
                    onClick    = { showPlacePicker = true },
                    onClear    = { placeSelected = null }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onApply(
                startMillis?.let { millisToLocalDate(it).toStartIso() },
                endMillis?.let   { millisToLocalDate(it).toEndIso() },
                typeSelected, formatSelected, locationSelected, placeSelected
            ) },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)
        ) { Text("Применить фильтры", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Шторка выбора локации
    if (showLocationPicker) {
        ModalBottomSheet(
            onDismissRequest = { showLocationPicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            PickerSheetContent(
                title    = "Выберите локацию",
                icon     = Icons.Outlined.LocationOn,
                items    = locations.map { Pair(it.id, it.title ?: "Локация ${it.id}") },
                selected = locationSelected,
                onSelect = { id ->
                    if (locationSelected != id) {
                        locationSelected = id
                        placeSelected    = null
                        onLocationSelected(id)
                    }
                    showLocationPicker = false
                }
            )
        }
    }

    // Шторка выбора помещения
    if (showPlacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showPlacePicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            PickerSheetContent(
                title    = "Выберите помещение",
                icon     = Icons.Outlined.MeetingRoom,
                items    = places.map { place ->
                    val label = buildString {
                        if (!place.title.isNullOrBlank()) append(place.title) else append("Помещение")
                        if (!place.number.isNullOrBlank()) append(" №${place.number}")
                    }
                    Pair(place.id, label)
                },
                selected = placeSelected,
                onSelect = { id -> placeSelected = id; showPlacePicker = false }
            )
        }
    }
}

// ── Кнопка-пикер ─────────────────────────────────────────────────

@Composable
private fun PickerButton(
    icon: ImageVector, label: String, isSelected: Boolean,
    onClick: () -> Unit, onClear: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(12.dp),
        color   = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null,
                tint     = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp))
            Text(
                text     = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color    = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Шторка списка с поиском ───────────────────────────────────────

@Composable
private fun PickerSheetContent(
    title: String, icon: ImageVector,
    items: List<Pair<Int, String>>, selected: Int?,
    onSelect: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) items
    else items.filter { it.second.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        // Поиск
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder   = { Text("Поиск...") },
            leadingIcon   = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
            trailingIcon  = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp)) } },
            singleLine    = true,
            shape         = RoundedCornerShape(12.dp),
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Список через LazyColumn с ограниченной высотой
            androidx.compose.foundation.lazy.LazyColumn(
                modifier            = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding      = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered) { (id, label) ->
                    val isSelected = selected == id
                    Surface(
                        onClick  = { onSelect(id) },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text       = label,
                                fontSize   = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier   = Modifier.weight(1f),
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка выбора диапазона дат
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSheetContent(
    sheetTitle: String, currentStartIso: String?, currentEndIso: String?,
    onApply: (startIso: String?, endIso: String?) -> Unit, onReset: () -> Unit
) {
    var startMillis by remember { mutableStateOf(currentStartIso?.let {
        try { LocalDate.parse(it.substringBefore("T")).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        catch (_: Exception) { null }
    }) }
    var endMillis by remember { mutableStateOf(currentEndIso?.let {
        try { LocalDate.parse(it.substringBefore("T")).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        catch (_: Exception) { null }
    }) }
    var startText    by remember { mutableStateOf(currentStartIso?.let { isoToDisplay(it) } ?: "") }
    var endText      by remember { mutableStateOf(currentEndIso?.let { isoToDisplay(it) } ?: "") }
    var startError   by remember { mutableStateOf(false) }
    var endError     by remember { mutableStateOf(false) }
    var editingStart by remember { mutableStateOf(true) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (editingStart) startMillis else endMillis)
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val selected = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        val date = millisToLocalDate(selected)
        if (editingStart) { startMillis = selected; startText = date.format(DISPLAY_FMT); startError = false }
        else              { endMillis   = selected; endText   = date.format(DISPLAY_FMT); endError   = false }
    }
    LaunchedEffect(editingStart) {
        datePickerState.selectedDateMillis = if (editingStart) startMillis else endMillis
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(text = sheetTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { startMillis = null; endMillis = null; startText = ""; endText = ""; onReset() }) {
                Text("Сбросить")
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateInputField("Начало", startText, editingStart, startError, { editingStart = true },
                { input -> startText = formatDateInput(input); startError = false
                    val date = parseDisplayDate(startText)
                    if (date != null) { startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); datePickerState.selectedDateMillis = startMillis }
                    else if (startText.length == 10) { startError = true; startMillis = null } else startMillis = null },
                { startText = ""; startMillis = null; startError = false; if (editingStart) datePickerState.selectedDateMillis = null },
                Modifier.weight(1f))
            DateInputField("Конец", endText, !editingStart, endError, { editingStart = false },
                { input -> endText = formatDateInput(input); endError = false
                    val date = parseDisplayDate(endText)
                    if (date != null) { endMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); datePickerState.selectedDateMillis = endMillis }
                    else if (endText.length == 10) { endError = true; endMillis = null } else endMillis = null },
                { endText = ""; endMillis = null; endError = false; if (!editingStart) datePickerState.selectedDateMillis = null },
                Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth(),
            title = null, headline = null, showModeToggle = false,
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
        Button(
            onClick  = { onApply(startMillis?.let { millisToLocalDate(it).toStartIso() }, endMillis?.let { millisToLocalDate(it).toEndIso() }) },
            enabled  = (startMillis != null || endMillis != null) && !startError && !endError,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) { Text("Применить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DateInputField(
    label: String, text: String, isActive: Boolean, isError: Boolean,
    onFocus: () -> Unit, onValueChange: (String) -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when { isError -> MaterialTheme.colorScheme.error; isActive -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outline },
        label = "dateFieldBorder")
    OutlinedTextField(
        value = text, onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("дд.мм.гггг", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        trailingIcon = { if (text.isNotEmpty()) IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
        isError = isError,
        supportingText = if (isError) { { Text("Неверный формат", color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } } else null,
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.onFocusChanged { if (it.isFocused) onFocus() }, shape = RoundedCornerShape(12.dp))
}

private fun formatDateInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    return buildString { digits.forEachIndexed { idx, c -> append(c); if (idx == 1 || idx == 3) append('.') } }
}

@Composable
private fun GridPickSheetContent(
    title: String, icon: ImageVector, items: List<Pair<Int, String>>,
    selectedId: Int?, onSelect: (Int) -> Unit, onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (selectedId != null) TextButton(onClick = onReset) { Text("Сбросить") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FilterGrid(items = items, selectedId = selectedId, onSelect = { onSelect(it) })
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FilterGrid(items: List<Pair<Int, String>>, selectedId: Int?, onSelect: (Int) -> Unit) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        return
    }
    items.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (id, label) ->
                FilterGridItem(label = label, selected = selectedId == id, onClick = { onSelect(id) }, modifier = Modifier.weight(1f))
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterGridItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                if (selected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false