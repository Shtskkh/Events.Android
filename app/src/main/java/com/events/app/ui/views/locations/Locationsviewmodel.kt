package com.events.app.ui.views.locations

import androidx.lifecycle.ViewModel
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
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Роль ─────────────────────────────────────────────────────
    val isAdmin get() = authRepository.currentUser.value?.role == UserRole.ADMIN

    // ── Локации ───────────────────────────────────────────────────
    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _locationsLoading = MutableStateFlow(false)
    val locationsLoading = _locationsLoading.asStateFlow()

    // ── Помещения для открытой локации ────────────────────────────
    private val _places = MutableStateFlow<List<PlaceDto>>(emptyList())
    val places = _places.asStateFlow()

    private val _placesLoading = MutableStateFlow(false)
    val placesLoading = _placesLoading.asStateFlow()

    private val _expandedLocationId = MutableStateFlow<Int?>(null)
    val expandedLocationId = _expandedLocationId.asStateFlow()

    // ── Детали выбранного помещения ───────────────────────────────
    private val _selectedPlace = MutableStateFlow<PlaceDto?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _selectedLocationForPlace = MutableStateFlow<LocationDto?>(null)
    val selectedLocationForPlace = _selectedLocationForPlace.asStateFlow()

    // ── Типы помещений ────────────────────────────────────────────
    private val _placeTypes = MutableStateFlow<List<PlaceTypeDto>>(emptyList())
    val placeTypes = _placeTypes.asStateFlow()

    // ── Оборудование выбранного помещения ─────────────────────────
    private val _equipment = MutableStateFlow<List<EquipmentDto>>(emptyList())
    val equipment = _equipment.asStateFlow()

    private val _equipmentLoading = MutableStateFlow(false)
    val equipmentLoading = _equipmentLoading.asStateFlow()

    // ── Типы оборудования (для создания) ─────────────────────────
    private val _equipmentTypes = MutableStateFlow<List<EquipmentTypeDto>>(emptyList())
    val equipmentTypes = _equipmentTypes.asStateFlow()

    // ── Ошибки и успех ────────────────────────────────────────────
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading = _isActionLoading.asStateFlow()

    fun clearMessages() { _error.value = null; _successMessage.value = null }

    init {
        loadLocations()
        loadPlaceTypes()
        loadEquipmentTypes()
    }

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

    // ── Раскрыть / свернуть локацию ──────────────────────────────

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

    // ── Открыть карточку помещения + загрузить оборудование ───────

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

    // ── CRUD Admin: Оборудование ──────────────────────────────────

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
                // Перезагружаем оборудование помещения
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

    // ── CRUD Admin: Локации ───────────────────────────────────────

    fun createLocation(title: String, address: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val newId = remoteDataSource.createLocation(title.trim().toBody(), address.trim().toBody())
                _locations.value = _locations.value + LocationDto(id = newId, title = title.trim(), address = address.trim())
                _successMessage.value = "Локация «${title.trim()}» создана"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания: ${e.message}"
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

    // ── CRUD Admin: Помещения ─────────────────────────────────────

    fun createPlace(
        locationId: Int, number: String, capacity: Int,
        typeId: Int, title: String?, onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                val titleBody = title?.trim()?.takeIf { it.isNotBlank() }?.toBody()
                remoteDataSource.createPlace(locationId, number.trim().toBody(),
                    capacity.toString().toBody(), typeId.toString().toBody(), titleBody)
                _places.value = remoteDataSource.getPlacesByLocation(locationId)
                _successMessage.value = "Помещение создано"
                onDone()
            } catch (e: Exception) {
                _error.value = "Ошибка создания: ${e.message}"
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
}