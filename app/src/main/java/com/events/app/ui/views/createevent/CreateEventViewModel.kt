package com.events.app.ui.views.createevent

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
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
    application: Application
) : AndroidViewModel(application) {

    // --- Шаг 1: основные данные ---
    val title = MutableStateFlow("")
    val announcement = MutableStateFlow("")
    val description = MutableStateFlow("")
    val selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedPlaceholder = MutableStateFlow<String?>(null)

    // --- Шаг 2: параметры ---
    val startDateTime = MutableStateFlow("")
    val endDateTime = MutableStateFlow("")
    val needsRegistration = MutableStateFlow(false)
    val selectedTypeId = MutableStateFlow<Int?>(null)
    val selectedFormatId = MutableStateFlow<Int?>(null)
    val selectedLocationId = MutableStateFlow<Int?>(null)

    // --- Справочники ---
    private val _placeholders = MutableStateFlow<List<String>>(emptyList())
    val placeholders = _placeholders.asStateFlow()

    private val _eventTypes = MutableStateFlow<List<EventTypeDto>>(emptyList())
    val eventTypes = _eventTypes.asStateFlow()

    private val _eventFormats = MutableStateFlow<List<EventFormatDto>>(emptyList())
    val eventFormats = _eventFormats.asStateFlow()

    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()

    // --- UI состояние ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    // --- Состояние диалога создания локации ---
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
            try { _placeholders.value = remoteDataSource.getPlaceholders() } catch (_: Exception) {}
            try { _eventTypes.value = remoteDataSource.getEventTypes() } catch (_: Exception) {}
            try { _eventFormats.value = remoteDataSource.getEventFormats() } catch (_: Exception) {}
            try { _locations.value = remoteDataSource.getLocations() } catch (_: Exception) {}
        }
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
                val newId = remoteDataSource.createLocation(
                    title = title.toBody(),
                    address = address.toBody()
                )
                // Добавляем новую локацию в список и выбираем её
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

                val previewPart: MultipartBody.Part? = selectedImageUri.value?.let { uri ->
                    val bytes = getApplication<Application>().contentResolver
                        .openInputStream(uri)?.readBytes() ?: return@let null
                    val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("Preview", "preview.jpg", body)
                }

                val placeholderBody = selectedPlaceholder.value
                    ?.takeIf { previewPart == null }
                    ?.toBody()

                remoteDataSource.createEvent(
                    title = title.value.toBody(),
                    announcement = announcement.value.toBody(),
                    description = description.value.toBody(),
                    startDateTime = startDateTime.value.toBody(),
                    endDateTime = endDateTime.value.toBody(),
                    eventTypeId = selectedTypeId.value.toString().toBody(),
                    eventFormatId = selectedFormatId.value.toString().toBody(),
                    needsRegistration = needsRegistration.value.toString().toBody(),
                    placeholder = placeholderBody,
                    preview = previewPart
                )
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