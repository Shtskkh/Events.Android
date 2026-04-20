package com.events.app.ui.views.createevent

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.local.EventLocationCache
import com.events.app.data.local.EventRefreshBus
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.TagDto
import com.events.app.domain.repositories.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val authRepository: AuthRepository,
    private val locationCache: EventLocationCache,
    private val refreshBus: EventRefreshBus,
    application: Application
) : AndroidViewModel(application) {

    // ── Шаг 1 ────────────────────────────────────────────────────
    val title               = MutableStateFlow("")
    val announcement        = MutableStateFlow("")
    val description         = MutableStateFlow("")
    val selectedImageUri    = MutableStateFlow<Uri?>(null)
    val selectedPlaceholder = MutableStateFlow<String?>(null)

    // ── Шаг 2 ────────────────────────────────────────────────────
    val startDateTime      = MutableStateFlow("")
    val endDateTime        = MutableStateFlow("")
    val needsRegistration  = MutableStateFlow(false)
    val maxParticipants    = MutableStateFlow("")
    val selectedTypeId     = MutableStateFlow<Int?>(null)
    val selectedFormatId   = MutableStateFlow<Int?>(null)
    val selectedLocationId = MutableStateFlow<Int?>(null)
    val selectedPlaceId    = MutableStateFlow<Int?>(null)

    // ── Тэги ──────────────────────────────────────────────────────
    private val _availableTags = MutableStateFlow<List<TagDto>>(emptyList())
    val availableTags = _availableTags.asStateFlow()

    val selectedTagIds = MutableStateFlow<Set<Int>>(emptySet())

    private val _tagSearchQuery = MutableStateFlow("")
    val tagSearchQuery = _tagSearchQuery.asStateFlow()

    // ── Справочники ───────────────────────────────────────────────
    private val _placeholders = MutableStateFlow<List<String>>(emptyList())
    val placeholders = _placeholders.asStateFlow()

    private val _eventTypes = MutableStateFlow<List<EventTypeDto>>(emptyList())
    val eventTypes = _eventTypes.asStateFlow()

    private val _eventFormats = MutableStateFlow<List<EventFormatDto>>(emptyList())
    val eventFormats = _eventFormats.asStateFlow()

    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _places = MutableStateFlow<List<PlaceDto>>(emptyList())
    val places = _places.asStateFlow()

    // ── Оборудование выбранного помещения ─────────────────────────
    private val _placeEquipment = MutableStateFlow<List<EquipmentDto>>(emptyList())
    val placeEquipment = _placeEquipment.asStateFlow()

    private val _equipmentLoading = MutableStateFlow(false)
    val equipmentLoading = _equipmentLoading.asStateFlow()

    // ── UI состояние ──────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    private val _showCreateLocationDialog = MutableStateFlow(false)
    val showCreateLocationDialog = _showCreateLocationDialog.asStateFlow()

    private val _isCreatingLocation = MutableStateFlow(false)
    val isCreatingLocation = _isCreatingLocation.asStateFlow()

    private val _createLocationError = MutableStateFlow<String?>(null)
    val createLocationError = _createLocationError.asStateFlow()

    init {
        loadReferenceData()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            try { _placeholders.value   = remoteDataSource.getPlaceholders() } catch (_: Exception) {}
            try { _eventTypes.value     = remoteDataSource.getEventTypes()   } catch (_: Exception) {}
            try { _eventFormats.value   = remoteDataSource.getEventFormats() } catch (_: Exception) {}
            try { _locations.value      = remoteDataSource.getLocations()    } catch (_: Exception) {}
            try { _availableTags.value  = remoteDataSource.getTags(size = 100) } catch (_: Exception) {}
        }
    }

    fun searchTags(query: String) {
        _tagSearchQuery.value = query
        viewModelScope.launch {
            try {
                _availableTags.value = remoteDataSource.getTags(
                    titleLike = query.takeIf { it.isNotBlank() },
                    size = 100
                )
            } catch (_: Exception) {}
        }
    }

    fun toggleTag(tagId: Int) {
        val current = selectedTagIds.value
        selectedTagIds.value = if (tagId in current) {
            current - tagId
        } else if (current.size < 5) {
            current + tagId
        } else {
            current // лимит 5 тэгов
        }
    }

    fun loadPlaces(locationId: Int) {
        viewModelScope.launch {
            try {
                _places.value = remoteDataSource.getPlacesByLocation(locationId)
                selectedPlaceId.value = null
                _placeEquipment.value = emptyList()
            } catch (_: Exception) {
                _places.value = emptyList()
                _placeEquipment.value = emptyList()
            }
        }
    }

    fun loadEquipmentForPlace(placeId: Int) {
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

    fun clearPlaceEquipment() {
        _placeEquipment.value = emptyList()
    }

    fun openCreateLocationDialog() {
        _createLocationError.value = null
        _showCreateLocationDialog.value = true
    }

    fun closeCreateLocationDialog() {
        _showCreateLocationDialog.value = false
        _createLocationError.value = null
    }

    fun createLocation(title: String, address: String, onSuccess: (LocationDto) -> Unit) {
        viewModelScope.launch {
            _isCreatingLocation.value = true
            _createLocationError.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val newId = remoteDataSource.createLocation(title.toBody(), address.toBody())
                val newLocation = LocationDto(id = newId, title = title, address = address)
                _locations.value = _locations.value + newLocation
                selectedLocationId.value = newId
                _showCreateLocationDialog.value = false
                onSuccess(newLocation)
            } catch (e: Exception) {
                _createLocationError.value = "Не удалось создать локацию: ${e.message}"
            } finally {
                _isCreatingLocation.value = false
            }
        }
    }

    fun submitEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())

                val userId = authRepository.currentUser.value?.id
                    ?: throw Exception("Пользователь не авторизован")

                val previewPart: MultipartBody.Part? = selectedImageUri.value?.let { uri ->
                    val bytes = getApplication<Application>().contentResolver
                        .openInputStream(uri)?.readBytes() ?: return@let null
                    val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("Preview", "preview.jpg", body)
                }

                val placeholderBody = selectedPlaceholder.value
                    ?.takeIf { previewPart == null }?.toBody()

                val maxParticipantsBody = if (needsRegistration.value) {
                    val entered = maxParticipants.value.trim()
                    val value   = if (entered.isBlank()) "999999" else entered
                    value.toBody()
                } else null

                // Передаём locationId — новое поле в API
                val locationIdBody = selectedLocationId.value?.toString()?.toBody()
                val placeIdBody    = selectedPlaceId.value?.toString()?.toBody()

                val newEventId = remoteDataSource.createEvent(
                    userId            = userId.toBody(),
                    title             = title.value.toBody(),
                    announcement      = announcement.value.toBody(),
                    description       = description.value.toBody(),
                    startDateTime     = startDateTime.value.toBody(),
                    endDateTime       = endDateTime.value.toBody(),
                    eventTypeId       = selectedTypeId.value.toString().toBody(),
                    eventFormatId     = selectedFormatId.value.toString().toBody(),
                    needsRegistration = needsRegistration.value.toString().toBody(),
                    maxParticipants   = maxParticipantsBody,
                    locationId        = locationIdBody,
                    placeId           = placeIdBody,
                    placeholder       = placeholderBody,
                    preview           = previewPart,
                    tagIds            = selectedTagIds.value.toList()
                )

                // Кэшируем название локации
                val locId = selectedLocationId.value
                if (locId != null && newEventId.isNotBlank()) {
                    val locTitle = _locations.value.find { it.id == locId }?.title
                    if (!locTitle.isNullOrBlank()) {
                        locationCache.put(newEventId, locTitle)
                    }
                }

                refreshBus.notifyRefresh()
                _success.value = true

            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при создании мероприятия"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}