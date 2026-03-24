package com.events.app.ui.views.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.local.EventLocationCache
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.domain.models.events.Event
import com.events.app.domain.models.users.UserRole
import com.events.app.domain.repositories.auth.AuthRepository
import com.events.app.domain.usecases.events.GetEventByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val locationCache: EventLocationCache,
    private val authRepository: AuthRepository,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    // Реактивный флаг admin — подписываемся на currentUser
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    // ── Аналитика ─────────────────────────────────────────────────

    private val _analytics = MutableStateFlow<EventAnalyticDto?>(null)
    val analytics = _analytics.asStateFlow()

    private val _analyticsLoading = MutableStateFlow(false)
    val analyticsLoading = _analyticsLoading.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _isAdmin.value = user?.role == UserRole.ADMIN
            }
        }
    }

    fun loadEvent(eventId: String) {
        if (eventId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Передаём accessToken → бэкенд записывает просмотр на пользователя.
                // Для неавторизованного пользователя accessToken = null (просто не записывается).
                val accessToken = authRepository.currentUser.value?.accessToken
                val loaded = getEventByIdUseCase(eventId, accessToken)

                // Обогащаем локацией из кэша если бэкенд не вернул
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

            // Загружаем аналитику параллельно — некритично, не влияет на отображение основного контента
            loadAnalytics(eventId)
        }
    }

    private fun loadAnalytics(eventId: String) {
        viewModelScope.launch {
            _analyticsLoading.value = true
            try {
                _analytics.value = remoteDataSource.getEventAnalytics(eventId)
            } catch (_: Exception) {
                // Аналитика — некритична, молча игнорируем
                _analytics.value = null
            } finally {
                _analyticsLoading.value = false
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            _isDeleting.value = true
            _error.value = null
            try {
                remoteDataSource.deleteEvent(id)
                _deleteSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Не удалось удалить мероприятие"
            } finally {
                _isDeleting.value = false
            }
        }
    }
}