package com.events.app.ui.views.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.local.EventLocationCache
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // ── Недавно просмотренные ──────────────────────────────────────

    private val _recentEvents = MutableStateFlow<List<Event>>(emptyList())
    val recentEvents = _recentEvents.asStateFlow()

    private val _recentLoading = MutableStateFlow(false)
    val recentLoading = _recentLoading.asStateFlow()

    init {
        loadEvents()
        loadRecentEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loadedEvents = getEventsUseCase(size = 20, page = 1)
                _events.value = loadedEvents.map { event ->
                    if (event.location.isBlank()) {
                        val cached = locationCache.get(event.id)
                        if (!cached.isNullOrBlank()) event.copy(location = cached) else event
                    } else event
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _events.value = emptyList()
                } else {
                    _error.value = "Ошибка сервера: ${e.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecentEvents() {
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            _recentLoading.value = true
            try {
                val recent = getRecentEventsUseCase(userId)
                _recentEvents.value = recent.map { event ->
                    if (event.location.isBlank()) {
                        val cached = locationCache.get(event.id)
                        if (!cached.isNullOrBlank()) event.copy(location = cached) else event
                    } else event
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _recentEvents.value = emptyList()
                // остальные ошибки — некритично для главной
            } catch (_: Exception) {
                _recentEvents.value = emptyList()
            } finally {
                _recentLoading.value = false
            }
        }
    }
}