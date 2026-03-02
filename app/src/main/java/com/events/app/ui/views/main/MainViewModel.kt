package com.events.app.ui.views.main

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
class MainViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loadedEvents = getEventsUseCase(size = 20, page = 1)
                _events.value = loadedEvents
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    // Нет событий — просто показываем пустой список
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
}