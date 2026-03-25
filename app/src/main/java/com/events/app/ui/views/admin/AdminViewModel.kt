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

// UUID regex: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
private val UUID_REGEX = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

/**
 * Возвращает true если строка похожа на UUID (полный или частичный — от 8+ символов hex).
 * Частичный: начало UUID без дефисов тоже считаем поиском по ID.
 */
private fun String.looksLikeUuid(): Boolean {
    val clean = trim()
    return UUID_REGEX.matches(clean) || (clean.length >= 8 && clean.all { it.isLetterOrDigit() || it == '-' } && clean.contains('-'))
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    enum class AdminTab { EVENTS, USERS, LOCATIONS }

    private val _selectedTab = MutableStateFlow(AdminTab.EVENTS)
    val selectedTab = _selectedTab.asStateFlow()
    fun selectTab(tab: AdminTab) { _selectedTab.value = tab }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    fun clearMessages() { _error.value = null; _successMessage.value = null }

    // ── МЕРОПРИЯТИЯ ───────────────────────────────────────────────

    // Все загруженные с сервера события (без фильтра)
    private val _allEvents = MutableStateFlow<List<ShortEventDto>>(emptyList())

    // Отображаемые события (после фильтра по ID на клиенте или серверного поиска)
    private val _events = MutableStateFlow<List<ShortEventDto>>(emptyList())
    val events = _events.asStateFlow()

    private val _eventsLoading = MutableStateFlow(false)
    val eventsLoading = _eventsLoading.asStateFlow()

    private var eventsPage    = 1
    private var eventsHasMore = true

    /**
     * Загрузить/обновить список мероприятий.
     * [query] — строка поиска. Если похожа на UUID — фильтруем локально.
     * Иначе — передаём на сервер (полнотекстовый поиск по названию/анонсу/описанию).
     */
    fun loadEvents(query: String? = null, reset: Boolean = true) {
        val q = query?.trim()

        // ── Поиск по ID — локально ───────────────────────────────
        if (!q.isNullOrBlank() && q.looksLikeUuid()) {
            val filtered = _allEvents.value.filter { it.id.contains(q, ignoreCase = true) }
            _events.value = filtered
            return
        }

        // ── Обычный поиск — на сервер ────────────────────────────
        if (reset) { eventsPage = 1; eventsHasMore = true; _allEvents.value = emptyList(); _events.value = emptyList() }
        if (!eventsHasMore) return

        viewModelScope.launch {
            _eventsLoading.value = true
            try {
                val page = remoteDataSource.getEvents(
                    size = 30,
                    page = eventsPage,
                    text = q?.takeIf { it.length >= 2 }
                )
                val merged = if (reset) page else _allEvents.value + page
                _allEvents.value = merged
                _events.value    = merged
                if (page.size < 30) eventsHasMore = false else eventsPage++
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _allEvents.value = emptyList()
                    _events.value    = emptyList()
                } else {
                    _error.value = "Ошибка загрузки: HTTP ${e.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally {
                _eventsLoading.value = false
            }
        }
    }

    fun loadMoreEvents() {
        if (!_eventsLoading.value && eventsHasMore) loadEvents(reset = false)
    }

    fun deleteEvent(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                remoteDataSource.deleteEvent(id)
                _allEvents.value = _allEvents.value.filter { it.id != id }
                _events.value    = _events.value.filter { it.id != id }
                _successMessage.value = "Мероприятие удалено"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    // ── ПОЛЬЗОВАТЕЛИ ──────────────────────────────────────────────

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users = _users.asStateFlow()

    private val _usersLoading = MutableStateFlow(false)
    val usersLoading = _usersLoading.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _usersLoading.value = true
            try {
                _users.value = remoteDataSource.getUsers(size = 20, page = 1)
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 404) _error.value = "Ошибка загрузки пользователей: HTTP ${e.code()}"
                _users.value = emptyList()
            } catch (e: Exception) {
                _users.value = emptyList()
            } finally { _usersLoading.value = false }
        }
    }

    fun createUser(
        firstName: String, lastName: String, email: String,
        password: String, patronymic: String, onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                remoteDataSource.createUser(
                    firstName  = firstName.trim().toBody(),
                    lastName   = lastName.trim().toBody(),
                    email      = email.trim().toBody(),
                    password   = password.toBody(),
                    patronymic = patronymic.trim().takeIf { it.isNotEmpty() }?.toBody()
                )
                _successMessage.value = "Пользователь «${firstName.trim()} ${lastName.trim()}» создан"
                loadUsers()
                onDone()
            } catch (e: retrofit2.HttpException) {
                _error.value = when (e.code()) {
                    400  -> "Неверные данные. Проверь email и пароль."
                    409  -> "Пользователь с таким email уже существует."
                    else -> "Ошибка создания: HTTP ${e.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun deleteUser(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                remoteDataSource.deleteUser(id)
                _users.value = _users.value.filter { it.id != id }
                _successMessage.value = "Пользователь удалён"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    // ── ЛОКАЦИИ ───────────────────────────────────────────────────

    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _locationsLoading = MutableStateFlow(false)
    val locationsLoading = _locationsLoading.asStateFlow()

    fun loadLocations() {
        viewModelScope.launch {
            _locationsLoading.value = true
            try {
                _locations.value = remoteDataSource.getLocations()
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 404) _error.value = "Ошибка загрузки локаций"
                _locations.value = emptyList()
            } catch (e: Exception) {
                _locations.value = emptyList()
            } finally { _locationsLoading.value = false }
        }
    }

    fun createLocation(title: String, address: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val newId = remoteDataSource.createLocation(title.trim().toBody(), address.trim().toBody())
                _locations.value = _locations.value + LocationDto(id = newId, title = title.trim(), address = address.trim())
                _successMessage.value = "Локация «${title.trim()}» создана"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания локации: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun deleteLocation(id: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                remoteDataSource.deleteLocation(id)
                _locations.value = _locations.value.filter { it.id != id }
                _successMessage.value = "Локация удалена"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    init {
        loadEvents()
        loadUsers()
        loadLocations()
    }
}