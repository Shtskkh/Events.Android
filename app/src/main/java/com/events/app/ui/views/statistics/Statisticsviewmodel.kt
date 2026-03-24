package com.events.app.ui.views.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PieSlice(val label: String, val count: Int, val color: Long)

data class StatisticsData(
    // Мероприятия по типам
    val byType: List<PieSlice>,
    // Мероприятия по форматам
    val byFormat: List<PieSlice>,
    // Предстоящие vs завершённые
    val upcoming: Int,
    val finished: Int,
    // Топ-5 по макс
    val topByParticipants: List<Pair<String, Int>>,
    // Всего мероприятий
    val total: Int
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _stats = MutableStateFlow<StatisticsData?>(null)
    val stats = _stats.asStateFlow()

    // Цвета для диаграмм (ARGB hex)
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
                // Грузим максимум событий (30 — лимит API)
                val events = getEventsUseCase(size = 30, page = 1)
                _stats.value = aggregate(events)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _stats.value = emptyStats()
                } else {
                    _error.value = "Ошибка загрузки: ${e.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun aggregate(events: List<Event>): StatisticsData {
        // По типам
        val typeGroups = events.groupBy { it.type.ifBlank { "Не указан" } }
        val byType = typeGroups.entries.mapIndexed { i, (label, list) ->
            PieSlice(label, list.size, palette[i % palette.size])
        }.sortedByDescending { it.count }

        // По форматам
        val formatGroups = events.groupBy { it.format.ifBlank { "Не указан" } }
        val byFormat = formatGroups.entries.mapIndexed { i, (label, list) ->
            PieSlice(label, list.size, palette[(i + 2) % palette.size])
        }.sortedByDescending { it.count }

        // Статус
        val upcoming = events.count { !it.isFinished }
        val finished = events.count { it.isFinished }

        // Топ-5 по maxParticipants
        val top = events
            .filter { (it.maxParticipants ?: 0) > 0 }
            .sortedByDescending { it.maxParticipants }
            .take(5)
            .map { Pair(it.title.take(24), it.maxParticipants ?: 0) }

        return StatisticsData(
            byType               = byType,
            byFormat             = byFormat,
            upcoming             = upcoming,
            finished             = finished,
            topByParticipants    = top,
            total                = events.size
        )
    }

    private fun emptyStats() = StatisticsData(
        byType = emptyList(), byFormat = emptyList(),
        upcoming = 0, finished = 0, topByParticipants = emptyList(), total = 0
    )
}