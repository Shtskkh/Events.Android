package com.events.app.ui.views.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.local.EventLocationCache
import com.events.app.data.local.EventRefreshBus
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val UUID_REGEX = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

private fun String.looksLikeUuid(): Boolean {
    val clean = trim()
    return UUID_REGEX.matches(clean) ||
            (clean.length >= 8 && clean.all { it.isLetterOrDigit() || it == '-' } && clean.contains('-'))
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val remoteDataSource: RemoteDataSource,
    private val locationCache: EventLocationCache,
    private val refreshBus: EventRefreshBus
) : ViewModel() {

    private val _allLoadedEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _displayedEvents = MutableStateFlow<List<Event>>(emptyList())
    val displayedEvents = _displayedEvents.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentPage = 1
    private val pageSize    = 20

    // ── Фильтры ───────────────────────────────────────────────────

    private val _searchText = MutableStateFlow<String?>(null)
    val searchText = _searchText.asStateFlow()

    /** Фильтр по дате НАЧАЛА мероприятия (StartDateTime) */
    private val _filterStartDate = MutableStateFlow<String?>(null)
    val filterStartDate = _filterStartDate.asStateFlow()

    /** Фильтр по дате КОНЦА мероприятия (EndDateTime) */
    private val _filterEndDate = MutableStateFlow<String?>(null)
    val filterEndDate = _filterEndDate.asStateFlow()

    /** Фильтр по дате СОЗДАНИЯ: после (CreatedAfter) */
    private val _filterCreatedAfter = MutableStateFlow<String?>(null)
    val filterCreatedAfter = _filterCreatedAfter.asStateFlow()

    /** Фильтр по дате СОЗДАНИЯ: до (CreatedBefore) */
    private val _filterCreatedBefore = MutableStateFlow<String?>(null)
    val filterCreatedBefore = _filterCreatedBefore.asStateFlow()

    private val _filterTypeId = MutableStateFlow<Int?>(null)
    val filterTypeId = _filterTypeId.asStateFlow()

    private val _filterFormatId = MutableStateFlow<Int?>(null)
    val filterFormatId = _filterFormatId.asStateFlow()

    // ── Справочники ───────────────────────────────────────────────

    private val _eventTypes = MutableStateFlow<List<EventTypeDto>>(emptyList())
    val eventTypes = _eventTypes.asStateFlow()

    private val _eventFormats = MutableStateFlow<List<EventFormatDto>>(emptyList())
    val eventFormats = _eventFormats.asStateFlow()

    init {
        loadEvents(reset = true)
        loadReferenceData()
        viewModelScope.launch {
            refreshBus.events.collect { resetAllFilters() }
        }
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            try { _eventTypes.value   = remoteDataSource.getEventTypes()   } catch (_: Exception) {}
            try { _eventFormats.value = remoteDataSource.getEventFormats() } catch (_: Exception) {}
        }
    }

    // ── Публичные методы изменения фильтров ───────────────────────

    fun setSearchText(text: String?) {
        val trimmed = text?.trim()
        if (!trimmed.isNullOrBlank() && trimmed.looksLikeUuid()) {
            _displayedEvents.value = _allLoadedEvents.value
                .filter { it.id.contains(trimmed, ignoreCase = true) }
            _hasMore.value = false
            return
        }
        _searchText.value = trimmed?.takeIf { it.length >= 2 }
        loadEvents(reset = true)
    }

    /** Установить фильтр по дате начала/конца мероприятия (StartDateTime / EndDateTime) */
    fun setDateFilter(start: String?, end: String?) {
        _filterStartDate.value = start
        _filterEndDate.value   = end
        loadEvents(reset = true)
    }

    /** Установить фильтр по дате создания (CreatedAfter / CreatedBefore) */
    fun setCreatedDateFilter(after: String?, before: String?) {
        _filterCreatedAfter.value  = after
        _filterCreatedBefore.value = before
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

    fun applyAllFilters(
        text: String?,
        startDate: String?,
        endDate: String?,
        createdAfter: String?,
        createdBefore: String?,
        typeId: Int?,
        formatId: Int?
    ) {
        val trimmed = text?.trim()
        _searchText.value          = trimmed?.takeIf { it.length >= 2 }
        _filterStartDate.value     = startDate
        _filterEndDate.value       = endDate
        _filterCreatedAfter.value  = createdAfter
        _filterCreatedBefore.value = createdBefore
        _filterTypeId.value        = typeId
        _filterFormatId.value      = formatId
        loadEvents(reset = true)
    }

    fun resetAllFilters() {
        _searchText.value          = null
        _filterStartDate.value     = null
        _filterEndDate.value       = null
        _filterCreatedAfter.value  = null
        _filterCreatedBefore.value = null
        _filterTypeId.value        = null
        _filterFormatId.value      = null
        loadEvents(reset = true)
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        currentPage++
        loadEvents(reset = false)
    }

    private fun loadEvents(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            _displayedEvents.value = emptyList()
            _allLoadedEvents.value = emptyList()
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null
            try {
                val events = getEventsUseCase(
                    size          = pageSize,
                    page          = currentPage,
                    text          = _searchText.value,
                    startDateTime = _filterStartDate.value,
                    endDateTime   = _filterEndDate.value,
                    typeId        = _filterTypeId.value,
                    formatId      = _filterFormatId.value,
                    createdAfter  = _filterCreatedAfter.value,
                    createdBefore = _filterCreatedBefore.value
                )

                val enriched = events.map { event ->
                    if (event.location.isBlank()) {
                        val cached = locationCache.get(event.id)
                        if (!cached.isNullOrBlank()) event.copy(location = cached) else event
                    } else event
                }

                val merged = if (reset) enriched else _allLoadedEvents.value + enriched
                _allLoadedEvents.value = merged
                _displayedEvents.value = merged
                _hasMore.value         = events.size >= pageSize
                if (events.size >= pageSize) currentPage++

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _displayedEvents.value = emptyList()
                    _hasMore.value         = false
                } else {
                    _error.value = "Ошибка сервера: ${e.code()}"
                }
            } catch (_: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _isLoading.value = false
            }
        }
    }
}