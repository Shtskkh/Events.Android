package com.events.app.ui.views.events

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.ui.components.eventscards.UpcomingEventCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

// ═══════════════════════════════════════════════════════════════════
// EventsScreen
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit,
) {
    val displayedEvents by viewModel.displayedEvents.collectAsState()
    val hasMore         by viewModel.hasMore.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val error           by viewModel.error.collectAsState()
    val eventTypes      by viewModel.eventTypes.collectAsState()
    val eventFormats    by viewModel.eventFormats.collectAsState()

    val vmStartIso  by viewModel.filterStartDate.collectAsState()
    val vmEndIso    by viewModel.filterEndDate.collectAsState()
    val vmTypeId    by viewModel.filterTypeId.collectAsState()
    val vmFormatId  by viewModel.filterFormatId.collectAsState()

    val startDisplay = isoToDisplay(vmStartIso)
    val endDisplay   = isoToDisplay(vmEndIso)
    val dateActive   = vmStartIso != null || vmEndIso != null
    val anyActive    = dateActive || vmTypeId != null || vmFormatId != null

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

            // ── Поиск ────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(500)
                        viewModel.setSearchText(q.trim().ifBlank { null })
                    }
                },
                placeholder  = { Text("Поиск по названию или ID...") },
                leadingIcon  = { Icon(Icons.Outlined.Search, "Поиск",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchJob?.cancel()
                            viewModel.setSearchText(null)
                        }) { Icon(Icons.Outlined.Close, "Очистить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(16.dp),
                modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor   = MaterialTheme.colorScheme.primary
                )
            )

            // ── Быстрые фильтры ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = dateActive,
                    onClick  = { showDateSheet = true },
                    label = { Text(when {
                        startDisplay != null && endDisplay != null -> "$startDisplay – $endDisplay"
                        startDisplay != null -> "от $startDisplay"
                        endDisplay != null   -> "до $endDisplay"
                        else -> "Дата"
                    }, maxLines = 1) },
                    leadingIcon  = { Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (dateActive) {
                        { IconButton(modifier = Modifier.size(16.dp),
                            onClick = { viewModel.setDateFilter(null, null) }) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(12.dp)) } }
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = vmTypeId != null,
                    onClick  = { showTypeSheet = true },
                    label    = { Text(selectedTypeName ?: "Тип", maxLines = 1) },
                    leadingIcon  = { Icon(Icons.Outlined.Category, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (vmTypeId != null) {
                        { IconButton(modifier = Modifier.size(16.dp),
                            onClick = { viewModel.setTypeFilter(null) }) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(12.dp)) } }
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = vmFormatId != null,
                    onClick  = { showFormatSheet = true },
                    label    = { Text(selectedFormatName ?: "Формат", maxLines = 1) },
                    leadingIcon  = { Icon(Icons.Outlined.Tv, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (vmFormatId != null) {
                        { IconButton(modifier = Modifier.size(16.dp),
                            onClick = { viewModel.setFormatFilter(null) }) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(12.dp)) } }
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
                AssistChip(
                    onClick     = { showAllFiltersSheet = true },
                    label       = { Text("Все фильтры", maxLines = 1) },
                    leadingIcon = { Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(16.dp)) },
                    colors = if (anyActive) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                    ) else AssistChipDefaults.assistChipColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Список / состояния ────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && displayedEvents.isEmpty() ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    error != null && displayedEvents.isEmpty() ->
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text      = error ?: "Неизвестная ошибка",
                                modifier  = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                textAlign = TextAlign.Center,
                                color     = MaterialTheme.colorScheme.error,
                                fontSize  = 15.sp
                            )
                        }

                    displayedEvents.isEmpty() && !isLoading ->
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier            = Modifier.padding(horizontal = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Outlined.SearchOff, null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Мероприятий не найдено",
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize  = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier.fillMaxWidth()
                                )
                                if (anyActive) {
                                    Spacer(Modifier.height(12.dp))
                                    TextButton(onClick = {
                                        viewModel.resetAllFilters()
                                        searchQuery = ""
                                    }) {
                                        Text("Сбросить фильтры")
                                    }
                                }
                            }
                        }

                    else -> {
                        LazyColumn(state = listState,
                            modifier = Modifier.fillMaxSize().padding(bottom = 48.dp)) {
                            items(displayedEvents) { event ->
                                UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                                Spacer(Modifier.height(16.dp))
                            }
                            if (hasMore) {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally))
                                }
                            }
                        }
                        if (showScrollToTop) {
                            FloatingActionButton(
                                onClick        = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                                modifier       = Modifier.align(Alignment.BottomEnd)
                                    .padding(end = 15.dp, bottom = 80.dp).alpha(0.7f),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor   = Color.White,
                                elevation      = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                            ) { Icon(Icons.Default.ArrowUpward, "Вверх") }
                        }
                    }
                }
            }
        }

        // ── Шторки ──────────────────────────────────────────────

        if (showDateSheet) {
            ModalBottomSheet(onDismissRequest = { showDateSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                DateRangeSheetContent(
                    currentStartIso = vmStartIso, currentEndIso = vmEndIso,
                    onApply = { s, e -> viewModel.setDateFilter(s, e); showDateSheet = false },
                    onReset = { viewModel.setDateFilter(null, null); showDateSheet = false }
                )
            }
        }

        if (showTypeSheet) {
            ModalBottomSheet(onDismissRequest = { showTypeSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)) {
                GridPickSheetContent(
                    title = "Тип мероприятия", icon = Icons.Outlined.Category,
                    items = eventTypes.map { Pair(it.id, it.title ?: "Тип ${it.id}") },
                    selectedId = vmTypeId,
                    onSelect = { id -> viewModel.setTypeFilter(id); showTypeSheet = false },
                    onReset  = { viewModel.setTypeFilter(null); showTypeSheet = false }
                )
            }
        }

        if (showFormatSheet) {
            ModalBottomSheet(onDismissRequest = { showFormatSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)) {
                GridPickSheetContent(
                    title = "Формат мероприятия", icon = Icons.Outlined.Tv,
                    items = eventFormats.map { Pair(it.id, it.title ?: "Формат ${it.id}") },
                    selectedId = vmFormatId,
                    onSelect = { id -> viewModel.setFormatFilter(id); showFormatSheet = false },
                    onReset  = { viewModel.setFormatFilter(null); showFormatSheet = false }
                )
            }
        }

        if (showAllFiltersSheet) {
            ModalBottomSheet(onDismissRequest = { showAllFiltersSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                AllFiltersSheetContent(
                    eventTypes      = eventTypes,
                    eventFormats    = eventFormats,
                    currentStartIso = vmStartIso,
                    currentEndIso   = vmEndIso,
                    currentTypeId   = vmTypeId,
                    currentFormatId = vmFormatId,
                    onReset = {
                        viewModel.resetAllFilters()
                        searchQuery = ""
                        showAllFiltersSheet = false
                    },
                    onApply = { startIso, endIso, typeId, formatId ->
                        viewModel.applyAllFilters(
                            text      = searchQuery.trim().ifBlank { null },
                            startDate = startIso,
                            endDate   = endIso,
                            typeId    = typeId,
                            formatId  = formatId
                        )
                        showAllFiltersSheet = false
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка диапазона дат
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSheetContent(
    currentStartIso: String?, currentEndIso: String?,
    onApply: (startIso: String?, endIso: String?) -> Unit,
    onReset: () -> Unit
) {
    val initialStartMs = currentStartIso?.let {
        try { LocalDate.parse(it.substringBefore("T"))
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() } catch (_: Exception) { null }
    }
    val initialEndMs = currentEndIso?.let {
        try { LocalDate.parse(it.substringBefore("T"))
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() } catch (_: Exception) { null }
    }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMs,
        initialSelectedEndDateMillis   = initialEndMs
    )
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.CalendarMonth, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Диапазон дат", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onReset) { Text("Сбросить") }
        }
        DateRangePicker(
            state    = dateRangePickerState,
            modifier = Modifier.fillMaxWidth().height(420.dp),
            title    = null,
            headline = {
                val startMs  = dateRangePickerState.selectedStartDateMillis
                val endMs    = dateRangePickerState.selectedEndDateMillis
                val startStr = startMs?.let { millisToLocalDate(it).format(DISPLAY_FMT) }
                val endStr   = endMs?.let   { millisToLocalDate(it).format(DISPLAY_FMT) }
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(startStr ?: "Начало", fontSize = 14.sp,
                        color = if (startStr != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (endStr != null) {
                        Text("–", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(endStr, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val startMs = dateRangePickerState.selectedStartDateMillis
                val endMs   = dateRangePickerState.selectedEndDateMillis
                onApply(
                    startMs?.let { millisToLocalDate(it).toStartIso() },
                    endMs?.let   { millisToLocalDate(it).toEndIso() }
                )
            },
            enabled  = dateRangePickerState.selectedStartDateMillis != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) { Text("Применить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(16.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка сетки (тип / формат)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun GridPickSheetContent(
    title: String, icon: ImageVector,
    items: List<Pair<Int, String>>, selectedId: Int?,
    onSelect: (Int) -> Unit, onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (selectedId != null) TextButton(onClick = onReset) { Text("Сбросить") }
        }
        Spacer(Modifier.height(8.dp))
        FilterGrid(items = items, selectedId = selectedId, onSelect = { onSelect(it) })
        Spacer(Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка «Все фильтры» — без секции локаций
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllFiltersSheetContent(
    eventTypes: List<EventTypeDto>,
    eventFormats: List<EventFormatDto>,
    currentStartIso: String?,
    currentEndIso: String?,
    currentTypeId: Int?,
    currentFormatId: Int?,
    onReset: () -> Unit,
    onApply: (startIso: String?, endIso: String?, typeId: Int?, formatId: Int?) -> Unit
) {
    var typeSelected   by remember { mutableStateOf(currentTypeId) }
    var formatSelected by remember { mutableStateOf(currentFormatId) }

    val initialStartMs = currentStartIso?.let {
        try { LocalDate.parse(it.substringBefore("T"))
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() } catch (_: Exception) { null }
    }
    val initialEndMs = currentEndIso?.let {
        try { LocalDate.parse(it.substringBefore("T"))
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() } catch (_: Exception) { null }
    }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMs,
        initialSelectedEndDateMillis   = initialEndMs
    )

    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
    ) {
        // ── Заголовок ─────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Tune, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Text("Все фильтры", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onReset) { Text("Сбросить все") }
        }

        // ── Диапазон дат ──────────────────────────────────────────
        FilterSectionHeader(Icons.Outlined.CalendarMonth, "Диапазон дат")
        Spacer(Modifier.height(8.dp))
        DateRangePicker(
            state    = dateRangePickerState,
            modifier = Modifier.fillMaxWidth().height(400.dp),
            title    = null,
            headline = {
                val startMs  = dateRangePickerState.selectedStartDateMillis
                val endMs    = dateRangePickerState.selectedEndDateMillis
                val startStr = startMs?.let { millisToLocalDate(it).format(DISPLAY_FMT) }
                val endStr   = endMs?.let   { millisToLocalDate(it).format(DISPLAY_FMT) }
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(startStr ?: "Начало", fontSize = 14.sp,
                        color = if (startStr != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (endStr != null) {
                        Text("–", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(endStr, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // ── Тип мероприятия ───────────────────────────────────────
        FilterSectionHeader(Icons.Outlined.Category, "Тип мероприятия")
        Spacer(Modifier.height(8.dp))
        FilterGrid(
            items      = eventTypes.map { Pair(it.id, it.title ?: "Тип ${it.id}") },
            selectedId = typeSelected,
            onSelect   = { typeSelected = if (typeSelected == it) null else it }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // ── Формат ────────────────────────────────────────────────
        FilterSectionHeader(Icons.Outlined.Tv, "Формат")
        Spacer(Modifier.height(8.dp))
        FilterGrid(
            items      = eventFormats.map { Pair(it.id, it.title ?: "Формат ${it.id}") },
            selectedId = formatSelected,
            onSelect   = { formatSelected = if (formatSelected == it) null else it }
        )

        Spacer(Modifier.height(24.dp))

        // ── Кнопка применить ──────────────────────────────────────
        Button(
            onClick = {
                val startMs  = dateRangePickerState.selectedStartDateMillis
                val endMs    = dateRangePickerState.selectedEndDateMillis
                onApply(
                    startMs?.let { millisToLocalDate(it).toStartIso() },
                    endMs?.let   { millisToLocalDate(it).toEndIso() },
                    typeSelected,
                    formatSelected
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Text("Применить фильтры", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ── Сетка элементов фильтра ───────────────────────────────────────

@Composable
private fun FilterGrid(
    items: List<Pair<Int, String>>,
    selectedId: Int?,
    onSelect: (Int) -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        return
    }
    items.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (id, label) ->
                FilterGridItem(
                    label    = label,
                    selected = selectedId == id,
                    onClick  = { onSelect(id) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterGridItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text      = label,
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color     = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selected) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.CheckCircle, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false