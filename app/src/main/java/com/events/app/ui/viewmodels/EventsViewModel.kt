package com.events.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.domain.model.Event
import com.events.app.domain.usecase.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _events.value = getEventsUseCase()
        }
    }
}
//Управляет состоянием экрана
//В init загружает события через GetEventsUseCase, хранит в MutableStateFlow.