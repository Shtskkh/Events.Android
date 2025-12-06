package com.events.app.ui.views.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    init {
        val eventId = savedStateHandle.get<Int>("id") ?: 0  // Изменено на "id" — совпадает с вашим NavigationRoute
        loadEvent(eventId)
    }

    private fun loadEvent(eventId: Int) {
        viewModelScope.launch {
            val allEvents = getEventsUseCase()
            _event.value = allEvents.find { it.id == eventId }
        }
    }
}