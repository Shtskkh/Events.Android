package com.events.app.ui.views.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    // --- Список мероприятий ---
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    private val _displayedEvents = MutableStateFlow<List<Event>>(emptyList())
    val displayedEvents = _displayedEvents.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20
    private val displayStep = 5

    // --- Справочники ---
    private val _eventTypes = MutableStateFlow<List<EventTypeDto>>(emptyList())
    val eventTypes = _eventTypes.asStateFlow()

    private val _eventFormats = MutableStateFlow<List<EventFormatDto>>(emptyList())
    val eventFormats = _eventFormats.asStateFlow()

    init {
        loadEvents()
        loadReferenceData()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            try { _eventTypes.value = remoteDataSource.getEventTypes() } catch (_: Exception) {}
            try { _eventFormats.value = remoteDataSource.getEventFormats() } catch (_: Exception) {}
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val events = getEventsUseCase(size = pageSize, page = currentPage)
                _allEvents.value = events
                _displayedEvents.value = events.take(displayStep)
                _hasMore.value = events.size > displayStep
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        val all = _allEvents.value
        val displayed = _displayedEvents.value
        if (displayed.size < all.size) {
            val next = min(displayed.size + displayStep, all.size)
            _displayedEvents.value = all.take(next)
            _hasMore.value = next < all.size
        }
    }
}