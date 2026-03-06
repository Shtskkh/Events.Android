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
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    // ── Список мероприятий ─────────────────────────────────────
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

    // ── Активные фильтры ───────────────────────────────────────
    private val _searchText = MutableStateFlow<String?>(null)
    val searchText = _searchText.asStateFlow()

    private val _filterStartDate = MutableStateFlow<String?>(null)
    val filterStartDate = _filterStartDate.asStateFlow()

    private val _filterEndDate = MutableStateFlow<String?>(null)
    val filterEndDate = _filterEndDate.asStateFlow()

    private val _filterTypeId = MutableStateFlow<Int?>(null)
    val filterTypeId = _filterTypeId.asStateFlow()

    private val _filterFormatId = MutableStateFlow<Int?>(null)
    val filterFormatId = _filterFormatId.asStateFlow()

    // ── Справочники ────────────────────────────────────────────
    private val _eventTypes = MutableStateFlow<List<EventTypeDto>>(emptyList())
    val eventTypes = _eventTypes.asStateFlow()

    private val _eventFormats = MutableStateFlow<List<EventFormatDto>>(emptyList())
    val eventFormats = _eventFormats.asStateFlow()

    init {
        loadEvents(reset = true)
        loadReferenceData()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            try { _eventTypes.value = remoteDataSource.getEventTypes() } catch (_: Exception) {}
            try { _eventFormats.value = remoteDataSource.getEventFormats() } catch (_: Exception) {}
        }
    }

    private fun loadEvents(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            _displayedEvents.value = emptyList()
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val events = getEventsUseCase(
                    size = pageSize,
                    page = currentPage,
                    text = _searchText.value,
                    startDateTime = _filterStartDate.value,
                    endDateTime = _filterEndDate.value,
                    typeId = _filterTypeId.value,
                    formatId = _filterFormatId.value
                )
                _displayedEvents.value = if (reset) events
                else _displayedEvents.value + events
                _hasMore.value = events.size >= pageSize
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _displayedEvents.value = emptyList()
                    _hasMore.value = false
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

    // ── Быстрые фильтры — применяются сразу ───────────────────
    fun setDateFilter(start: String?, end: String?) {
        _filterStartDate.value = start
        _filterEndDate.value = end
        loadEvents(reset = true)
    }

    fun setTypeFilter(typeId: Int?) {
        _filterTypeId.value = typeId
        loadEvents(reset = true)
    }

    fun setFormatFilter(formatId: Int?) {
        _filterFormatId.value = formatId
        loadEvents(reset = true)
    }

    fun setSearchText(text: String?) {
        _searchText.value = text?.ifBlank { null }
        loadEvents(reset = true)
    }

    // ── Все фильтры — по кнопке ────────────────────────────────
    fun applyAllFilters(
        text: String?,
        startDate: String?,
        endDate: String?,
        typeId: Int?,
        formatId: Int?
    ) {
        _searchText.value = text?.ifBlank { null }
        _filterStartDate.value = startDate
        _filterEndDate.value = endDate
        _filterTypeId.value = typeId
        _filterFormatId.value = formatId
        loadEvents(reset = true)
    }

    fun resetAllFilters() {
        _searchText.value = null
        _filterStartDate.value = null
        _filterEndDate.value = null
        _filterTypeId.value = null
        _filterFormatId.value = null
        loadEvents(reset = true)
    }

    // ── Пагинация ──────────────────────────────────────────────
    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        currentPage++
        loadEvents(reset = false)
    }
}