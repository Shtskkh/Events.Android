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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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

// ═══════════════════════════════════════════════════════════════════
// StatisticsScreen
// Параметр onEventClick нужно передать из AppGraph:
//   StatisticsScreen(onEventClick = { id -> navToEventDetails(id) })
// ═══════════════════════════════════════════════════════════════════

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()
    val stats     by viewModel.stats.collectAsState()

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
                    Spacer(Modifier.width(8.dp))
                    Text("Повторить")
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
                stats        = stats!!,
                onEventClick = onEventClick,
                onRetry      = { viewModel.loadStats() }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Основной контент
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun StatisticsContent(
    stats: StatisticsData,
    onEventClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Плитки метрик ─────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Всего",       stats.total.toString(),            Icons.Outlined.Event,     ColorBlue,   Modifier.weight(1f))
            MetricTile("Предстоящих", stats.upcoming.toString(),         Icons.Outlined.Upcoming,  ColorGreen,  Modifier.weight(1f))
            MetricTile("Завершённых", stats.finished.toString(),         Icons.Outlined.EventBusy, ColorGray,   Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Просмотры",   stats.totalViews.toString(),       Icons.Outlined.Visibility, ColorCyan,  Modifier.weight(1f))
            MetricTile("Участники",   stats.totalParticipants.toString(),Icons.Outlined.Groups,     ColorViolet,Modifier.weight(1f))
            MetricTile("Заполнен.",   "${stats.fillRate.roundToInt()}%", Icons.Outlined.PieChart,   ColorRed,   Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // ── Суммарный area-график просмотров ──────────────────────
        if (stats.viewsTimeSeries.size >= 2) {
            DashCard(title = "Просмотры по дням", icon = Icons.Outlined.ShowChart, accentColor = ColorCyan) {
                AreaLineChart(
                    series = listOf(stats.viewsTimeSeries.map { it.views.toFloat() }),
                    colors = listOf(ColorCyan),
                    labels = stats.viewsTimeSeries.map { it.date.takeLast(5) },
                    height = 160
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Топ событий: мультилинейный график + вертикальная легенда
        if (stats.topEventsTimeSeries.size >= 2) {
            val allDates = stats.topEventsTimeSeries
                .flatMap { (_, _, series) -> series.map { it.date } }
                .distinct().sorted()

            DashCard(title = "Просмотры топ событий", icon = Icons.Outlined.Leaderboard, accentColor = ColorGreen) {
                // График
                val allSeries = stats.topEventsTimeSeries.map { (_, _, series) ->
                    val dateMap = series.associate { it.date to it.views.toFloat() }
                    allDates.map { dateMap[it] ?: 0f }
                }
                AreaLineChart(
                    series = allSeries,
                    colors = MultiLineColors,
                    labels = allDates.map { it.takeLast(5) },
                    height = 160,
                    filled = false
                )

                Spacer(Modifier.height(14.dp))

                // Вертикальная легенда — каждый элемент кликабелен
                HorizontalDivider(
                    color    = MaterialTheme.colorScheme.outline.copy(0.15f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    stats.topEventsTimeSeries.forEachIndexed { i, (eventId, title, series) ->
                        val lineColor  = MultiLineColors[i % MultiLineColors.size]
                        val totalViews = series.sumOf { it.views }
                        EventLegendRow(
                            color       = lineColor,
                            title       = title,
                            subtitle    = "$totalViews просм.",
                            onClick     = { onEventClick(eventId) }
                        )
                        if (i < stats.topEventsTimeSeries.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Статус и регистрация (рядом, одинаковая высота) ───────
        Row(
            modifier              = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashCard(
                title       = "Статус",
                icon        = Icons.Outlined.PieChart,
                accentColor = ColorBlue,
                modifier    = Modifier.weight(1f).fillMaxHeight()
            ) {
                DonutChart(
                    slices      = listOf(
                        PieSlice("Пред.", stats.upcoming, ColorGreen.value.toLong()),
                        PieSlice("Заверш.", stats.finished, ColorGray.value.toLong())
                    ),
                    centerLabel = stats.total.toString(),
                    centerSub   = "всего",
                    size        = 100
                )
            }
            DashCard(
                title       = "Регистрация",
                icon        = Icons.Outlined.HowToReg,
                accentColor = ColorViolet,
                modifier    = Modifier.weight(1f).fillMaxHeight()
            ) {
                DonutChart(
                    slices      = stats.registrationPie,
                    centerLabel = stats.withRegistration.toString(),
                    centerSub   = "с регистр.",
                    size        = 100
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Типы и форматы ────────────────────────────────────────
        if (stats.byType.isNotEmpty()) {
            DashCard(title = "По типам мероприятий", icon = Icons.Outlined.Category, accentColor = ColorAmber) {
                PieChartWithLegend(slices = stats.byType)
            }
            Spacer(Modifier.height(14.dp))
        }

        if (stats.byFormat.isNotEmpty()) {
            DashCard(title = "По форматам", icon = Icons.Outlined.Tv, accentColor = ColorTeal) {
                PieChartWithLegend(slices = stats.byFormat)
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Топ-5 по просмотрам (кликабельные) ───────────────────
        if (stats.topByViews.isNotEmpty()) {
            DashCard(title = "Топ-5 по просмотрам", icon = Icons.Outlined.Visibility, accentColor = ColorCyan) {
                ClickableBarChart(
                    items        = stats.topByViews,
                    color        = ColorCyan,
                    onItemClick  = { item -> if (item.eventId.isNotBlank()) onEventClick(item.eventId) }
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Топ-5 по участникам (кликабельные) ───────────────────
        if (stats.topByParticipants.isNotEmpty()) {
            DashCard(title = "Топ-5 по участникам", icon = Icons.Outlined.Groups, accentColor = ColorViolet) {
                ClickableBarChart(
                    items        = stats.topByParticipants,
                    color        = ColorViolet,
                    onItemClick  = { item -> if (item.eventId.isNotBlank()) onEventClick(item.eventId) }
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Топ-5 по заполненности (кликабельные) ─────────────────
        if (stats.topByFillRate.isNotEmpty()) {
            DashCard(title = "Топ-5 по заполненности", icon = Icons.Outlined.StackedBarChart, accentColor = ColorRed) {
                ClickableBarChart(
                    items        = stats.topByFillRate,
                    color        = ColorRed,
                    suffix       = "%",
                    maxValue     = 100,
                    onItemClick  = { item -> if (item.eventId.isNotBlank()) onEventClick(item.eventId) }
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Общая заполненность ───────────────────────────────────
        if (stats.totalParticipants > 0) {
            DashCard(title = "Общая заполненность", icon = Icons.Outlined.Analytics, accentColor = ColorRed) {
                FillRateGauge(
                    fillRate     = stats.fillRate,
                    participants = stats.totalParticipants,
                    avgFill      = stats.avgParticipants
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
// Строка легенды для мультилинейного графика (кликабельная)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EventLegendRow(
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Цветная линия-индикатор
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color    = color
            )
        }
        Icon(
            Icons.Outlined.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// Area / Line chart
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun AreaLineChart(
    series: List<List<Float>>,
    colors: List<Color>,
    labels: List<String>,
    height: Int = 160,
    filled: Boolean = true
) {
    if (series.isEmpty() || series.first().isEmpty()) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(series) { animProgress.animateTo(1f, tween(1200)) }
    val p = animProgress.value

    val gridColor  = Color(0xFF444466)
    val labelColor = Color(0xFF9999BB)
    val gridLines  = 4

    val allValues  = series.flatten()
    val maxVal     = allValues.maxOrNull()?.takeIf { it > 0 } ?: 1f

    Box(modifier = Modifier.fillMaxWidth().height(height.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w        = size.width
            val h        = size.height
            val padLeft  = 36.dp.toPx()
            val padRight = 8.dp.toPx()
            val padTop   = 8.dp.toPx()
            val padBot   = 24.dp.toPx()
            val chartW   = w - padLeft - padRight
            val chartH   = h - padTop - padBot

            // Сетка и Y-метки
            for (i in 0..gridLines) {
                val y = padTop + chartH * i / gridLines
                drawLine(
                    color       = gridColor,
                    start       = Offset(padLeft, y),
                    end         = Offset(w - padRight, y),
                    strokeWidth = 0.5.dp.toPx(),
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
                val yVal = (maxVal * (gridLines - i) / gridLines).roundToInt()
                drawContext.canvas.nativeCanvas.drawText(
                    yVal.toString(),
                    padLeft - 4.dp.toPx(),
                    y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color     = labelColor.toArgb()
                        textSize  = 9.dp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // X-метки
            val step = if (labels.size > 7) labels.size / 6 else 1
            labels.forEachIndexed { idx, lbl ->
                if (idx % step == 0 || idx == labels.lastIndex) {
                    val x = padLeft + chartW * idx / (labels.size - 1).coerceAtLeast(1)
                    drawContext.canvas.nativeCanvas.drawText(
                        lbl, x, h - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color     = labelColor.toArgb()
                            textSize  = 9.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // Серии
            series.forEachIndexed { si, points ->
                if (points.isEmpty()) return@forEachIndexed
                val color        = colors[si % colors.size]
                val n            = points.size
                val visibleCount = (n * p).roundToInt().coerceIn(2, n)

                fun xOf(i: Int)   = padLeft + chartW * i / (n - 1).coerceAtLeast(1)
                fun yOf(v: Float) = padTop + chartH * (1f - v / maxVal)

                val linePath = Path()
                for (i in 0 until visibleCount) {
                    val x = xOf(i); val y = yOf(points[i])
                    if (i == 0) linePath.moveTo(x, y)
                    else {
                        val px = xOf(i - 1); val py = yOf(points[i - 1])
                        val cx = (px + x) / 2
                        linePath.cubicTo(cx, py, cx, y, x, y)
                    }
                }

                if (filled) {
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(xOf(visibleCount - 1), padTop + chartH)
                        lineTo(padLeft, padTop + chartH)
                        close()
                    }
                    clipRect(padLeft, padTop, w - padRight, padTop + chartH) {
                        drawPath(fillPath, brush = Brush.verticalGradient(
                            colors = listOf(color.copy(0.35f), color.copy(0.02f)),
                            startY = padTop, endY = padTop + chartH
                        ))
                    }
                }

                clipRect(padLeft, padTop, w - padRight, padTop + chartH) {
                    drawPath(linePath, color = color, style = Stroke(
                        width = if (filled) 2.dp.toPx() else 1.8.dp.toPx(),
                        cap   = StrokeCap.Round, join = StrokeJoin.Round
                    ))
                }

                // Точки — только если данных немного
                if (n <= 30) {
                    for (i in 0 until visibleCount) {
                        drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(xOf(i), yOf(points[i])))
                        drawCircle(color = Color.Black.copy(0.4f), radius = 1.5.dp.toPx(), center = Offset(xOf(i), yOf(points[i])))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Кликабельный бар-чарт
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ClickableBarChart(
    items: List<BarItem>,
    color: Color,
    suffix: String = "",
    maxValue: Int? = null,
    onItemClick: (BarItem) -> Unit
) {
    val maxVal = maxValue?.toFloat() ?: items.maxOfOrNull { it.value.toFloat() }?.coerceAtLeast(1f) ?: 1f
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val anim = remember { Animatable(0f) }
    LaunchedEffect(items) { anim.animateTo(1f, tween(1000)) }
    val p = anim.value

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        items.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 4.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        item.label,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${item.value}$suffix",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = color
                        )
                        Icon(
                            Icons.Outlined.ChevronRight, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                val fraction = (item.value / maxVal * p).coerceIn(0f, 1f)
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                    drawRoundRect(color = surfaceVariant, size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(4.dp.toPx()))
                    val filled = size.width * fraction
                    if (filled > 0f) {
                        drawRoundRect(
                            brush        = Brush.horizontalGradient(
                                colors = listOf(color.copy(0.6f), color), startX = 0f, endX = filled
                            ),
                            size         = Size(filled, size.height),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        drawRoundRect(
                            brush        = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(0.25f), Color.Transparent)
                            ),
                            size         = Size(filled, size.height / 2),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }
            if (index < items.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Пончик
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun DonutChart(
    slices: List<PieSlice>,
    centerLabel: String,
    centerSub: String,
    size: Int = 130
) {
    val total = slices.sumOf { it.count }.takeIf { it > 0 } ?: 1
    val anim  = remember { Animatable(0f) }
    LaunchedEffect(slices) { anim.animateTo(1f, tween(900)) }
    val p = anim.value

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(size.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Butt)
                val pad    = 10.dp.toPx()
                val arc    = Size(this.size.width - pad * 2, this.size.height - pad * 2)
                val tl     = Offset(pad, pad)
                drawArc(color = ColorGray.copy(0.12f), 0f, 360f, false, tl, arc, style = stroke)
                var start = -90f
                slices.forEach { s ->
                    val sweep = 360f * s.count / total * p
                    drawArc(Color(s.color.toInt()), start, sweep, false, tl, arc, style = stroke)
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerLabel, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(centerSub, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.forEach { s ->
                LegendDot(Color(s.color.toInt()), s.label, s.count,
                    "${(s.count * 100f / total).roundToInt()}%")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Пирог с легендой
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun PieChartWithLegend(slices: List<PieSlice>) {
    val total = slices.sumOf { it.count }.takeIf { it > 0 } ?: 1
    val anim  = remember { Animatable(0f) }
    LaunchedEffect(slices) { anim.animateTo(1f, tween(900)) }
    val p = anim.value

    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Canvas(modifier = Modifier.size(110.dp)) {
            var start = -90f
            val pad   = 6.dp.toPx()
            val arc   = Size(size.width - pad * 2, size.height - pad * 2)
            val tl    = Offset(pad, pad)
            slices.forEach { s ->
                val sweep = 360f * s.count / total * p
                drawArc(Color(s.color.toInt()), start, sweep, true, tl, arc)
                start += sweep
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            slices.forEach { s ->
                LegendDot(Color(s.color.toInt()), s.label, s.count,
                    "${(s.count * 100f / total).roundToInt()}%")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Круговой индикатор заполненности
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FillRateGauge(fillRate: Float, participants: Int, avgFill: Float) {
    val color = when {
        fillRate >= 80f -> ColorRed
        fillRate >= 50f -> ColorAmber
        else            -> ColorGreen
    }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(fillRate) { anim.animateTo(fillRate / 100f, tween(1200)) }
    val p = anim.value

    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                val pad    = 10.dp.toPx()
                val arc    = Size(size.width - pad * 2, size.height - pad * 2)
                val tl     = Offset(pad, pad)
                drawArc(color = color.copy(0.12f), 135f, 270f, false, tl, arc, style = stroke)
                if (p > 0f) drawArc(color = color, 135f, 270f * p, false, tl, arc, style = stroke)
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
// Вспомогательные UI-компоненты
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MetricTile(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f), tonalElevation = 0.dp) {
        Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color,
                maxLines = 1, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = color.copy(0.8f),
                textAlign = TextAlign.Center, lineHeight = 12.sp, maxLines = 2)
        }
    }
}

@Composable
private fun DashCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(3.dp).height(16.dp)
                    .background(accentColor, RoundedCornerShape(2.dp)))
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(15.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.2.sp)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SmallMetric(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = color.copy(0.12f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, count: Int, suffix: String = "") {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(
            text     = if (suffix.isNotEmpty()) "$label — $count ($suffix)" else "$label — $count",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}