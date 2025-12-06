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

    private val _hasMore = MutableStateFlow(true)
    val hasMore = _hasMore.asStateFlow()

    private var currentIndex = 0
    private val pageSize = 5

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val loadedEvents = getEventsUseCase()
            _allEvents.value = loadedEvents
            loadMore()  // Загружаем первую порцию
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val allEvents = _allEvents.value
            if (currentIndex >= allEvents.size) {
                _hasMore.value = false
                return@launch
            }

            val endIndex = min(currentIndex + pageSize, allEvents.size)
            val nextEvents = allEvents.subList(currentIndex, endIndex)
            _displayedEvents.value += nextEvents
            currentIndex = endIndex
            _hasMore.value = currentIndex < allEvents.size
        }
    }
}