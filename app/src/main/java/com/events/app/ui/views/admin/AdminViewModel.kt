package com.events.app.ui.views.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.ShortEventDto
import com.events.app.data.remote.dto.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    // ── Вкладки ───────────────────────────────────────────────────
    enum class AdminTab { EVENTS, USERS, LOCATIONS }

    private val _selectedTab = MutableStateFlow(AdminTab.EVENTS)
    val selectedTab = _selectedTab.asStateFlow()

    fun selectTab(tab: AdminTab) { _selectedTab.value = tab }

    // ── Состояние загрузки / ошибки / успеха ──────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

    // ─────────────────────────────────────────────────────────────
    // МЕРОПРИЯТИЯ
    // ─────────────────────────────────────────────────────────────

    private val _events = MutableStateFlow<List<ShortEventDto>>(emptyList())
    val events = _events.asStateFlow()

    private val _eventsLoading = MutableStateFlow(false)
    val eventsLoading = _eventsLoading.asStateFlow()

    fun loadEvents(query: String? = null) {
        viewModelScope.launch {
            _eventsLoading.value = true
            try {
                _events.value = remoteDataSource.getEvents(
                    size = 50,
                    page = 1,
                    text = query?.trim()?.takeIf { it.length >= 2 }
                )
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить мероприятия"
            } finally {
                _eventsLoading.value = false
            }
        }
    }

    fun deleteEvent(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                remoteDataSource.deleteEvent(id)
                _events.value = _events.value.filter { it.id != id }
                _successMessage.value = "Мероприятие удалено"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ПОЛЬЗОВАТЕЛИ
    // ─────────────────────────────────────────────────────────────

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users = _users.asStateFlow()

    private val _usersLoading = MutableStateFlow(false)
    val usersLoading = _usersLoading.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _usersLoading.value = true
            try {
                _users.value = remoteDataSource.getUsers()
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить пользователей"
            } finally {
                _usersLoading.value = false
            }
        }
    }

    fun createUser(
        email: String,
        password: String,
        name: String,
        role: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                remoteDataSource.createUser(
                    email = email.trim().toBody(),
                    password = password.toBody(),
                    name = name.trim().toBody(),
                    role = role.toBody()
                )
                _successMessage.value = "Пользователь «${name.trim()}» создан"
                loadUsers()
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания пользователя: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                remoteDataSource.deleteUser(id)
                _users.value = _users.value.filter { it.id != id }
                _successMessage.value = "Пользователь удалён"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ЛОКАЦИИ
    // ─────────────────────────────────────────────────────────────

    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _locationsLoading = MutableStateFlow(false)
    val locationsLoading = _locationsLoading.asStateFlow()

    fun loadLocations() {
        viewModelScope.launch {
            _locationsLoading.value = true
            try {
                _locations.value = remoteDataSource.getLocations()
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить локации"
            } finally {
                _locationsLoading.value = false
            }
        }
    }

    fun createLocation(title: String, address: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val newId = remoteDataSource.createLocation(title.trim().toBody(), address.trim().toBody())
                _locations.value = _locations.value + LocationDto(id = newId, title = title.trim(), address = address.trim())
                _successMessage.value = "Локация «${title.trim()}» создана"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания локации: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLocation(id: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                remoteDataSource.deleteLocation(id)
                _locations.value = _locations.value.filter { it.id != id }
                _successMessage.value = "Локация удалена"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Инициализация — грузим сразу при открытии
    init {
        loadEvents()
        loadUsers()
        loadLocations()
    }
}