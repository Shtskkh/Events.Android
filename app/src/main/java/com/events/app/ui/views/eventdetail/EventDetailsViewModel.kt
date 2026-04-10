package com.events.app.ui.views.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.local.EventLocationCache
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.data.remote.dto.ParticipantDto
import com.events.app.data.remote.dto.UserDetailDto
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

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _author = MutableStateFlow<UserDetailDto?>(null)
    val author = _author.asStateFlow()

    private val _authorLoading = MutableStateFlow(false)
    val authorLoading = _authorLoading.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    private val _canRegister = MutableStateFlow(false)
    val canRegister = _canRegister.asStateFlow()

    val currentUserId: String? get() = authRepository.currentUser.value?.id

    private val _analytics = MutableStateFlow<EventAnalyticDto?>(null)
    val analytics = _analytics.asStateFlow()

    private val _analyticsLoading = MutableStateFlow(false)
    val analyticsLoading = _analyticsLoading.asStateFlow()

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

    private val _placeEquipment = MutableStateFlow<List<EquipmentDto>>(emptyList())
    val placeEquipment = _placeEquipment.asStateFlow()

    private val _equipmentLoading = MutableStateFlow(false)
    val equipmentLoading = _equipmentLoading.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _isAdmin.value     = user?.role == UserRole.ADMIN
                _canRegister.value = user?.role == UserRole.ADMIN || user?.role == UserRole.USER
            }
        }
    }

    fun loadEvent(eventId: String) {
        if (eventId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val accessToken = authRepository.currentUser.value?.accessToken
                val dto = remoteDataSource.getEventById(eventId, accessToken)
                val base = dto.toDomain()

                // Определяем locationId: из DTO напрямую или ищем по placeId
                val knownLocationId = dto.locationId

                val enriched = when {
                    // Случай 1: есть placeId — ищем локацию по нему
                    base.placeId != null -> enrichByPlaceId(base, knownLocationId)

                    // Случай 2: есть только locationId — загружаем локацию напрямую
                    knownLocationId != null -> enrichByLocationId(base, knownLocationId)

                    // Случай 3: ничего нет — берём из кэша
                    else -> {
                        val cached = locationCache.get(eventId)
                        if (!cached.isNullOrBlank()) base.copy(location = cached) else base
                    }
                }

                _event.value = enriched

                if (enriched.placeId != null) loadEquipmentForPlace(enriched.placeId)
                if (!enriched.userId.isNullOrBlank()) loadAuthor(enriched.userId)

            } catch (e: Exception) {
                _error.value = "Ошибка загрузки мероприятия"
            } finally {
                _isLoading.value = false
            }
        }

        loadAnalytics(eventId)
        loadParticipants(eventId)
    }

    /** Случай когда есть placeId — ищем по локациям какая содержит это помещение */
    private suspend fun enrichByPlaceId(base: Event, knownLocationId: Int?): Event {
        return try {
            // Если locationId уже известен — сразу грузим только эту локацию
            if (knownLocationId != null) {
                val loc    = remoteDataSource.getLocationById(knownLocationId)
                val places = try { remoteDataSource.getPlacesByLocation(knownLocationId) } catch (_: Exception) { emptyList() }
                val matched = places.find { it.id == base.placeId }
                return base.copy(
                    location        = loc.title ?: base.location,
                    locationAddress = loc.address,
                    locationId      = loc.id,
                    placeTitle      = matched?.title,
                    placeCapacity   = matched?.capacity,
                    placeNumber     = matched?.number ?: base.placeNumber
                )
            }

            // Иначе перебираем все локации
            val allLocations = remoteDataSource.getLocations()
            var result = base
            outer@ for (loc in allLocations) {
                val places = try { remoteDataSource.getPlacesByLocation(loc.id) } catch (_: Exception) { emptyList() }
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
        } catch (_: Exception) { base }
    }

    /** Случай когда есть только locationId без placeId */
    private suspend fun enrichByLocationId(base: Event, locationId: Int): Event {
        return try {
            val loc = remoteDataSource.getLocationById(locationId)
            base.copy(
                location        = loc.title ?: base.location,
                locationAddress = loc.address,
                locationId      = loc.id
            )
        } catch (_: Exception) { base }
    }

    private fun loadAuthor(userId: String) {
        viewModelScope.launch {
            _authorLoading.value = true
            try {
                _author.value = remoteDataSource.getUserById(userId)
            } catch (_: Exception) {
                _author.value = null
            } finally {
                _authorLoading.value = false
            }
        }
    }

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

    private fun loadAnalytics(eventId: String) {
        viewModelScope.launch {
            _analyticsLoading.value = true
            try {
                val analytic = remoteDataSource.getEventAnalytics(eventId)
                _analytics.value = analytic
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
            } catch (_: Exception) {
                _registrationError.value = "Нет соединения с сервером"
            } finally {
                _registrationLoading.value = false
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
            } catch (_: Exception) {
                _error.value = "Не удалось удалить мероприятие"
            } finally {
                _isDeleting.value = false
            }
        }
    }
}