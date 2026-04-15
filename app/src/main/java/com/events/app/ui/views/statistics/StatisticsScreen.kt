package com.events.app.ui.views.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

// ── Палитра ───────────────────────────────────────────────────────
private val ColorBlue   = Color(0xFF3B5BDB)
private val ColorGreen  = Color(0xFF22C55E)
private val ColorGray   = Color(0xFF9E9E9E)
private val ColorAmber  = Color(0xFFF59E0B)
private val ColorRed    = Color(0xFFEF4444)
private val ColorViolet = Color(0xFF8B5CF6)
private val ColorCyan   = Color(0xFF06B6D4)
private val ColorTeal   = Color(0xFF10B981)
private val ColorOrange = Color(0xFFFF6B35)
private val MultiLineColors = listOf(ColorCyan, ColorGreen, ColorOrange)

private enum class StatTab(val label: String, val icon: ImageVector) {
    OVERVIEW ("Обзор",          Icons.Outlined.Dashboard),
    TYPES    ("Типы",           Icons.Outlined.Category),
    PLAN     ("Локации", Icons.Outlined.Map)
}

private val ScreenPalette = listOf(
    0xFF3B5BDB, 0xFF22C55E, 0xFFEF4444, 0xFFF59E0B,
    0xFF8B5CF6, 0xFF06B6D4, 0xFFEC4899, 0xFF10B981,
    0xFFFF6B35, 0xFF6366F1
)

// ═══════════════════════════════════════════════════════════════════
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()
    val stats     by viewModel.stats.collectAsState()
    val allEvents            by viewModel.allEvents.collectAsState()
    val selectedConversion   by viewModel.selectedEventConversion.collectAsState()
    val conversionLoading    by viewModel.conversionLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.ErrorOutline, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                OutlinedButton(onClick = { viewModel.loadStats() }, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.Refresh, null)
                    Spacer(Modifier.width(8.dp)); Text("Повторить")
                }
            }
            stats != null && stats!!.total == 0 -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.BarChart, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                    modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Нет данных", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f), fontSize = 15.sp)
            }
            stats != null -> StatisticsContent(
                stats              = stats!!,
                allEvents          = allEvents,
                selectedConversion = selectedConversion,
                conversionLoading  = conversionLoading,
                onEventClick       = onEventClick,
                onSelectEvent      = { viewModel.loadEventConversion(it) },
                onClearEvent       = { viewModel.clearEventConversion() }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Контент с вкладками
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsContent(
    stats: StatisticsData,
    allEvents: List<com.events.app.domain.models.events.Event>,
    selectedConversion: EventConversion?,
    conversionLoading: Boolean,
    onEventClick: (String) -> Unit,
    onSelectEvent: (String) -> Unit,
    onClearEvent: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(StatTab.OVERVIEW) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Вкладки ───────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor   = MaterialTheme.colorScheme.background,
            contentColor     = MaterialTheme.colorScheme.primary,
            divider          = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.15f)) }
        ) {
            StatTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick  = { selectedTab = tab },
                    icon = { Icon(tab.icon, null, modifier = Modifier.size(15.dp)) },
                    text = { Text(tab.label, fontSize = 11.sp, maxLines = 1) }
                )
            }
        }

        // ── Содержимое вкладки ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            when (selectedTab) {
                StatTab.OVERVIEW -> OverviewTab(
                    stats              = stats,
                    allEvents          = allEvents,
                    selectedConversion = selectedConversion,
                    conversionLoading  = conversionLoading,
                    onEventClick       = onEventClick,
                    onSelectEvent      = onSelectEvent,
                    onClearEvent       = onClearEvent
                )
                StatTab.TYPES -> TypesTab(allEvents)
                StatTab.PLAN  -> LocationTimeTab(stats, allEvents, onEventClick)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вкладка 1: Обзор
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewTab(
    stats: StatisticsData,
    allEvents: List<com.events.app.domain.models.events.Event>,
    selectedConversion: EventConversion?,
    conversionLoading: Boolean,
    onEventClick: (String) -> Unit,
    onSelectEvent: (String) -> Unit,
    onClearEvent: () -> Unit
) {
    // ── 1. Счётчики мероприятий ──────────────────────────────────────
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile("Всего",       stats.total.toString(),    Icons.Outlined.Event,       ColorBlue,  Modifier.weight(1f))
        MetricTile("Предстоящие", stats.upcoming.toString(), Icons.Outlined.Upcoming,    ColorGreen, Modifier.weight(1f))
        MetricTile("Завершены",   stats.finished.toString(), Icons.Outlined.CheckCircle, ColorGray,  Modifier.weight(1f))
    }
    Spacer(Modifier.height(14.dp))

    // ── 2. Регистрация по месяцам ────────────────────────────────────
    RegistrationByMonthCard(allEvents)
    Spacer(Modifier.height(14.dp))

    // ── 3. Конверсия просмотры → регистрации ────────────────────────
    ConversionCard(
        globalViews        = stats.totalViews,
        globalParticipants = stats.totalParticipants,
        globalRate         = stats.conversionRate,
        allEvents          = allEvents,
        selectedConversion = selectedConversion,
        conversionLoading  = conversionLoading,
        onSelectEvent      = onSelectEvent,
        onClearEvent       = onClearEvent
    )
}

// ═══════════════════════════════════════════════════════════════════
// Карточка: регистрация по месяцам
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationByMonthCard(
    allEvents: List<com.events.app.domain.models.events.Event>
) {
    var periodMonths by remember { mutableIntStateOf(3) }

    val monthNames = listOf("", "Янв", "Фев", "Мар", "Апр", "Май", "Июн",
                            "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")

    // Triple: метка / кол-во с регистрацией / кол-во без регистрации
    val data: List<Triple<String, Int, Int>> = remember(allEvents, periodMonths) {
        val cutoff = if (periodMonths == 1)
            java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay()
        else
            java.time.LocalDate.now().minusMonths(periodMonths.toLong()).withDayOfMonth(1).atStartOfDay()
        allEvents
            .filter { !it.startDate.isBefore(cutoff) }
            .groupBy { it.startDate.toLocalDate().withDayOfMonth(1) }
            .entries
            .sortedBy { it.key }
            .map { (date, evs) ->
                val label = "${monthNames[date.monthValue]} '${date.year.toString().takeLast(2)}"
                Triple(label, evs.count { it.needsRegistration }, evs.count { !it.needsRegistration })
            }
    }

    DashCard("Регистрация по месяцам", Icons.Outlined.HowToReg, ColorBlue) {
        // Выбор периода
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "Месяц", 3 to "3 мес.", 6 to "6 мес.", 12 to "1 год").forEach { (m, label) ->
                FilterChip(
                    selected = periodMonths == m,
                    onClick  = { periodMonths = m },
                    label    = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.height(32.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        if (data.isEmpty()) {
            Text(
                "Нет мероприятий за выбранный период",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            RegMonthBarChart(data)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LegendDot(ColorBlue, "С регистрацией",    0)
                LegendDot(ColorGray, "Без регистрации",   0)
            }
        }
    }
}

@Composable
private fun RegMonthBarChart(data: List<Triple<String, Int, Int>>) {
    val maxVal = data.maxOfOrNull { maxOf(it.second, it.third) }
        ?.coerceAtLeast(1)?.toFloat() ?: 1f
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val groupW = size.width / data.size
        val barW   = (groupW * 0.28f).coerceAtLeast(4.dp.toPx())
        val gap    = groupW * 0.06f

        // Сетка
        for (i in 1..3) {
            val y = size.height * (1f - i / 4f)
            drawLine(surfVar, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }

        data.forEachIndexed { idx, (_, withReg, withoutReg) ->
            val cx = idx * groupW + groupW / 2f

            // Бар "с регистрацией"
            val h1 = (withReg / maxVal * size.height)
                .coerceAtLeast(if (withReg > 0) 4.dp.toPx() else 0f)
            if (h1 > 0f) drawRoundRect(
                color        = Color(0xFF3B5BDB),
                topLeft      = Offset(cx - barW - gap / 2f, size.height - h1),
                size         = Size(barW, h1),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            // Бар "без регистрации"
            val h2 = (withoutReg / maxVal * size.height)
                .coerceAtLeast(if (withoutReg > 0) 4.dp.toPx() else 0f)
            if (h2 > 0f) drawRoundRect(
                color        = Color(0xFF9E9E9E).copy(alpha = 0.55f),
                topLeft      = Offset(cx + gap / 2f, size.height - h2),
                size         = Size(barW, h2),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
        }
    }

    // Метки месяцев
    Row(modifier = Modifier.fillMaxWidth()) {
        data.forEach { (label, _, _) ->
            Text(
                label,
                modifier  = Modifier.weight(1f),
                fontSize  = 9.sp,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вкладка 2: По типам
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypesTab(
    allEvents: List<com.events.app.domain.models.events.Event>
) {
    var periodMonths by remember { mutableIntStateOf(3) }

    val filtered = remember(allEvents, periodMonths) {
        when (periodMonths) {
            0    -> allEvents
            1    -> {
                val cutoff = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay()
                allEvents.filter { !it.startDate.isBefore(cutoff) }
            }
            else -> {
                val cutoff = java.time.LocalDate.now()
                    .minusMonths(periodMonths.toLong()).withDayOfMonth(1).atStartOfDay()
                allEvents.filter { !it.startDate.isBefore(cutoff) }
            }
        }
    }

    val byType: List<PieSlice> = remember(filtered) {
        filtered.groupBy { it.type.ifBlank { "Не указан" } }
            .entries.mapIndexed { i, (label, list) ->
                PieSlice(label, list.size, ScreenPalette[i % ScreenPalette.size])
            }.sortedByDescending { it.count }
    }

    val byFormat: List<PieSlice> = remember(filtered) {
        filtered.groupBy { it.format.ifBlank { "Не указан" } }
            .entries.mapIndexed { i, (label, list) ->
                PieSlice(label, list.size, ScreenPalette[(i + 3) % ScreenPalette.size])
            }.sortedByDescending { it.count }
    }

    // ── Выбор периода (общий для обоих пирогов) ───────────────────────
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1 to "Месяц", 3 to "3 мес.", 6 to "6 мес.", 12 to "1 год", 0 to "Всё").forEach { (m, label) ->
            FilterChip(
                selected = periodMonths == m,
                onClick  = { periodMonths = m },
                label    = { Text(label, fontSize = 12.sp) },
                modifier = Modifier.height(32.dp)
            )
        }
    }
    Spacer(Modifier.height(14.dp))

    // ── Типы ─────────────────────────────────────────────────────────
    if (byType.isNotEmpty()) {
        DashCard("Типы мероприятий", Icons.Outlined.DonutLarge, ColorAmber) {
            PieChartWithLegend(byType)
        }
        Spacer(Modifier.height(14.dp))
    }

    // ── Форматы ───────────────────────────────────────────────────────
    if (byFormat.isNotEmpty()) {
        DashCard("Форматы мероприятий", Icons.Outlined.Tv, ColorTeal) {
            PieChartWithLegend(byFormat)
        }
        Spacer(Modifier.height(14.dp))
    }

    // ── Избыточность типов — только текущая неделя ────────────────────
    CurrentWeekRedundancy(allEvents)
}

@Composable
private fun CurrentWeekRedundancy(
    allEvents: List<com.events.app.domain.models.events.Event>
) {
    val weekGroups = remember(allEvents) {
        val today     = java.time.LocalDate.now()
        val dow       = today.dayOfWeek.value          // 1=Пн … 7=Вс
        val weekStart = today.minusDays((dow - 1).toLong())
        val weekEnd   = weekStart.plusDays(6)
        allEvents
            .filter {
                val d = it.startDate.toLocalDate()
                !d.isBefore(weekStart) && !d.isAfter(weekEnd)
            }
            .groupBy { it.type.ifBlank { "Не указан" } }
            .filter { (_, evs) -> evs.size >= 2 }
            .entries
            .sortedByDescending { it.value.size }
    }

    DashCard("Избыточность типов · эта неделя", Icons.Outlined.ContentCopy, ColorAmber) {
        Text(
            "Типы с 2+ мероприятиями на текущей неделе",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        if (weekGroups.isEmpty()) {
            InsightText("✅ Избыточности не обнаружено")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                weekGroups.forEach { (type, evs) ->
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = ColorAmber.copy(0.07f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(type, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f), maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Surface(shape = RoundedCornerShape(8.dp), color = ColorAmber.copy(0.18f)) {
                                    Text("${evs.size}×",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorAmber)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                evs.joinToString(" · ") { it.title.take(20) },
                                fontSize = 10.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вкладка 3: Площадки · Время (объединённая)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun LocationTimeTab(
    stats: StatisticsData,
    allEvents: List<com.events.app.domain.models.events.Event>,
    onEventClick: (String) -> Unit
) {
    LocationsTab(stats, allEvents, onEventClick)
}

// ═══════════════════════════════════════════════════════════════════
// Вкладка 3: Локации
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsTab(
    stats: StatisticsData,
    allEvents: List<com.events.app.domain.models.events.Event>,
    onEventClick: (String) -> Unit
) {
    val allLocations = remember(stats.locationStats) {
        listOf("Все") + stats.locationStats
            .map { it.locationName }
            .filter { it.isNotBlank() && it != "—" }
    }
    var selectedLocation by remember { mutableStateOf("Все") }
    var showLocPicker    by remember { mutableStateOf(false) }

    // ── Мероприятия выбранной локации ────────────────────────────────
    val locEvents = remember(allEvents, stats.eventLocationMap, selectedLocation) {
        if (selectedLocation == "Все") allEvents
        else allEvents.filter { stats.eventLocationMap[it.id] == selectedLocation }
    }

    // ── Мероприятия текущей недели (Пн–Вс) ───────────────────────────
    val weekEvents = remember(locEvents) {
        val today     = java.time.LocalDate.now()
        val dow       = today.dayOfWeek.value
        val weekStart = today.minusDays((dow - 1).toLong()).atStartOfDay()
        val weekEnd   = weekStart.plusDays(7)
        locEvents.filter { !it.startDate.isBefore(weekStart) && it.startDate.isBefore(weekEnd) }
    }

    // ── Локации с количеством мероприятий (для топ/антитоп) ──────────
    val filteredLocStats = remember(stats.locationStats, selectedLocation) {
        if (selectedLocation == "Все") stats.locationStats
        else stats.locationStats.filter { it.locationName == selectedLocation }
    }

    // ── 1. Поиск / выбор локации ──────────────────────────────────────
    OutlinedButton(
        onClick        = { showLocPicker = true },
        modifier       = Modifier.fillMaxWidth().height(52.dp),
        shape          = RoundedCornerShape(14.dp),
        border         = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outline.copy(0.35f)),
        colors         = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = ColorBlue),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(18.dp), tint = ColorBlue)
        Spacer(Modifier.width(10.dp))
        Text(
            if (selectedLocation == "Все") "Выбрать локацию" else selectedLocation,
            fontSize = 14.sp, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            color = if (selectedLocation == "Все") MaterialTheme.colorScheme.onSurfaceVariant else ColorBlue
        )
        if (selectedLocation != "Все") {
            IconButton(onClick = { selectedLocation = "Все" }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp), tint = ColorBlue)
            }
        } else {
            Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
        }
    }
    if (showLocPicker) {
        ModalBottomSheet(
            onDismissRequest = { showLocPicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            StatPickerSheet(
                title    = "Выберите локацию",
                icon     = Icons.Outlined.LocationOn,
                items    = allLocations,
                selected = selectedLocation,
                onSelect = { selectedLocation = it; showLocPicker = false }
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    // ── 2. Топ-3 загруженных ──────────────────────────────────────────
    val busiest = remember(stats.placeStats, filteredLocStats, selectedLocation) {
        if (stats.placeStats.isNotEmpty()) {
            (if (selectedLocation == "Все") stats.placeStats
            else stats.placeStats.filter { it.locationName == selectedLocation })
                .sortedByDescending { it.eventCount }.take(3)
                .map { ps -> Triple(ps.placeName, ps.locationName, ps.eventCount) }
        } else {
            filteredLocStats.sortedByDescending { it.eventCount }.take(3)
                .map { ls -> Triple(ls.locationName, "${ls.placeCount} помещений", ls.eventCount) }
        }
    }
    DashCard("Топ-3 загруженных", Icons.Outlined.Whatshot, ColorRed) {
        if (busiest.isEmpty()) {
            InsightText("Нет данных")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                busiest.forEachIndexed { i, (name, sub, count) ->
                    val (medal, rankColor) = when (i) {
                        0    -> "🥇" to ColorRed
                        1    -> "🥈" to ColorAmber
                        else -> "🥉" to ColorGreen
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = rankColor.copy(0.07f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier.size(40.dp).background(rankColor.copy(0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text(medal, fontSize = 18.sp) }
                            Column(Modifier.weight(1f)) {
                                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(sub, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("$count", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                color = rankColor)
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))

    // ── 3. Топ-3 незагруженных ────────────────────────────────────────
    val leastBusy = remember(stats.placeStats, filteredLocStats, selectedLocation) {
        if (stats.placeStats.isNotEmpty()) {
            (if (selectedLocation == "Все") stats.placeStats
            else stats.placeStats.filter { it.locationName == selectedLocation })
                .filter { it.eventCount > 0 }
                .sortedBy { it.eventCount }.take(3)
                .map { ps -> Triple(ps.placeName, ps.locationName, ps.eventCount) }
        } else {
            filteredLocStats.filter { it.eventCount > 0 }
                .sortedBy { it.eventCount }.take(3)
                .map { ls -> Triple(ls.locationName, "${ls.placeCount} помещений", ls.eventCount) }
        }
    }
    DashCard("Топ-3 незагруженных", Icons.Outlined.EventAvailable, ColorTeal) {
        if (leastBusy.isEmpty()) {
            InsightText("Нет данных")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                leastBusy.forEachIndexed { i, (name, sub, count) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ColorTeal.copy(0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier.size(40.dp).background(ColorTeal.copy(0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold, color = ColorTeal)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(sub, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("$count", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                color = ColorTeal)
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))

    // ── 4. Конкуренция за аудиторию — эта неделя ─────────────────────
    val competition = remember(weekEvents) {
        weekEvents.groupBy { it.type.ifBlank { "Не указан" } }
            .filter { (_, evs) -> evs.size >= 2 }
            .entries.sortedByDescending { it.value.size }
    }
    DashCard("Конкуренция за аудиторию · эта неделя", Icons.Outlined.CompareArrows, ColorViolet) {
        Text("Мероприятия одного типа на одной неделе размывают аудиторию",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        if (competition.isEmpty()) {
            InsightText("✅ Конкуренции не обнаружено")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                competition.forEach { (type, evs) ->
                    Surface(shape = RoundedCornerShape(10.dp), color = ColorViolet.copy(0.07f),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(type, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f), maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Surface(shape = RoundedCornerShape(8.dp), color = ColorViolet.copy(0.18f)) {
                                    Text("${evs.size}×",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorViolet)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(evs.joinToString(" · ") { it.title.take(20) },
                                fontSize = 10.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))

    // ── 5. Тепловая карта плотности — текущая неделя ─────────────────
    val weekHeatmap = remember(weekEvents) {
        val map = mutableMapOf<Pair<Int, Int>, Int>()
        weekEvents.forEach { ev ->
            val key = ev.startDate.dayOfWeek.value to ev.startDate.hour
            map[key] = (map[key] ?: 0) + 1
        }
        map.map { (k, v) -> HeatCell(k.first, k.second, v) }
    }
    DashCard("Плотность мероприятий · эта неделя", Icons.Outlined.GridOn, ColorAmber) {
        if (weekHeatmap.isEmpty()) {
            InsightText("На этой неделе мероприятий нет")
        } else {
            HeatmapGrid(weekHeatmap)
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
// Таблица данных (StatTable)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun StatTable(
    headers: List<String>,
    rows: List<List<String>>,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    highlightRows: Set<Int> = emptySet()
) {
    val divColor  = MaterialTheme.colorScheme.outlineVariant
    val headerBg  = accentColor.copy(alpha = 0.10f)
    val altBg     = MaterialTheme.colorScheme.surfaceVariant.copy(0.35f)

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth().background(headerBg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            headers.forEachIndexed { i, h ->
                Text(
                    h,
                    modifier   = if (i == 0) Modifier.weight(1.5f) else Modifier.weight(1f),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor,
                    maxLines   = 1,
                    textAlign  = if (i == 0) TextAlign.Start else TextAlign.End
                )
            }
        }
        HorizontalDivider(color = divColor)
        // Строки данных
        rows.forEachIndexed { rowIdx, row ->
            val highlighted = rowIdx in highlightRows
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        when {
                            highlighted  -> accentColor.copy(0.06f)
                            rowIdx % 2 == 1 -> altBg
                            else         -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEachIndexed { colIdx, cell ->
                    Text(
                        cell,
                        modifier   = if (colIdx == 0) Modifier.weight(1.5f) else Modifier.weight(1f),
                        style      = if (colIdx == 0) MaterialTheme.typography.bodySmall
                                     else MaterialTheme.typography.labelMedium,
                        fontWeight = if (highlighted && colIdx > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (highlighted && colIdx > 0) accentColor
                                     else MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1, overflow = TextOverflow.Ellipsis,
                        textAlign  = if (colIdx == 0) TextAlign.Start else TextAlign.End
                    )
                }
            }
            if (rowIdx < rows.lastIndex) HorizontalDivider(color = divColor.copy(0.5f))
        }
    }
}

// ── Универсальная шторка-пикер для статистики ───────────────────
@Composable
private fun StatPickerSheet(
    title: String, icon: ImageVector,
    items: List<String>, selected: String,
    onSelect: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) items
    else items.filter { it.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = ColorBlue, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Поиск...") },
            leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp)) } },
            singleLine = true, shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center) {
                Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filtered) { item ->
                    val isSel = item == selected
                    Surface(
                        onClick = { onSelect(item) },
                        shape   = RoundedCornerShape(10.dp),
                        color   = if (isSel) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item, fontSize = 14.sp,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f), maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            if (isSel) Icon(Icons.Outlined.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Вспомогательный текст-инсайт ─────────────────────────────────
@Composable
private fun InsightText(text: String) {
    Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium)
}

// ── Пустое состояние для фильтра ──────────────────────────────────
@Composable
private fun EmptyFilterState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier              = Modifier.padding(32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.SearchOff, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                modifier = Modifier.size(36.dp)
            )
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вкладка 4: Время
// ═══════════════════════════════════════════════════════════════════
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TimeTab(
    stats: StatisticsData,
    allEvents: List<com.events.app.domain.models.events.Event>,
    onEventClick: (String) -> Unit
) {

    // ── Фильтр: локация + помещение ───────────────────────────────
    // Список локаций из locationStats (placeStats пуст т.к. ShortEventDto не возвращает placeId)
    val allLocNames = remember(stats.locationStats) {
        listOf("Все") + stats.locationStats
            .map { it.locationName }
            .filter { it.isNotBlank() && it != "—" }
    }
    var timeLoc   by remember { mutableStateOf("Все") }
    var timePlace by remember { mutableStateOf("Все") }

    // Список помещений для выбранной локации.
    // Доступен только если API возвращает placeId в событиях (сейчас ShortEventDto не возвращает).
    val placesForLoc = remember(stats.placeStats, timeLoc) {
        val places = if (timeLoc == "Все") stats.placeStats.map { it.placeName }
        else stats.placeStats.filter { it.locationName == timeLoc }.map { it.placeName }
        listOf("Все") + places
    }

    // Фильтруем события по выбранной локации и помещению
    // Фильтруем события:
    // 1) через placeStats — события с placeId (помещение известно)
    // 2) через eventLocationMap — события без placeId (определили по locationId/location)
    // Оба источника объединяются, чтобы ни одно событие не пропало.
    val filteredEvents = remember(allEvents, stats.placeStats, stats.eventLocationMap, timeLoc, timePlace) {
        if (timeLoc == "Все" && timePlace == "Все") return@remember allEvents

        // Источник 1: через placeStats (знает placeName для фильтра по помещению)
        val idsFromPlace: Set<String> = if (timePlace != "Все") {
            stats.placeStats
                .filter { ps ->
                    (timeLoc == "Все" || ps.locationName == timeLoc) && ps.placeName == timePlace
                }
                .flatMap { it.events }.toSet()
        } else {
            stats.placeStats
                .filter { ps -> timeLoc == "Все" || ps.locationName == timeLoc }
                .flatMap { it.events }.toSet()
        }

        // Источник 2: через eventLocationMap (все события, в т.ч. без placeId)
        val idsFromMap: Set<String> = if (timePlace == "Все") {
            stats.eventLocationMap
                .filter { (_, locName) -> locName == timeLoc }
                .keys
        } else emptySet()  // фильтр по помещению недоступен через карту — используем только placeStats

        val relevantIds = idsFromPlace + idsFromMap
        if (relevantIds.isEmpty()) emptyList()
        else allEvents.filter { it.id in relevantIds }
    }

    // Пересчитываем heatmap из отфильтрованных событий
    val fHeatmap = remember(filteredEvents) {
        val map = mutableMapOf<Pair<Int, Int>, Int>()
        filteredEvents.forEach { ev ->
            val key = ev.startDate.dayOfWeek.value to ev.startDate.hour
            map[key] = (map[key] ?: 0) + 1
        }
        map.map { (k, v) -> HeatCell(k.first, k.second, v) }
    }

    val fByDay = remember(filteredEvents) {
        val dayMap = mutableMapOf<Int, Int>()
        filteredEvents.forEach { ev ->
            val d = ev.startDate.dayOfWeek.value
            dayMap[d] = (dayMap[d] ?: 0) + 1
        }
        val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        (1..7).map { d -> BarItem(dayNames[d - 1], dayMap[d] ?: 0) }
    }

    val fBurdenDays = remember(filteredEvents) {
        filteredEvents
            .groupBy { it.startDate.toLocalDate().toString() }
            .filter { (_, evs) -> evs.size >= 3 }
            .entries.sortedByDescending { (_, evs) -> evs.size }
            .take(7)
            .map { (date, evs) -> BurdenDay(date, evs.size, evs.map { it.title.take(20) }) }
    }

    val fByHour = remember(filteredEvents) {
        val hourMap = mutableMapOf<Int, Int>()
        filteredEvents.forEach { ev ->
            val h = ev.startDate.hour
            hourMap[h] = (hourMap[h] ?: 0) + 1
        }
        (0..23).map { h -> BarItem("%02d:00".format(h), hourMap[h] ?: 0) }
    }

    // ── Пикеры: локация и помещение ───────────────────────────────
    var showTimeLocPicker   by remember { mutableStateOf(false) }
    var showTimePlacePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Кнопка локации
        OutlinedButton(
            onClick        = { showTimeLocPicker = true },
            modifier       = Modifier.fillMaxWidth().height(52.dp),
            shape          = RoundedCornerShape(14.dp),
            border         = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outline.copy(0.35f)),
            colors         = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface, contentColor = ColorBlue),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(18.dp), tint = ColorBlue)
            Spacer(Modifier.width(10.dp))
            Text(
                if (timeLoc == "Все") "Выбрать локацию" else timeLoc,
                fontSize = 14.sp, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                color = if (timeLoc == "Все") MaterialTheme.colorScheme.onSurfaceVariant else ColorBlue
            )
            if (timeLoc != "Все") {
                IconButton(onClick = { timeLoc = "Все"; timePlace = "Все" }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp), tint = ColorBlue)
                }
            } else {
                Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
            }
        }
        // Кнопка помещения (только если выбрана локация)
        if (timeLoc != "Все" && placesForLoc.size > 1) {
            OutlinedButton(
                onClick        = { showTimePlacePicker = true },
                modifier       = Modifier.fillMaxWidth().height(52.dp),
                shape          = RoundedCornerShape(14.dp),
                border         = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline.copy(0.35f)),
                colors         = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface, contentColor = ColorViolet),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Outlined.MeetingRoom, null, modifier = Modifier.size(18.dp), tint = ColorViolet)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (timePlace == "Все") "Выбрать помещение (необязательно)" else timePlace,
                    fontSize = 14.sp, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    color = if (timePlace == "Все") MaterialTheme.colorScheme.onSurfaceVariant else ColorViolet
                )
                if (timePlace != "Все") {
                    IconButton(onClick = { timePlace = "Все" }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp), tint = ColorViolet)
                    }
                } else {
                    Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    if (showTimeLocPicker) {
        ModalBottomSheet(
            onDismissRequest = { showTimeLocPicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            StatPickerSheet(
                title    = "Выберите локацию",
                icon     = Icons.Outlined.LocationOn,
                items    = allLocNames,
                selected = timeLoc,
                onSelect = { timeLoc = it; timePlace = "Все"; showTimeLocPicker = false }
            )
        }
    }
    if (showTimePlacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showTimePlacePicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            StatPickerSheet(
                title    = "Выберите помещение",
                icon     = Icons.Outlined.MeetingRoom,
                items    = placesForLoc,
                selected = timePlace,
                onSelect = { timePlace = it; showTimePlacePicker = false }
            )
        }
    }



    // ── 1. GitHub-style тепловая карта по дням ────────────────────
    if (fHeatmap.isNotEmpty() || fByDay.any { it.value > 0 }) {
        DashCard("Плотность мероприятий", Icons.Outlined.GridOn, ColorAmber) {
            Text("Активность по дням недели и часам — видны «перегруженные» периоды",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            HeatmapGrid(fHeatmap)
        }
        Spacer(Modifier.height(14.dp))
    }

    // ── 2. Пиковые периоды и провалы ─────────────────────────────
    DashCard("Пиковые периоды и провалы", Icons.Outlined.Insights, ColorBlue) {
        // Самая загруженная неделя
        val peakDay = fByDay.maxByOrNull { it.value }
        val peakHour  = fByHour.maxByOrNull { it.value }
        val quietHour = fByHour.filter { it.value > 0 }.minByOrNull { it.value }

        if (peakDay != null && peakDay.value > 0) {
            InsightText("📈 Самый активный день: ${peakDay.label} — ${peakDay.value} мероприятий")
            Spacer(Modifier.height(6.dp))
        }
        if (peakHour != null && peakHour.value > 0) {
            InsightText("⏰ Пиковый час: ${peakHour.label} — ${peakHour.value} мероприятий")
            Spacer(Modifier.height(6.dp))
        }
        if (quietHour != null) {
            InsightText("😴 Тихий час: ${quietHour.label} — ${quietHour.value} мероприятий")
            Spacer(Modifier.height(10.dp))
        }

        // Активность по дням недели
        if (fByDay.any { it.value > 0 }) {
            Spacer(Modifier.height(4.dp))
            SimpleBarChart(fByDay.filter { it.value > 0 }, ColorBlue)
        }
    }
    Spacer(Modifier.height(14.dp))

    // ── 4. Дни высокой нагрузки ───────────────────────────────────
    DashCard("Дни высокой нагрузки", Icons.Outlined.PriorityHigh, ColorRed) {
        Text("Дни с 3+ мероприятиями — повышенная нагрузка на организацию",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        if (fBurdenDays.isEmpty()) {
            InsightText("✅ Дней перегрузки не обнаружено")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fBurdenDays.take(5).forEach { bd ->
                    Surface(shape = RoundedCornerShape(10.dp), color = ColorRed.copy(0.06f),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.Event, null, tint = ColorRed, modifier = Modifier.size(14.dp))
                                    Text(bd.date, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = ColorRed.copy(0.15f)) {
                                    Text("${bd.eventCount} мероприятий",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorRed)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(bd.titles.joinToString(" · "), fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))

}

// ═══════════════════════════════════════════════════════════════════
// Строка загруженности помещения
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun PlaceStatRow(ps: PlaceStat, onClick: () -> Unit) {
    val fillColor = when {
        ps.avgFillRate >= 75f -> ColorRed
        ps.avgFillRate >= 40f -> ColorAmber
        ps.avgFillRate > 0f   -> ColorGreen
        else                  -> ColorGray
    }
    val anim = remember(ps.placeId) { Animatable(0f) }
    LaunchedEffect(ps) { anim.animateTo(ps.avgFillRate / 100f, tween(800)) }

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 4.dp, vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ps.placeName, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${ps.locationName} · ${ps.eventCount} мероприятий",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(if (ps.avgFillRate > 0f) "${ps.avgFillRate.roundToInt()}%"
            else "${ps.eventCount} соб.",
                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fillColor,
                modifier = Modifier.padding(start = 8.dp))
        }
        if (ps.avgFillRate > 0f) {
            Spacer(Modifier.height(6.dp))
            val p = anim.value
            val surfVar = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                drawRoundRect(color = surfVar, size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(3.dp.toPx()))
                val w = size.width * p.coerceIn(0f, 1f)
                if (w > 0f) drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(fillColor.copy(0.6f), fillColor), 0f, w),
                    size  = Size(w, size.height), cornerRadius = CornerRadius(3.dp.toPx()))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Строка конфликта расписания
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun OverlapRow(ov: OverlapPair, onEventClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.MeetingRoom, null, tint = ColorAmber, modifier = Modifier.size(14.dp))
            Text(ov.placeName, fontSize = 11.sp, color = ColorAmber, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(modifier = Modifier.weight(1f).clickable { onEventClick(ov.eventA) },
                shape = RoundedCornerShape(8.dp), color = ColorRed.copy(0.08f)) {
                Text(ov.titleA, modifier = Modifier.padding(8.dp), fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.Outlined.SwapHoriz, null, tint = ColorAmber,
                modifier = Modifier.size(20.dp).align(Alignment.CenterVertically))
            Surface(modifier = Modifier.weight(1f).clickable { onEventClick(ov.eventB) },
                shape = RoundedCornerShape(8.dp), color = ColorRed.copy(0.08f)) {
                Text(ov.titleB, modifier = Modifier.padding(8.dp), fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Тепловая карта день × час
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun HeatmapGrid(heatmap: List<HeatCell>) {
    val days  = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val hours = listOf("0", "3", "6", "9", "12", "15", "18", "21")
    val maxVal = heatmap.maxOfOrNull { it.count }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        // Заголовок часов
        Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp)) {
            hours.forEach { h ->
                Text(h, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(2.dp))
        // Строки по дням
        (1..7).forEach { day ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(days[day - 1], fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp))
                // Ячейки по 3 часа
                (0..7).forEach { hourBucket ->
                    val hourStart = hourBucket * 3
                    val count = heatmap.filter { it.day == day && it.hour in hourStart until hourStart + 3 }
                        .sumOf { it.count }
                    val intensity = (count / maxVal).coerceIn(0f, 1f)
                    val cellColor = ColorAmber.copy(alpha = 0.1f + intensity * 0.9f)
                    Box(modifier = Modifier.weight(1f).height(22.dp).padding(1.dp)
                        .background(if (count > 0) cellColor else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(3.dp))) {
                        if (count > 0) Text(count.toString(), fontSize = 8.sp,
                            color = if (intensity > 0.5f) Color.White else Color(0xFF7C4D00),
                            modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // Легенда
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Мало", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            (0..4).forEach { i ->
                Box(modifier = Modifier.size(14.dp)
                    .background(ColorAmber.copy(alpha = 0.1f + i * 0.2f), RoundedCornerShape(2.dp)))
            }
            Text("Много", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Карточка конверсии просмотры → регистрации
// ═══════════════════════════════════════════════════════════════════
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConversionCard(
    globalViews: Int, globalParticipants: Int, globalRate: Float,
    allEvents: List<com.events.app.domain.models.events.Event>,
    selectedConversion: EventConversion?,
    conversionLoading: Boolean,
    onSelectEvent: (String) -> Unit,
    onClearEvent: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayViews = selectedConversion?.views ?: globalViews
    val displayParticipants = selectedConversion?.participants ?: globalParticipants
    val displayRate  = selectedConversion?.conversionRate ?: globalRate
    val isFiltered   = selectedConversion != null

    val color = when {
        displayRate >= 30f -> ColorGreen
        displayRate >= 10f -> ColorAmber
        else               -> ColorCyan
    }
    val anim = remember(displayRate) { Animatable(0f) }
    LaunchedEffect(displayRate) { anim.animateTo((displayRate / 100f).coerceIn(0f, 1f), tween(1200)) }
    val p = anim.value

    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).background(color, RoundedCornerShape(2.dp)))
                Icon(Icons.Outlined.TrendingUp, null, tint = color, modifier = Modifier.size(15.dp))
                Text("Конверсия", fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(10.dp))
            // Кнопка выбора/сброса — во всю ширину под заголовком
            if (isFiltered) {
                Surface(onClick = onClearEvent, shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(0.5f),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Сбросить выбранное мероприятие", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                OutlinedButton(
                    onClick  = { showPicker = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline.copy(0.35f)
                    ),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor   = ColorBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp), tint = ColorBlue)
                    Spacer(Modifier.width(10.dp))
                    Text("Выбрать мероприятие", fontSize = 14.sp,
                        fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start)
                    Icon(Icons.Outlined.ChevronRight, null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                }
            }

            // Подпись выбранного мероприятия
            if (isFiltered) {
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = color.copy(0.08f),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Event, null, tint = color, modifier = Modifier.size(12.dp))
                        Text(selectedConversion!!.title, fontSize = 11.sp, color = color,
                            fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Загрузка
            if (conversionLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else {
                // Воронка просмотры → участники
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = ColorCyan.copy(0.10f),
                        modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Visibility, null, tint = ColorCyan, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(displayViews.toString(), fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold, color = ColorCyan)
                            Text("просмотров", fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                    Column(modifier = Modifier.padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${displayRate.roundToInt()}%", fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold, color = color)
                        Icon(Icons.Outlined.ArrowForward, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                    Surface(shape = RoundedCornerShape(14.dp), color = ColorViolet.copy(0.10f),
                        modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.HowToReg, null, tint = ColorViolet, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(displayParticipants.toString(), fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold, color = ColorViolet)
                            Text("зарегистрировалось", fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }

                // Заполняемость (только для конкретного события)
                if (isFiltered && selectedConversion?.fillRate != null) {
                    Spacer(Modifier.height(8.dp))
                    val fillColor = when {
                        selectedConversion.fillRate >= 75f -> ColorRed
                        selectedConversion.fillRate >= 40f -> ColorAmber
                        else                              -> ColorGreen
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.PeopleAlt, null, tint = fillColor, modifier = Modifier.size(14.dp))
                        Text("Заполняемость: ${selectedConversion.fillRate.roundToInt()}% из ${selectedConversion.maxParticipants ?: "?"} мест",
                            fontSize = 12.sp, color = fillColor, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Прогресс-бар конверсии
                val surfVar = MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    drawRoundRect(color = surfVar, size = Size(size.width, size.height), cornerRadius = CornerRadius(4.dp.toPx()))
                    val w = size.width * p
                    if (w > 0f) drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(color.copy(0.5f), color), 0f, w.coerceAtLeast(1f)),
                        size  = Size(w, size.height), cornerRadius = CornerRadius(4.dp.toPx()))
                }
                val text = when {
                    displayRate >= 30f -> "Отличный результат: мероприятие хорошо привлекает участников к регистрации."
                    displayRate >= 10f -> "Конверсия на среднем уровне."
                    displayRate > 0f   -> "Большинство зрителей не доходит до регистрации."
                    else               -> if (isFiltered) "По данному мероприятию регистраций пока не зафиксировано."
                    else "Данные о регистрациях отсутствуют."
                }
                Text(text, fontSize = 12.sp, color = color.copy(0.85f))
            }
        }
    }

    // Шторка выбора мероприятия
    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EventPickerSheet(
                events   = allEvents,
                onSelect = { id -> onSelectEvent(id); showPicker = false }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Шторка выбора мероприятия для конверсии
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun EventPickerSheet(
    events: List<com.events.app.domain.models.events.Event>,
    onSelect: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) events
    else events.filter {
        it.title.contains(query, ignoreCase = true) ||
                it.id.contains(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Заголовок
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.TrendingUp, null, tint = ColorCyan, modifier = Modifier.size(20.dp))
            Text("Выберите мероприятие", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }

        // Поиск
        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            placeholder   = { Text("Поиск по названию или ID...") },
            leadingIcon   = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
            trailingIcon  = {
                if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp))
                }
            },
            singleLine = true,
            shape      = RoundedCornerShape(12.dp),
            modifier   = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center) {
                Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier            = Modifier.fillMaxWidth().height(440.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding      = PaddingValues(bottom = 32.dp)
            ) {
                items(filtered) { event ->
                    Surface(
                        onClick  = { onSelect(event.id) },
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = ColorCyan.copy(0.12f),
                                modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Event, null, tint = ColorCyan, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title.ifBlank { "Без названия" }, fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(event.id.take(18) + "…", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Outlined.TrendingUp, null, tint = ColorCyan,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Простой бар-чарт (без кликов)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun SimpleBarChart(
    items: List<BarItem>, color: Color, suffix: String = "", maxValue: Int? = null
) {
    val maxVal = maxValue?.toFloat() ?: items.maxOfOrNull { it.value.toFloat() }?.coerceAtLeast(1f) ?: 1f
    val anim = remember { Animatable(0f) }
    LaunchedEffect(items) { anim.animateTo(1f, tween(900)) }
    val p = anim.value
    val surfVar = MaterialTheme.colorScheme.surfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        items.forEachIndexed { idx, item ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(item.label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${item.value}$suffix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color,
                        modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(Modifier.height(4.dp))
                val fraction = (item.value / maxVal * p).coerceIn(0f, 1f)
                Canvas(modifier = Modifier.fillMaxWidth().height(7.dp)) {
                    drawRoundRect(color = surfVar, size = Size(size.width, size.height), cornerRadius = CornerRadius(4.dp.toPx()))
                    val w = size.width * fraction
                    if (w > 0f) drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(color.copy(0.6f), color), 0f, w),
                        size  = Size(w, size.height), cornerRadius = CornerRadius(4.dp.toPx()))
                }
            }
            if (idx < items.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.07f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Кликабельный бар-чарт (из оригинала)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun ClickableBarChart(
    items: List<BarItem>, color: Color, suffix: String = "",
    maxValue: Int? = null, onItemClick: (BarItem) -> Unit
) {
    val maxVal = maxValue?.toFloat() ?: items.maxOfOrNull { it.value.toFloat() }?.coerceAtLeast(1f) ?: 1f
    val surfVar = MaterialTheme.colorScheme.surfaceVariant
    val anim = remember { Animatable(0f) }
    LaunchedEffect(items) { anim.animateTo(1f, tween(1000)) }
    val p = anim.value

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        items.forEachIndexed { index, item ->
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .clickable { onItemClick(item) }.padding(horizontal = 4.dp, vertical = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(item.label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${item.value}$suffix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                        Icon(Icons.Outlined.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                val fraction = (item.value / maxVal * p).coerceIn(0f, 1f)
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    drawRoundRect(color = surfVar, size = Size(size.width, size.height), cornerRadius = CornerRadius(4.dp.toPx()))
                    val w = size.width * fraction
                    if (w > 0f) drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(color.copy(0.6f), color), 0f, w),
                        size  = Size(w, size.height), cornerRadius = CornerRadius(4.dp.toPx()))
                }
            }
            if (index < items.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Area / Line chart (из оригинала)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun AreaLineChart(
    series: List<List<Float>>, colors: List<Color>, labels: List<String>,
    height: Int = 160, filled: Boolean = true
) {
    if (series.isEmpty() || series.first().isEmpty()) return
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(series) { animProgress.animateTo(1f, tween(1200)) }
    val p = animProgress.value
    val gridColor  = Color(0xFF444466); val labelColor = Color(0xFF9999BB); val gridLines = 4
    val maxVal = series.flatten().maxOrNull()?.takeIf { it > 0 } ?: 1f

    Box(modifier = Modifier.fillMaxWidth().height(height.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val padLeft = 36.dp.toPx(); val padRight = 8.dp.toPx()
            val padTop  = 8.dp.toPx();  val padBot   = 24.dp.toPx()
            val chartW  = w - padLeft - padRight; val chartH = h - padTop - padBot
            for (i in 0..gridLines) {
                val y = padTop + chartH * i / gridLines
                drawLine(gridColor, Offset(padLeft, y), Offset(w - padRight, y),
                    0.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                val yVal = (maxVal * (gridLines - i) / gridLines).roundToInt()
                drawContext.canvas.nativeCanvas.drawText(yVal.toString(), padLeft - 4.dp.toPx(), y + 4.dp.toPx(),
                    android.graphics.Paint().apply { color = labelColor.toArgb(); textSize = 9.dp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT })
            }
            val step = if (labels.size > 7) labels.size / 6 else 1
            labels.forEachIndexed { idx, lbl ->
                if (idx % step == 0 || idx == labels.lastIndex) {
                    val x = padLeft + chartW * idx / (labels.size - 1).coerceAtLeast(1)
                    drawContext.canvas.nativeCanvas.drawText(lbl, x, h - 4.dp.toPx(),
                        android.graphics.Paint().apply { color = labelColor.toArgb(); textSize = 9.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER })
                }
            }
            series.forEachIndexed { si, points ->
                if (points.isEmpty()) return@forEachIndexed
                val c = colors[si % colors.size]; val n = points.size
                val vc = (n * p).roundToInt().coerceIn(2, n)
                fun xOf(i: Int)   = padLeft + chartW * i / (n - 1).coerceAtLeast(1)
                fun yOf(v: Float) = padTop + chartH * (1f - v / maxVal)
                val linePath = Path()
                for (i in 0 until vc) {
                    val x = xOf(i); val y = yOf(points[i])
                    if (i == 0) linePath.moveTo(x, y)
                    else { val px = xOf(i - 1); val py = yOf(points[i - 1]); val cx = (px + x) / 2
                        linePath.cubicTo(cx, py, cx, y, x, y) }
                }
                if (filled) {
                    val fp = Path().apply { addPath(linePath)
                        lineTo(xOf(vc - 1), padTop + chartH); lineTo(padLeft, padTop + chartH); close() }
                    clipRect(padLeft, padTop, w - padRight, padTop + chartH) {
                        drawPath(fp, brush = Brush.verticalGradient(
                            listOf(c.copy(0.35f), c.copy(0.02f)), padTop, padTop + chartH)) }
                }
                clipRect(padLeft, padTop, w - padRight, padTop + chartH) {
                    drawPath(linePath, color = c, style = Stroke(
                        width = if (filled) 2.dp.toPx() else 1.8.dp.toPx(),
                        cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                if (n <= 30) for (i in 0 until vc) {
                    drawCircle(c, 3.dp.toPx(), Offset(xOf(i), yOf(points[i])))
                    drawCircle(Color.Black.copy(0.4f), 1.5.dp.toPx(), Offset(xOf(i), yOf(points[i])))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Пончик (из оригинала)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun DonutChart(slices: List<PieSlice>, centerLabel: String, centerSub: String, size: Int = 100) {
    val total = slices.sumOf { it.count }.takeIf { it > 0 } ?: 1
    val anim  = remember { Animatable(0f) }
    LaunchedEffect(slices) { anim.animateTo(1f, tween(900)) }
    val p = anim.value
    val stroke = Stroke(width = 18.dp.value, cap = StrokeCap.Round)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(size.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 10.dp.toPx(); val arc = Size(this.size.width - pad * 2, this.size.height - pad * 2)
                val tl  = Offset(pad, pad)
                drawArc(ColorGray.copy(0.12f), 0f, 360f, false, tl, arc, style = stroke)
                var start = -90f
                slices.forEach { s -> val sweep = 360f * s.count / total * p
                    drawArc(Color(s.color.toInt()), start, sweep, false, tl, arc, style = stroke)
                    start += sweep }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerLabel, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(centerSub, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.forEach { s ->
                LegendDot(Color(s.color.toInt()), s.label, s.count, "${(s.count * 100f / total).roundToInt()}%")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Пирог с легендой (из оригинала)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun PieChartWithLegend(slices: List<PieSlice>) {
    val total = slices.sumOf { it.count }.takeIf { it > 0 } ?: 1
    val anim  = remember { Animatable(0f) }
    LaunchedEffect(slices) { anim.animateTo(1f, tween(900)) }
    val p = anim.value
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Canvas(modifier = Modifier.size(110.dp)) {
            var start = -90f; val pad = 6.dp.toPx()
            val arc = Size(size.width - pad * 2, size.height - pad * 2); val tl = Offset(pad, pad)
            slices.forEach { s -> val sweep = 360f * s.count / total * p
                drawArc(Color(s.color.toInt()), start, sweep, true, tl, arc); start += sweep }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            slices.forEach { s -> LegendDot(Color(s.color.toInt()), s.label, s.count,
                "${(s.count * 100f / total).roundToInt()}%") }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Gauge заполненности (из оригинала)
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun FillRateGauge(fillRate: Float, participants: Int, avgFill: Float) {
    val color = when { fillRate >= 80f -> ColorRed; fillRate >= 50f -> ColorAmber; else -> ColorGreen }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(fillRate) { anim.animateTo(fillRate / 100f, tween(1200)) }
    val p = anim.value
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                val pad = 10.dp.toPx(); val arc = Size(size.width - pad * 2, size.height - pad * 2)
                val tl = Offset(pad, pad)
                drawArc(color.copy(0.12f), 135f, 270f, false, tl, arc, style = stroke)
                if (p > 0f) drawArc(color, 135f, 270f * p, false, tl, arc, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${fillRate.roundToInt()}%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
                Text("заполн.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            SmallMetric(Icons.Outlined.Groups, "Всего участников", participants.toString(), ColorViolet)
            SmallMetric(Icons.Outlined.Person, "Ср. на событие", "%.1f".format(avgFill), ColorAmber)
            SmallMetric(Icons.Outlined.PieChart, "Заполненность", "${fillRate.roundToInt()}%", color)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Вспомогательные компоненты
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun MetricTile(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.08f)) {
        Column(
            modifier            = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(0.16f), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = color,
                maxLines   = 1,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style     = MaterialTheme.typography.labelSmall,
                color     = color.copy(0.75f),
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
        }
    }
}

@Composable
private fun DashCard(
    title: String, icon: ImageVector, accentColor: Color,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = accentColor.copy(alpha = 0.13f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = accentColor, modifier = Modifier.size(17.dp))
                    }
                }
                Text(
                    title,
                    style  = MaterialTheme.typography.titleSmall,
                    color  = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SmallMetric(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = color.copy(0.12f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(16.dp)) }
        }
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (value.isNotEmpty()) Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, count: Int, suffix: String = "") {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(text = if (suffix.isNotEmpty()) "$label — $count ($suffix)" else "$label — $count",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
    }
}
