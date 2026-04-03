package com.events.app.ui.views.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.local.EventLocationCache
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.data.remote.dto.ParticipantDto
import com.events.app.data.remote.dto.toDomain
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

    // ── Мероприятие ───────────────────────────────────────────────

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // ── Удаление ──────────────────────────────────────────────────

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    // ── Роль пользователя ─────────────────────────────────────────

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    private val _canRegister = MutableStateFlow(false)
    val canRegister = _canRegister.asStateFlow()

    val currentUserId: String? get() = authRepository.currentUser.value?.id

    // ── Аналитика ─────────────────────────────────────────────────

    private val _analytics = MutableStateFlow<EventAnalyticDto?>(null)
    val analytics = _analytics.asStateFlow()

    private val _analyticsLoading = MutableStateFlow(false)
    val analyticsLoading = _analyticsLoading.asStateFlow()

    // ── Участники ─────────────────────────────────────────────────

    private val _participants = MutableStateFlow<List<ParticipantDto>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _participantsLoading = MutableStateFlow(false)
    val participantsLoading = _participantsLoading.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered = _isRegistered.asStateFlow()

    private val _registrationLoading = MutableStateFlow(false)
    val registrationLoading = _registrationLoading.asStateFlow()

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError = _registrationError.asStateFlow()

    fun clearRegistrationError() { _registrationError.value = null }

    // ── Оборудование помещения ────────────────────────────────────

    private val _placeEquipment = MutableStateFlow<List<EquipmentDto>>(emptyList())
    val placeEquipment = _placeEquipment.asStateFlow()

    private val _equipmentLoading = MutableStateFlow(false)
    val equipmentLoading = _equipmentLoading.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _isAdmin.value     = user?.role == UserRole.ADMIN
                _canRegister.value = user?.role == UserRole.ADMIN || user?.role == UserRole.USER
            }
        }
    }

    // ── Загрузка мероприятия с дозагрузкой локации и помещения ───

    fun loadEvent(eventId: String) {
        if (eventId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val accessToken = authRepository.currentUser.value?.accessToken

                // 1. Грузим базовые данные мероприятия
                val base = remoteDataSource.getEventById(eventId, accessToken).toDomain()

                // 2. Если у ивента есть placeId — ищем локацию и помещение
                val enriched = if (base.placeId != null) {
                    try {
                        val allLocations = remoteDataSource.getLocations()
                        var result = base

                        outer@ for (loc in allLocations) {
                            val places = try {
                                remoteDataSource.getPlacesByLocation(loc.id)
                            } catch (_: Exception) { emptyList() }

                            val matched = places.find { it.id == base.placeId }
                            if (matched != null) {
                                result = base.copy(
                                    location        = loc.title ?: base.location,
                                    locationAddress = loc.address,
                                    locationId      = loc.id,
                                    placeTitle      = matched.title,
                                    placeCapacity   = matched.capacity,
                                    placeNumber     = matched.number ?: base.placeNumber
                                )
                                break@outer
                            }
                        }
                        result
                    } catch (_: Exception) {
                        base
                    }
                } else {
                    if (base.location.isBlank()) {
                        base.copy(location = locationCache.get(eventId) ?: "")
                    } else base
                }

                _event.value = enriched

                // 3. Если есть placeId — параллельно загружаем оборудование
                if (enriched.placeId != null) {
                    loadEquipmentForPlace(enriched.placeId)
                }

            } catch (e: Exception) {
                _error.value = "Ошибка загрузки мероприятия"
            } finally {
                _isLoading.value = false
            }
        }

        // Параллельно грузим аналитику и участников
        loadAnalytics(eventId)
        loadParticipants(eventId)
    }

    // ── Оборудование ──────────────────────────────────────────────

    private fun loadEquipmentForPlace(placeId: Int) {
        viewModelScope.launch {
            _equipmentLoading.value = true
            try {
                _placeEquipment.value = remoteDataSource.getEquipment(placeId = placeId)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) _placeEquipment.value = emptyList()
            } catch (_: Exception) {
                _placeEquipment.value = emptyList()
            } finally {
                _equipmentLoading.value = false
            }
        }
    }

    // ── Аналитика ─────────────────────────────────────────────────

    private fun loadAnalytics(eventId: String) {
        viewModelScope.launch {
            _analyticsLoading.value = true
            try {
                val analytic = remoteDataSource.getEventAnalytics(eventId)
                _analytics.value = analytic

                // Ждём пока основной запрос загрузит event
                var attempts = 0
                while (_event.value == null && attempts < 10) {
                    kotlinx.coroutines.delay(100)
                    attempts++
                }

                val current = _event.value ?: return@launch
                _event.value = current.copy(
                    participantsCount = analytic.participantsCount,
                    viewsCount        = analytic.viewsCount,
                    maxParticipants   = current.maxParticipants ?: analytic.maxParticipantsCount
                )
            } catch (_: Exception) {
                _analytics.value = null
            } finally {
                _analyticsLoading.value = false
            }
        }
    }

    // ── Участники ─────────────────────────────────────────────────

    fun loadParticipants(eventId: String) {
        viewModelScope.launch {
            _participantsLoading.value = true
            try {
                val list = remoteDataSource.getParticipants(eventId)
                _participants.value = list
                val userId = authRepository.currentUser.value?.id
                _isRegistered.value = userId != null && list.any { it.id == userId }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _participants.value = emptyList()
                    _isRegistered.value = false
                }
            } catch (_: Exception) {
                _participants.value = emptyList()
            } finally {
                _participantsLoading.value = false
            }
        }
    }

    // ── Регистрация / отмена ──────────────────────────────────────

    fun toggleRegistration() {
        val eventId = _event.value?.id ?: return
        val userId  = authRepository.currentUser.value?.id ?: return

        viewModelScope.launch {
            _registrationLoading.value = true
            _registrationError.value   = null
            try {
                if (_isRegistered.value) {
                    remoteDataSource.leaveEvent(eventId, userId)
                    _isRegistered.value = false
                    _participants.value = _participants.value.filter { it.id != userId }
                } else {
                    remoteDataSource.registerForEvent(eventId, userId)
                    _isRegistered.value = true
                    loadParticipants(eventId)
                }
            } catch (e: retrofit2.HttpException) {
                _registrationError.value = when (e.code()) {
                    400  -> "Мероприятие уже заполнено"
                    404  -> "Мероприятие не найдено"
                    409  -> "Вы уже зарегистрированы"
                    else -> "Ошибка регистрации: ${e.code()}"
                }
            } catch (e: Exception) {
                _registrationError.value = "Нет соединения с сервером"
            } finally {
                _registrationLoading.value = false
            }
        }
    }

    // ── Удаление ──────────────────────────────────────────────────

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