package com.events.app.ui.views.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.local.EventLocationCache
import com.events.app.domain.models.events.Event
import com.events.app.domain.usecases.events.GetEventByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val locationCache: EventLocationCache
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadEvent(eventId: String) {
        if (eventId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loaded = getEventByIdUseCase(eventId)
                val locationTitle = if (loaded.location.isBlank()) {
                    locationCache.get(eventId) ?: ""
                } else {
                    loaded.location
                }
                _event.value = loaded.copy(location = locationTitle)
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки мероприятия"
            } finally {
                _isLoading.value = false
            }
        }
    }
}