package com.events.app.ui.views.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.local.EventLocationCache
import com.events.app.data.local.EventRefreshBus
import com.events.app.data.local.RecentEventsCache
import com.events.app.domain.models.events.Event
import com.events.app.domain.repositories.auth.AuthRepository
import com.events.app.domain.usecases.events.GetEventByIdUseCase
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val locationCache: EventLocationCache,
    private val recentEventsCache: RecentEventsCache,
    private val authRepository: AuthRepository,
    private val refreshBus: EventRefreshBus
) : ViewModel() {

    // ── Ближайшие ─────────────────────────────────────────────────
    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val events = _upcomingEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // ── Завершённые ───────────────────────────────────────────────
    private val _completedEvents = MutableStateFlow<List<Event>>(emptyList())
    val completedEvents = _completedEvents.asStateFlow()

    private val _completedLoading = MutableStateFlow(false)
    val completedLoading = _completedLoading.asStateFlow()

    // ── Созданные мной ────────────────────────────────────────────
    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents = _myEvents.asStateFlow()

    private val _myEventsLoading = MutableStateFlow(false)
    val myEventsLoading = _myEventsLoading.asStateFlow()

    // ── Недавно просмотренные ─────────────────────────────────────
    private val _recentEvents = MutableStateFlow<List<Event>>(emptyList())
    val recentEvents = _recentEvents.asStateFlow()

    private val _recentLoading = MutableStateFlow(false)
    val recentLoading = _recentLoading.asStateFlow()

    init {
        loadAll()
        viewModelScope.launch {
            refreshBus.events.collect { loadAll() }
        }
    }

    fun refresh() = loadAll()

    private fun loadAll() {
        loadUpcomingEvents()
        loadCompletedEvents()
        loadMyEvents()
        loadRecentEvents()
    }

    /** Вызывается при открытии карточки — записывает просмотр и обновляет список */
    fun onEventViewed(eventId: String) {
        recentEventsCache.recordView(eventId)
        loadRecentEvents()
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val nowIso = LocalDateTime.now().format(ISO_FMT)
                val loaded = getEventsUseCase(size = 20, page = 1, startDateTime = nowIso)
                _upcomingEvents.value = loaded
                    .filter { !it.isFinished }
                    .sortedBy { it.startDate }
                    .enrichWithCache()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _upcomingEvents.value = emptyList()
                else _error.value = "Ошибка сервера: ${e.code()}"
            } catch (e: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCompletedEvents() {
        viewModelScope.launch {
            _completedLoading.value = true
            try {
                val nowIso = LocalDateTime.now().format(ISO_FMT)
                val loaded = getEventsUseCase(size = 20, page = 1, endDateTime = nowIso)
                _completedEvents.value = loaded
                    .filter { it.isFinished }
                    .sortedByDescending { it.startDate }
                    .enrichWithCache()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _completedEvents.value = emptyList()
            } catch (_: Exception) {
                _completedEvents.value = emptyList()
            } finally {
                _completedLoading.value = false
            }
        }
    }

    fun loadMyEvents() {
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            _myEventsLoading.value = true
            try {
                val loaded = getEventsUseCase(size = 20, page = 1, userId = userId)
                _myEvents.value = loaded
                    .filter { !it.isFinished }
                    .sortedBy { it.startDate }
                    .enrichWithCache()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _myEvents.value = emptyList()
            } catch (_: Exception) {
                _myEvents.value = emptyList()
            } finally {
                _myEventsLoading.value = false
            }
        }
    }

    fun loadRecentEvents() {
        val ids = recentEventsCache.getRecentIds()
        if (ids.isEmpty()) {
            _recentEvents.value = emptyList()
            return
        }
        viewModelScope.launch {
            _recentLoading.value = true
            try {
                // Загружаем каждое мероприятие по ID, сохраняем порядок
                val loaded = ids.mapNotNull { id ->
                    try { getEventByIdUseCase(id) } catch (_: Exception) { null }
                }
                _recentEvents.value = loaded.enrichWithCache()
            } catch (_: Exception) {
                _recentEvents.value = emptyList()
            } finally {
                _recentLoading.value = false
            }
        }
    }

    private fun List<Event>.enrichWithCache(): List<Event> = map { event ->
        if (event.location.isBlank()) {
            val cached = locationCache.get(event.id)
            if (!cached.isNullOrBlank()) event.copy(location = cached) else event
        } else event
    }
}