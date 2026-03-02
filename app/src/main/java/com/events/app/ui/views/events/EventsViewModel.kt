package com.events.app.ui.views.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    private val _displayedEvents = MutableStateFlow<List<Event>>(emptyList())
    val displayedEvents = _displayedEvents.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    // Новые состояния для ошибок и загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loadedEvents = getEventsUseCase(size = pageSize, page = currentPage)
                _allEvents.value = loadedEvents
                _displayedEvents.value = loadedEvents
                // Если вернулось меньше чем pageSize — значит страниц больше нет
                _hasMore.value = loadedEvents.size >= pageSize
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    // 404 = просто нет данных, это не ошибка
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

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentPage++
                val nextEvents = getEventsUseCase(size = pageSize, page = currentPage)
                if (nextEvents.isEmpty()) {
                    _hasMore.value = false
                } else {
                    _displayedEvents.value = _displayedEvents.value + nextEvents
                    _hasMore.value = nextEvents.size >= pageSize
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _hasMore.value = false
                } else {
                    _error.value = "Ошибка загрузки: ${e.code()}"
                }
                currentPage-- // откатываем страницу при ошибке
            } catch (e: Exception) {
                _error.value = "Нет соединения"
                currentPage--
            } finally {
                _isLoading.value = false
            }
        }
    }
}