package com.events.app.ui.views.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.min

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()
    val stats     by viewModel.stats.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            error != null -> Column(
                modifier            = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { viewModel.loadStats() }) {
                    Icon(Icons.Outlined.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Повторить")
                }
            }

            stats != null && stats!!.total == 0 -> Column(
                modifier            = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Event, null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f),
                    modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Нет данных для статистики",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                    fontSize = 15.sp)
            }

            stats != null -> StatisticsContent(stats = stats!!)
        }
    }
}

@Composable
private fun StatisticsContent(stats: StatisticsData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Сводка ────────────────────────────────────────────────
        SummaryRow(stats)

        Spacer(Modifier.height(20.dp))

        // ── Статус: предстоящие vs завершённые ────────────────────
        StatCard(title = "Статус мероприятий") {
            StatusDonut(
                upcoming = stats.upcoming,
                finished = stats.finished
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── По типам ──────────────────────────────────────────────
        if (stats.byType.isNotEmpty()) {
            StatCard(title = "По типам") {
                PieChartWithLegend(slices = stats.byType)
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── По форматам ───────────────────────────────────────────
        if (stats.byFormat.isNotEmpty()) {
            StatCard(title = "По форматам") {
                PieChartWithLegend(slices = stats.byFormat)
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Топ по участникам ─────────────────────────────────────
        if (stats.topByParticipants.isNotEmpty()) {
            StatCard(title = "Топ по участникам") {
                BarChart(items = stats.topByParticipants)
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Сводные плитки ────────────────────────────────────────────────

@Composable
private fun SummaryRow(stats: StatisticsData) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryTile("Всего",        stats.total.toString(),    Color(0xFF3B5BDB), Modifier.weight(1f))
        SummaryTile("Предстоящих", stats.upcoming.toString(),  Color(0xFF22C55E), Modifier.weight(1f))
        SummaryTile("Завершённых", stats.finished.toString(),  Color(0xFF9E9E9E), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier      = modifier,
        shape         = RoundedCornerShape(16.dp),
        color         = color.copy(alpha = 0.10f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

// ── Обёртка карточки ──────────────────────────────────────────────

@Composable
private fun StatCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(20.dp),
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.3.sp)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── Пончик: предстоящие vs завершённые ───────────────────────────

@Composable
private fun StatusDonut(upcoming: Int, finished: Int) {
    val total = (upcoming + finished).takeIf { it > 0 } ?: 1
    val upcomingAngle = 360f * upcoming / total
    val finishedAngle = 360f * finished / total

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animProgress.animateTo(1f, tween(800)) }
    val p = animProgress.value

    val colorUpcoming = Color(0xFF22C55E)
    val colorFinished = Color(0xFF9E9E9E)

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier        = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke  = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Butt)
                val padding = 14.dp.toPx()
                val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                val topLeft = Offset(padding, padding)

                // Фон
                drawArc(color = colorFinished.copy(0.15f), startAngle = 0f, sweepAngle = 360f,
                    useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)

                // Предстоящие
                drawArc(color = colorUpcoming, startAngle = -90f,
                    sweepAngle = upcomingAngle * p,
                    useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)

                // Завершённые
                drawArc(color = colorFinished, startAngle = -90f + upcomingAngle * p,
                    sweepAngle = finishedAngle * p,
                    useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$total", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text("всего", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendItem(colorUpcoming, "Предстоящие", upcoming)
            LegendItem(colorFinished, "Завершённые", finished)
        }
    }
}

// ── Пирог с легендой ─────────────────────────────────────────────

@Composable
private fun PieChartWithLegend(slices: List<PieSlice>) {
    val total = slices.sumOf { it.count }.takeIf { it > 0 } ?: 1

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) { animProgress.animateTo(1f, tween(900)) }
    val p = animProgress.value

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var startAngle = -90f
            val padding = 8.dp.toPx()
            val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
            val topLeft = Offset(padding, padding)

            slices.forEach { slice ->
                val sweep = 360f * slice.count / total * p
                drawArc(
                    color      = Color(slice.color.toInt()),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter  = true,
                    topLeft    = topLeft,
                    size       = arcSize
                )
                startAngle += sweep
            }
        }

        Column(
            modifier              = Modifier.weight(1f),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEach { slice ->
                val pct = (slice.count * 100f / total).toInt()
                LegendItem(Color(slice.color.toInt()), slice.label, slice.count, "$pct%")
            }
        }
    }
}

// ── Горизонтальная столбчатая диаграмма ──────────────────────────

@Composable
private fun BarChart(items: List<Pair<String, Int>>) {
    val maxVal = items.maxOf { it.second }.takeIf { it > 0 } ?: 1
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(items) { animProgress.animateTo(1f, tween(900)) }
    val p = animProgress.value

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (label, value) ->
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(8.dp))
                    Text("$value", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = primary)
                }
                Spacer(Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    // Фоновая дорожка
                    drawRoundRect(
                        color        = surfaceVariant,
                        size         = Size(size.width, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                    )
                    // Заполненная часть
                    val filledWidth = size.width * (value.toFloat() / maxVal) * p
                    if (filledWidth > 0f) {
                        drawRoundRect(
                            color        = primary,
                            size         = Size(filledWidth, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

// ── Строка легенды ────────────────────────────────────────────────

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int,
    suffix: String = ""
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text     = if (suffix.isNotEmpty()) "$label — $count ($suffix)"
            else "$label — $count",
            fontSize = 12.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}