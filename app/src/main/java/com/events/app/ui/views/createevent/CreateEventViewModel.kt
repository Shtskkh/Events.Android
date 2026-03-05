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
    val startDateTime = MutableStateFlow("")   // ISO: "2026-04-10T10:00:00"
    val endDateTime = MutableStateFlow("")
    val needsRegistration = MutableStateFlow(false)
    val selectedTypeId = MutableStateFlow<Int?>(null)
    val selectedFormatId = MutableStateFlow<Int?>(null)
    val selectedLocationId = MutableStateFlow<Int?>(null)  // необязательно

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

    fun submitEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())

                // Собираем превью с устройства если есть
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