package com.events.app.ui.views.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.local.EventLocationCache
import com.events.app.data.local.EventRefreshBus
import com.events.app.domain.models.events.Event
import com.events.app.domain.repositories.auth.AuthRepository
import com.events.app.domain.usecases.events.GetEventsUseCase
import com.events.app.domain.usecases.events.GetRecentEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val getRecentEventsUseCase: GetRecentEventsUseCase,
    private val locationCache: EventLocationCache,
    private val authRepository: AuthRepository,
    private val refreshBus: EventRefreshBus
) : ViewModel() {

    // ── Ближайшие предстоящие (все, без фильтра по пользователю) ──

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val events = _upcomingEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // ── Созданные текущим пользователем ───────────────────────────
    // ИСПРАВЛЕНО: раньше это был тот же список что и "Ближайшие".
    // Теперь запрашивается отдельно с серверным фильтром UserId.

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
        loadUpcomingEvents()
        loadMyEvents()
        loadRecentEvents()

        // Слушаем шину: когда создаётся новое мероприятие — обновляем все списки
        viewModelScope.launch {
            refreshBus.events.collect {
                loadUpcomingEvents()
                loadMyEvents()
                loadRecentEvents()
            }
        }
    }

    fun refresh() {
        loadUpcomingEvents()
        loadMyEvents()
        loadRecentEvents()
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loaded = getEventsUseCase(size = 20, page = 1)
                _upcomingEvents.value = loaded.enrichWithCache()
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

    fun loadMyEvents() {
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            _myEventsLoading.value = true
            try {
                // Серверный фильтр по UserId — точный список мероприятий пользователя
                val loaded = getEventsUseCase(size = 20, page = 1, userId = userId)
                _myEvents.value = loaded.enrichWithCache()
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
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            _recentLoading.value = true
            try {
                val recent = getRecentEventsUseCase(userId)
                _recentEvents.value = recent.enrichWithCache()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _recentEvents.value = emptyList()
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