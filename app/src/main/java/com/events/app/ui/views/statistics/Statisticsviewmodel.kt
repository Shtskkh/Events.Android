package com.events.app.ui.views.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.ViewsDto
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

// ── Модели ────────────────────────────────────────────────────────

data class PieSlice(val label: String, val count: Int, val color: Long)

data class BarItem(val label: String, val value: Int, val eventId: String = "")

data class ViewPoint(val date: String, val views: Int)

data class StatisticsData(
    val total: Int,
    val upcoming: Int,
    val finished: Int,
    val withRegistration: Int,
    val withoutRegistration: Int,
    val totalViews: Int,
    val totalParticipants: Int,
    val avgParticipants: Float,
    val fillRate: Float,
    val byType: List<PieSlice>,
    val byFormat: List<PieSlice>,
    val registrationPie: List<PieSlice>,
    val topByViews: List<BarItem>,
    val topByParticipants: List<BarItem>,
    val topByFillRate: List<BarItem>,
    val viewsTimeSeries: List<ViewPoint>,
    /** Triple(eventId, displayTitle, series) */
    val topEventsTimeSeries: List<Triple<String, String, List<ViewPoint>>>
)

// ── ViewModel ─────────────────────────────────────────────────────

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _stats = MutableStateFlow<StatisticsData?>(null)
    val stats = _stats.asStateFlow()

    private val palette = listOf(
        0xFF3B5BDB, 0xFF22C55E, 0xFFEF4444, 0xFFF59E0B,
        0xFF8B5CF6, 0xFF06B6D4, 0xFFEC4899, 0xFF10B981,
        0xFFFF6B35, 0xFF6366F1
    )

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val events = loadAllEvents()

                val analyticsResults = events.map { event ->
                    async {
                        try {
                            val a = remoteDataSource.getEventAnalytics(event.id)
                            Triple(event.id, event.title, a)
                        } catch (_: Exception) {
                            Triple(event.id, event.title, null)
                        }
                    }
                }.awaitAll()

                _stats.value = aggregate(events, analyticsResults)

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _stats.value = emptyStats()
                else _error.value = "Ошибка загрузки: ${e.code()}"
            } catch (e: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadAllEvents(): List<Event> {
        val result = mutableListOf<Event>()
        var page = 1
        while (true) {
            val chunk = try {
                getEventsUseCase(size = 30, page = page)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) break else throw e
            }
            result.addAll(chunk)
            if (chunk.size < 30) break
            page++
        }
        return result
    }

    private fun aggregate(
        events: List<Event>,
        analyticsResults: List<Triple<String, String, com.events.app.data.remote.dto.EventAnalyticDto?>>
    ): StatisticsData {

        val total      = events.size
        val upcoming   = events.count { !it.isFinished }
        val finished   = events.count { it.isFinished }
        val withReg    = events.count { it.needsRegistration }
        val withoutReg = events.count { !it.needsRegistration }

        var totalViews = 0
        var totalParticipants = 0
        var sumFill = 0f
        var fillCount = 0

        data class Enriched(
            val id: String, val title: String,
            val views: Int, val participants: Int,
            val fillPct: Int?,
            val timeSeries: List<ViewsDto>
        )

        val enriched = analyticsResults.map { (id, title, a) ->
            val event    = events.find { it.id == id }
            val views    = a?.viewsCount ?: 0
            val part     = a?.participantsCount ?: 0
            val maxP     = event?.maxParticipants ?: a?.maxParticipantsCount
            val fillPct  = if (maxP != null && maxP > 0)
                (part * 100f / maxP).roundToInt().coerceIn(0, 100) else null
            totalViews += views
            totalParticipants += part
            if (fillPct != null) { sumFill += fillPct; fillCount++ }
            Enriched(id, title, views, part, fillPct, a?.views ?: emptyList())
        }

        // ── Временные ряды ───────────────────────────────────────
        val viewsByDate = mutableMapOf<String, Int>()
        enriched.forEach { e -> e.timeSeries.forEach { v ->
            viewsByDate[v.date] = (viewsByDate[v.date] ?: 0) + v.views
        }}
        val globalTs = viewsByDate.entries.sortedBy { it.key }
            .map { ViewPoint(it.key, it.value) }

        // Топ-3 по просмотрам → Triple(eventId, shortTitle, series)
        val topEventsTs = enriched
            .filter { it.timeSeries.isNotEmpty() }
            .sortedByDescending { it.views }
            .take(3)
            .map { e ->
                val series = e.timeSeries.sortedBy { it.date }.map { ViewPoint(it.date, it.views) }
                Triple(e.id, e.title.take(20), series)
            }

        // ── Диаграммы ─────────────────────────────────────────────
        val byType = events.groupBy { it.type.ifBlank { "Не указан" } }
            .entries.mapIndexed { i, (l, list) ->
                PieSlice(l, list.size, palette[i % palette.size])
            }.sortedByDescending { it.count }

        val byFormat = events.groupBy { it.format.ifBlank { "Не указан" } }
            .entries.mapIndexed { i, (l, list) ->
                PieSlice(l, list.size, palette[(i + 3) % palette.size])
            }.sortedByDescending { it.count }

        val regPie = listOf(
            PieSlice("С регистрацией",  withReg,    0xFF3B5BDB),
            PieSlice("Без регистрации", withoutReg, 0xFF9E9E9E)
        ).filter { it.count > 0 }

        // ── Топы — BarItem содержит eventId для навигации ─────────
        val topViews = enriched
            .filter { it.views > 0 }
            .sortedByDescending { it.views }
            .take(5)
            .map { BarItem(it.title.take(26), it.views, it.id) }

        val topPart = enriched
            .filter { it.participants > 0 }
            .sortedByDescending { it.participants }
            .take(5)
            .map { BarItem(it.title.take(26), it.participants, it.id) }

        val topFill = enriched
            .filter { it.fillPct != null && it.fillPct > 0 }
            .sortedByDescending { it.fillPct }
            .take(5)
            .map { BarItem(it.title.take(26), it.fillPct!!, it.id) }

        return StatisticsData(
            total = total, upcoming = upcoming, finished = finished,
            withRegistration = withReg, withoutRegistration = withoutReg,
            totalViews = totalViews, totalParticipants = totalParticipants,
            avgParticipants = if (withReg > 0) totalParticipants.toFloat() / withReg else 0f,
            fillRate = if (fillCount > 0) sumFill / fillCount else 0f,
            byType = byType, byFormat = byFormat, registrationPie = regPie,
            topByViews = topViews, topByParticipants = topPart, topByFillRate = topFill,
            viewsTimeSeries = globalTs,
            topEventsTimeSeries = topEventsTs
        )
    }

    private fun emptyStats() = StatisticsData(
        total = 0, upcoming = 0, finished = 0,
        withRegistration = 0, withoutRegistration = 0,
        totalViews = 0, totalParticipants = 0,
        avgParticipants = 0f, fillRate = 0f,
        byType = emptyList(), byFormat = emptyList(), registrationPie = emptyList(),
        topByViews = emptyList(), topByParticipants = emptyList(), topByFillRate = emptyList(),
        viewsTimeSeries = emptyList(), topEventsTimeSeries = emptyList()
    )
}