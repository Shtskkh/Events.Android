package com.events.app.ui.views.events

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.events.app.ui.components.eventscards.UpcomingEventCard
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isCloseToEnd by remember { derivedStateOf { listState.isCloseToEnd() } }
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }

    LaunchedEffect(isCloseToEnd) {
        if (isCloseToEnd && hasMore) viewModel.loadMore()
    }

    // Состояния фильтров
    var searchQuery by remember { mutableStateOf("") }

    // Дата
    var dateStartLabel by remember { mutableStateOf<String?>(null) }
    var dateEndLabel by remember { mutableStateOf<String?>(null) }
    var showDateSheet by remember { mutableStateOf(false) }

    // Тип
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showTypeSheet by remember { mutableStateOf(false) }

    // Формат
    var selectedFormat by remember { mutableStateOf<String?>(null) }
    var showFormatSheet by remember { mutableStateOf(false) }

    // Все фильтры
    var showAllFiltersSheet by remember { mutableStateOf(false) }

    val dateActive = dateStartLabel != null || dateEndLabel != null
    val typeActive = selectedType != null
    val formatActive = selectedFormat != null

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Поиск ──────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск мероприятий...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Поиск",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            // ── Быстрые фильтры — одна строка ─────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Дата
                FilterChip(
                    selected = dateActive,
                    onClick = { showDateSheet = true },
                    label = {
                        Text(
                            if (dateActive) buildString {
                                if (dateStartLabel != null) append(dateStartLabel)
                                if (dateStartLabel != null && dateEndLabel != null) append(" – ")
                                if (dateEndLabel != null) append(dateEndLabel)
                            } else "Дата",
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // Тип
                FilterChip(
                    selected = typeActive,
                    onClick = { showTypeSheet = true },
                    label = { Text(selectedType ?: "Тип", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Category, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // Формат
                FilterChip(
                    selected = formatActive,
                    onClick = { showFormatSheet = true },
                    label = { Text(selectedFormat ?: "Формат", maxLines = 1) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Tv, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // Все фильтры
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
                            text = "Мероприятий пока нет",
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
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (hasMore) {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }

                        if (showScrollToTop) {
                            FloatingActionButton(
                                onClick = {
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 15.dp, bottom = 80.dp)
                                    .alpha(0.7f),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Вверх")
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
                    startLabel = dateStartLabel,
                    endLabel = dateEndLabel,
                    onApply = { start, end ->
                        dateStartLabel = start
                        dateEndLabel = end
                        showDateSheet = false
                    },
                    onReset = {
                        dateStartLabel = null
                        dateEndLabel = null
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
                QuickPickSheetContent(
                    title = "Тип мероприятия",
                    options = listOf("Конференция", "Семинар", "Вебинар", "Митап", "Воркшоп"),
                    selected = selectedType,
                    onSelect = { selectedType = it; showTypeSheet = false },
                    onReset = { selectedType = null; showTypeSheet = false }
                )
            }
        }

        // ── Шторка: Формат ────────────────────────────────────────
        if (showFormatSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFormatSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                QuickPickSheetContent(
                    title = "Формат мероприятия",
                    options = listOf("Офлайн", "Онлайн", "Гибридный"),
                    selected = selectedFormat,
                    onSelect = { selectedFormat = it; showFormatSheet = false },
                    onReset = { selectedFormat = null; showFormatSheet = false }
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
                    onReset = {
                        dateStartLabel = null
                        dateEndLabel = null
                        selectedType = null
                        selectedFormat = null
                        showAllFiltersSheet = false
                    },
                    onApply = { showAllFiltersSheet = false }
                )
            }
        }
    }
}

// ── Шторка выбора диапазона дат ───────────────────────────────────
@Composable
private fun DateFilterSheetContent(
    startLabel: String?,
    endLabel: String?,
    onApply: (String?, String?) -> Unit,
    onReset: () -> Unit
) {
    var start by remember { mutableStateOf(startLabel ?: "") }
    var end by remember { mutableStateOf(endLabel ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Диапазон дат", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onReset) { Text("Сбросить") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = start,
            onValueChange = { start = it },
            label = { Text("Начало (дд.мм.гггг)") },
            leadingIcon = {
                Icon(Icons.Outlined.CalendarMonth, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = end,
            onValueChange = { end = it },
            label = { Text("Конец (дд.мм.гггг)") },
            leadingIcon = {
                Icon(Icons.Outlined.CalendarMonth, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onApply(
                    start.ifBlank { null },
                    end.ifBlank { null }
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Применить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Шторка быстрого выбора одного значения (тип / формат) ─────────
@Composable
private fun QuickPickSheetContent(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (selected != null) {
                TextButton(onClick = onReset) { Text("Сбросить") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Шторка «Все фильтры» ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllFiltersSheetContent(
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    var dateStart by remember { mutableStateOf("") }
    var dateEnd by remember { mutableStateOf("") }
    var typeSelected by remember { mutableStateOf<String?>(null) }
    var formatSelected by remember { mutableStateOf<String?>(null) }
    var registrationRequired by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Все фильтры", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onReset) { Text("Сбросить все") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Дата
        Text("Диапазон дат", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dateStart,
                onValueChange = { dateStart = it },
                label = { Text("С") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dateEnd,
                onValueChange = { dateEnd = it },
                label = { Text("По") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Тип
        Text("Тип мероприятия", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Конференция", "Семинар", "Вебинар", "Митап", "Воркшоп").forEach { type ->
                FilterChip(
                    selected = typeSelected == type,
                    onClick = { typeSelected = if (typeSelected == type) null else type },
                    label = { Text(type) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Category, null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Формат
        Text("Формат", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Офлайн", "Онлайн", "Гибридный").forEach { format ->
                FilterChip(
                    selected = formatSelected == format,
                    onClick = { formatSelected = if (formatSelected == format) null else format },
                    label = { Text(format) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Регистрация
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Требуется регистрация",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = registrationRequired,
                onCheckedChange = { registrationRequired = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Применить фильтры", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.isCloseToEnd(threshold: Int = 3) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.let { last ->
        last.index >= layoutInfo.totalItemsCount - threshold - 1
    } ?: false