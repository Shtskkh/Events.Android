package com.events.app.ui.views.events

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit,
) {
    val displayedEvents by viewModel.displayedEvents.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val eventTypes by viewModel.eventTypes.collectAsState()
    val eventFormats by viewModel.eventFormats.collectAsState()

    val vmStartDate by viewModel.filterStartDate.collectAsState()
    val vmEndDate by viewModel.filterEndDate.collectAsState()
    val vmTypeId by viewModel.filterTypeId.collectAsState()
    val vmFormatId by viewModel.filterFormatId.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isCloseToEnd by remember { derivedStateOf { listState.isCloseToEnd() } }
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }

    LaunchedEffect(isCloseToEnd) {
        if (isCloseToEnd && hasMore) viewModel.loadMore()
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var showDateSheet by remember { mutableStateOf(false) }
    var showTypeSheet by remember { mutableStateOf(false) }
    var showFormatSheet by remember { mutableStateOf(false) }
    var showAllFiltersSheet by remember { mutableStateOf(false) }

    val dateActive = vmStartDate != null || vmEndDate != null
    val selectedTypeName = eventTypes.find { it.id == vmTypeId }?.title
    val selectedFormatName = eventFormats.find { it.id == vmFormatId }?.title

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Поиск с debounce 500ms ─────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(500)
                        // trim() убирает пробелы, ifBlank даёт null — Retrofit не добавит параметр
                        viewModel.setSearchText(query.trim().ifBlank { null })
                    }
                },
                placeholder = { Text("Поиск мероприятий...") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, "Поиск", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                // Кнопка очистки поля
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchJob?.cancel()
                            viewModel.setSearchText(null)
                        }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Очистить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // ── Быстрые фильтры ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = dateActive,
                    onClick = { showDateSheet = true },
                    label = {
                        Text(
                            if (dateActive) buildString {
                                if (vmStartDate != null) append(vmStartDate)
                                if (vmStartDate != null && vmEndDate != null) append(" – ")
                                if (vmEndDate != null) append(vmEndDate)
                            } else "Дата",
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = vmTypeId != null,
                    onClick = { showTypeSheet = true },
                    label = { Text(selectedTypeName ?: "Тип", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Category, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = vmFormatId != null,
                    onClick = { showFormatSheet = true },
                    label = { Text(selectedFormatName ?: "Формат", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Tv, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                AssistChip(
                    onClick = { showAllFiltersSheet = true },
                    label = { Text("Все фильтры", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Список мероприятий ─────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && displayedEvents.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    error != null && displayedEvents.isEmpty() -> {
                        Text(
                            text = error ?: "Неизвестная ошибка",
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    displayedEvents.isEmpty() && !isLoading -> {
                        Text(
                            text = "Мероприятий не найдено",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(bottom = 48.dp)
                        ) {
                            items(displayedEvents) { event ->
                                UpcomingEventCard(event = event, onClick = { onEventClick(event.id) })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (hasMore) {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                        if (showScrollToTop) {
                            FloatingActionButton(
                                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 15.dp, bottom = 80.dp)
                                    .alpha(0.7f),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, "Вверх")
                            }
                        }
                    }
                }
            }
        }

        // ── Шторка: Дата ──────────────────────────────────────────
        if (showDateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDateSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                DateFilterSheetContent(
                    startLabel = vmStartDate,
                    endLabel = vmEndDate,
                    onApply = { s, e ->
                        viewModel.setDateFilter(s, e)
                        showDateSheet = false
                    },
                    onReset = {
                        viewModel.setDateFilter(null, null)
                        showDateSheet = false
                    }
                )
            }
        }

        // ── Шторка: Тип ───────────────────────────────────────────
        if (showTypeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTypeSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                DtoGridPickSheetContent(
                    title = "Тип мероприятия",
                    icon = Icons.Outlined.Category,
                    options = eventTypes,
                    selectedId = vmTypeId,
                    onSelect = { id ->
                        viewModel.setTypeFilter(id)
                        showTypeSheet = false
                    },
                    onReset = {
                        viewModel.setTypeFilter(null)
                        showTypeSheet = false
                    }
                )
            }
        }

        // ── Шторка: Формат ────────────────────────────────────────
        if (showFormatSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFormatSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                DtoGridPickSheetContent(
                    title = "Формат мероприятия",
                    icon = Icons.Outlined.Tv,
                    options = eventFormats,
                    selectedId = vmFormatId,
                    onSelect = { id ->
                        viewModel.setFormatFilter(id)
                        showFormatSheet = false
                    },
                    onReset = {
                        viewModel.setFormatFilter(null)
                        showFormatSheet = false
                    }
                )
            }
        }

        // ── Шторка: Все фильтры ───────────────────────────────────
        if (showAllFiltersSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAllFiltersSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                AllFiltersSheetContent(
                    eventTypes = eventTypes,
                    eventFormats = eventFormats,
                    initialStartDate = vmStartDate,
                    initialEndDate = vmEndDate,
                    initialTypeId = vmTypeId,
                    initialFormatId = vmFormatId,
                    onReset = {
                        viewModel.resetAllFilters()
                        searchQuery = ""
                        showAllFiltersSheet = false
                    },
                    onApply = { startDate, endDate, typeId, formatId ->
                        viewModel.applyAllFilters(
                            text = searchQuery.trim().ifBlank { null },
                            startDate = startDate,
                            endDate = endDate,
                            typeId = typeId,
                            formatId = formatId
                        )
                        showAllFiltersSheet = false
                    }
                )
            }
        }
    }
}

// ── Шторка диапазона дат ──────────────────────────────────────────
@Composable
private fun DateFilterSheetContent(
    startLabel: String?,
    endLabel: String?,
    onApply: (String?, String?) -> Unit,
    onReset: () -> Unit
) {
    var start by remember { mutableStateOf(startLabel ?: "") }
    var end by remember { mutableStateOf(endLabel ?: "") }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                Text("Диапазон дат", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = onReset) { Text("Сбросить") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = start,
            onValueChange = { start = it },
            label = { Text("Начало периода") },
            placeholder = { Text("дд.мм.гггг") },
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = end,
            onValueChange = { end = it },
            label = { Text("Конец периода") },
            placeholder = { Text("дд.мм.гггг") },
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onApply(start.ifBlank { null }, end.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Применить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Универсальная шторка сетки ────────────────────────────────────
@Composable
private fun DtoGridPickSheetContent(
    title: String,
    icon: ImageVector,
    options: List<Any>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onReset: () -> Unit
) {
    val items = options.mapNotNull { dto ->
        when (dto) {
            is EventTypeDto -> Pair(dto.id, dto.title ?: "Тип ${dto.id}")
            is EventFormatDto -> Pair(dto.id, dto.title ?: "Формат ${dto.id}")
            else -> null
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            if (selectedId != null) {
                TextButton(onClick = onReset) { Text("Сбросить") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            items.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (id, label) ->
                        FilterGridItem(
                            label = label,
                            selected = selectedId == id,
                            onClick = { onSelect(id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Шторка «Все фильтры» ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllFiltersSheetContent(
    eventTypes: List<EventTypeDto>,
    eventFormats: List<EventFormatDto>,
    initialStartDate: String?,
    initialEndDate: String?,
    initialTypeId: Int?,
    initialFormatId: Int?,
    onReset: () -> Unit,
    onApply: (startDate: String?, endDate: String?, typeId: Int?, formatId: Int?) -> Unit
) {
    var dateStart by remember { mutableStateOf(initialStartDate ?: "") }
    var dateEnd by remember { mutableStateOf(initialEndDate ?: "") }
    var typeSelected by remember { mutableStateOf(initialTypeId) }
    var formatSelected by remember { mutableStateOf(initialFormatId) }
    var statusSelected by remember { mutableStateOf<String?>(null) }
    var registrationRequired by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Tune, null, tint = MaterialTheme.colorScheme.primary)
                Text("Все фильтры", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = onReset) { Text("Сбросить все") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionHeader(Icons.Outlined.CalendarMonth, "Диапазон дат")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dateStart, onValueChange = { dateStart = it },
            label = { Text("Начало периода") }, placeholder = { Text("дд.мм.гггг") },
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp)) },
            singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = dateEnd, onValueChange = { dateEnd = it },
            label = { Text("Конец периода") }, placeholder = { Text("дд.мм.гггг") },
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp)) },
            singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionHeader(Icons.Outlined.Category, "Тип мероприятия")
        Spacer(modifier = Modifier.height(8.dp))
        if (eventTypes.isEmpty()) {
            Text("Загрузка...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        } else {
            eventTypes.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { type ->
                        FilterGridItem(
                            label = type.title ?: "Тип ${type.id}",
                            selected = typeSelected == type.id,
                            onClick = { typeSelected = if (typeSelected == type.id) null else type.id },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionHeader(Icons.Outlined.Tv, "Формат")
        Spacer(modifier = Modifier.height(8.dp))
        if (eventFormats.isEmpty()) {
            Text("Загрузка...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        } else {
            eventFormats.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { format ->
                        FilterGridItem(
                            label = format.title ?: "Формат ${format.id}",
                            selected = formatSelected == format.id,
                            onClick = { formatSelected = if (formatSelected == format.id) null else format.id },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionHeader(Icons.Outlined.CheckCircle, "Статус")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Активные", "Завершённые").forEach { status ->
                FilterGridItem(
                    label = status, selected = statusSelected == status,
                    onClick = { statusSelected = if (statusSelected == status) null else status },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        FilterSectionHeader(Icons.Outlined.AppRegistration, "Регистрация")
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Требуется регистрация", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = registrationRequired, onCheckedChange = { registrationRequired = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onApply(
                    dateStart.ifBlank { null },
                    dateEnd.ifBlank { null },
                    typeSelected,
                    formatSelected
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Применить фильтры", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FilterSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterGridItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selected) {
                Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false