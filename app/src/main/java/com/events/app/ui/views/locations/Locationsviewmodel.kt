package com.events.app.ui.views.locations

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EquipmentTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.PlaceTypeDto
import com.events.app.domain.models.users.UserRole
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
class LocationsViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val authRepository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    // ── Роль ──────────────────────────────────────────────────────
    private val _isAdmin = MutableStateFlow(false)
    val isAdminFlow = _isAdmin.asStateFlow()
    val isAdmin: Boolean get() = _isAdmin.value

    // ── Локации ───────────────────────────────────────────────────
    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _locationsLoading = MutableStateFlow(false)
    val locationsLoading = _locationsLoading.asStateFlow()

    // ── Помещения ─────────────────────────────────────────────────
    private val _places = MutableStateFlow<List<PlaceDto>>(emptyList())
    val places = _places.asStateFlow()

    private val _placesLoading = MutableStateFlow(false)
    val placesLoading = _placesLoading.asStateFlow()

    private val _expandedLocationId = MutableStateFlow<Int?>(null)
    val expandedLocationId = _expandedLocationId.asStateFlow()

    // ── Помещения для фильтра-пикера (независимо от expandedLocationId) ──
    private val _filterPlaces = MutableStateFlow<List<PlaceDto>>(emptyList())
    val filterPlaces = _filterPlaces.asStateFlow()

    private val _filterPlacesLoading = MutableStateFlow(false)
    val filterPlacesLoading = _filterPlacesLoading.asStateFlow()

    fun loadFilterPlaces(locationId: Int) {
        viewModelScope.launch {
            _filterPlacesLoading.value = true
            _filterPlaces.value = emptyList()
            try {
                _filterPlaces.value = remoteDataSource.getPlacesByLocation(locationId)
            } catch (_: Exception) {
                _filterPlaces.value = emptyList()
            } finally {
                _filterPlacesLoading.value = false
            }
        }
    }

    fun clearFilterPlaces() {
        _filterPlaces.value = emptyList()
    }
    private val _selectedPlace = MutableStateFlow<PlaceDto?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _selectedLocationForPlace = MutableStateFlow<LocationDto?>(null)
    val selectedLocationForPlace = _selectedLocationForPlace.asStateFlow()

    // ── Типы помещений ────────────────────────────────────────────
    private val _placeTypes = MutableStateFlow<List<PlaceTypeDto>>(emptyList())
    val placeTypes = _placeTypes.asStateFlow()

    // ── Оборудование ──────────────────────────────────────────────
    private val _equipment = MutableStateFlow<List<EquipmentDto>>(emptyList())
    val equipment = _equipment.asStateFlow()

    private val _equipmentLoading = MutableStateFlow(false)
    val equipmentLoading = _equipmentLoading.asStateFlow()

    private val _equipmentTypes = MutableStateFlow<List<EquipmentTypeDto>>(emptyList())
    val equipmentTypes = _equipmentTypes.asStateFlow()

    // ── Состояния ─────────────────────────────────────────────────
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading = _isActionLoading.asStateFlow()

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _isAdmin.value = user?.role == UserRole.ADMIN
            }
        }
        loadLocations()
        loadPlaceTypes()
        loadEquipmentTypes()
    }

    // ── Загрузка ──────────────────────────────────────────────────

    fun loadLocations() {
        viewModelScope.launch {
            _locationsLoading.value = true
            try {
                _locations.value = remoteDataSource.getLocations()
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 404) _error.value = "Ошибка загрузки локаций"
                _locations.value = emptyList()
            } catch (_: Exception) {
                _locations.value = emptyList()
            } finally {
                _locationsLoading.value = false
            }
        }
    }

    private fun loadPlaceTypes() {
        viewModelScope.launch {
            try { _placeTypes.value = remoteDataSource.getPlaceTypes() } catch (_: Exception) {}
        }
    }

    private fun loadEquipmentTypes() {
        viewModelScope.launch {
            try { _equipmentTypes.value = remoteDataSource.getEquipmentTypes() } catch (_: Exception) {}
        }
    }

    fun toggleLocation(locationId: Int) {
        if (_expandedLocationId.value == locationId) {
            _expandedLocationId.value = null
            _places.value = emptyList()
        } else {
            _expandedLocationId.value = locationId
            loadPlaces(locationId)
        }
    }

    fun loadPlaces(locationId: Int) {
        viewModelScope.launch {
            _placesLoading.value = true
            _places.value = emptyList()
            try {
                _places.value = remoteDataSource.getPlacesByLocation(locationId)
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 404) _error.value = "Ошибка загрузки помещений"
                _places.value = emptyList()
            } catch (_: Exception) {
                _places.value = emptyList()
            } finally {
                _placesLoading.value = false
            }
        }
    }

    fun openPlace(locationId: Int, place: PlaceDto) {
        _selectedPlace.value = place
        _selectedLocationForPlace.value = _locations.value.find { it.id == locationId }
        _equipment.value = emptyList()
        loadEquipmentForPlace(place.id)
    }

    fun closePlace() {
        _selectedPlace.value = null
        _selectedLocationForPlace.value = null
        _equipment.value = emptyList()
    }

    fun loadEquipmentForPlace(placeId: Int) {
        viewModelScope.launch {
            _equipmentLoading.value = true
            try {
                _equipment.value = remoteDataSource.getEquipment(placeId = placeId)
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 404) _error.value = "Ошибка загрузки оборудования"
                _equipment.value = emptyList()
            } catch (_: Exception) {
                _equipment.value = emptyList()
            } finally {
                _equipmentLoading.value = false
            }
        }
    }

    // ── CRUD: Локации ─────────────────────────────────────────────

    /**
     * Создаёт локацию с опциональным фото.
     * API POST /locations принимает Photos[] — массив файлов.
     * Здесь передаём одно фото (сервер возвращает одно preview).
     */
    fun createLocation(
        title: String,
        address: String,
        photoUri: Uri? = null,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())

                val context: Context = getApplication()
                val photoParts: List<MultipartBody.Part> = photoUri?.let { uri ->
                    val bytes = try {
                        context.contentResolver.openInputStream(uri)?.readBytes()
                    } catch (_: Exception) { null }
                    if (bytes != null) {
                        val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        listOf(MultipartBody.Part.createFormData("Photos", "location_preview.jpg", body))
                    } else emptyList()
                } ?: emptyList()

                val newId = remoteDataSource.createLocation(
                    title   = title.trim().toBody(),
                    address = address.trim().toBody(),
                    photos  = photoParts
                )
                _locations.value = _locations.value + LocationDto(
                    id      = newId,
                    title   = title.trim(),
                    address = address.trim()
                )
                _successMessage.value = "Локация создана"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания: ${e.message}"
            } finally { _isActionLoading.value = false }
        }
    }

    fun editLocation(
        location: LocationDto,
        newTitle: String,
        newAddress: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val titleBody   = newTitle.trim().toBody()
                val addressBody = newAddress.trim().toBody()
                remoteDataSource.updateLocation(location.id, titleBody, addressBody)

                _locations.value = _locations.value.map { loc ->
                    if (loc.id == location.id)
                        loc.copy(title = newTitle.trim(), address = newAddress.trim())
                    else loc
                }
                if (_selectedLocationForPlace.value?.id == location.id) {
                    _selectedLocationForPlace.value = _selectedLocationForPlace.value?.copy(
                        title   = newTitle.trim(),
                        address = newAddress.trim()
                    )
                }
                _successMessage.value = "Локация обновлена"
                onDone()
            } catch (e: retrofit2.HttpException) {
                _error.value = when (e.code()) {
                    400  -> "Неверные данные"
                    404  -> "Локация не найдена"
                    else -> "Ошибка сервера: ${e.code()}"
                }
            } catch (_: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally { _isActionLoading.value = false }
        }
    }

    fun deleteLocation(id: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                remoteDataSource.deleteLocation(id)
                _locations.value = _locations.value.filter { it.id != id }
                if (_expandedLocationId.value == id) {
                    _expandedLocationId.value = null
                    _places.value = emptyList()
                }
                _successMessage.value = "Локация удалена"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            } finally { _isActionLoading.value = false }
        }
    }

    // ── CRUD: Помещения ───────────────────────────────────────────

    fun createPlace(
        locationId: Int,
        number: String,
        capacity: Int,
        typeId: Int,
        title: String?,
        photoUris: List<Uri> = emptyList(),
        onDone: () -> Unit = {}
    ) {
        if (_isActionLoading.value) return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val context: Context = getApplication()
                val photoBytes = photoUris.mapNotNull { uri ->
                    try { context.contentResolver.openInputStream(uri)?.readBytes() }
                    catch (_: Exception) { null }
                }
                remoteDataSource.createPlace(
                    locationId = locationId,
                    number     = number.trim().toBody(),
                    capacity   = capacity.toString().toBody(),
                    type       = typeId.toString().toBody(),
                    title      = title?.trim()?.takeIf { it.isNotBlank() }?.toBody(),
                    photos     = photoBytes
                )
                onDone()
                _expandedLocationId.value = locationId
                _successMessage.value = "Помещение создано"
                _placesLoading.value = true
                try {
                    _places.value = remoteDataSource.getPlacesByLocation(locationId)
                } catch (_: Exception) {
                } finally { _placesLoading.value = false }

            } catch (e: retrofit2.HttpException) {
                val serverMessage = try {
                    e.response()?.errorBody()?.string()?.let { parseServerError(it) }
                } catch (_: Exception) { null }
                _error.value = when {
                    serverMessage != null -> serverMessage
                    e.code() == 400      -> "Помещение с таким номером уже существует"
                    e.code() == 404      -> "Локация или тип помещения не найдены"
                    else                 -> "Ошибка сервера: ${e.code()}"
                }
            } catch (_: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally { _isActionLoading.value = false }
        }
    }

    fun editPlace(
        locationId: Int,
        place: PlaceDto,
        newTitle: String?,
        newCapacity: Int,
        newTypeId: Int,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val updatedPlace = remoteDataSource.updatePlace(
                    locationId = locationId,
                    placeId    = place.id,
                    title      = newTitle?.trim()?.takeIf { it.isNotBlank() }?.toBody(),
                    type       = newTypeId.toString().toBody(),
                    capacity   = newCapacity.toString().toBody()
                )
                _places.value = _places.value.map { p ->
                    if (p.id == place.id) updatedPlace else p
                }
                if (_selectedPlace.value?.id == place.id) {
                    _selectedPlace.value = updatedPlace
                }
                _successMessage.value = "Помещение обновлено"
                onDone()
            } catch (e: retrofit2.HttpException) {
                _error.value = when (e.code()) {
                    400  -> "Неверные данные"
                    404  -> "Помещение или тип не найдены"
                    else -> "Ошибка сервера: ${e.code()}"
                }
            } catch (_: Exception) {
                _error.value = "Нет соединения с сервером"
            } finally { _isActionLoading.value = false }
        }
    }

    fun deletePlace(locationId: Int, placeId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                remoteDataSource.deletePlace(locationId, placeId)
                _places.value = _places.value.filter { it.id != placeId }
                if (_selectedPlace.value?.id == placeId) closePlace()
                _successMessage.value = "Помещение удалено"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            } finally { _isActionLoading.value = false }
        }
    }

    // ── CRUD: Оборудование ────────────────────────────────────────

    fun createEquipment(
        title: String,
        inventoryNumber: String,
        equipmentTypeId: Int,
        placeId: Int,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                remoteDataSource.createEquipment(
                    title           = title.trim().toBody(),
                    inventoryNumber = inventoryNumber.trim().toBody(),
                    equipmentTypeId = equipmentTypeId.toString().toBody(),
                    placeId         = placeId.toString().toBody()
                )
                _equipment.value = remoteDataSource.getEquipment(placeId = placeId)
                _successMessage.value = "Оборудование «${title.trim()}» добавлено"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания: ${e.message}"
            } finally { _isActionLoading.value = false }
        }
    }

    fun deleteEquipment(equipmentId: Int, placeId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                remoteDataSource.deleteEquipment(equipmentId)
                _equipment.value = _equipment.value.filter { it.id != equipmentId }
                _successMessage.value = "Оборудование удалено"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            } finally { _isActionLoading.value = false }
        }
    }
}

private fun parseServerError(json: String): String? {
    return try {
        Regex(""""message"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
    } catch (_: Exception) { null }
}