package com.events.app.ui.views.editevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    // ── Поля формы (заполняются из текущего Event) ─────────────────

    val title        = MutableStateFlow("")
    val announcement = MutableStateFlow("")
    val description  = MutableStateFlow("")
    val startDateTime = MutableStateFlow("")   // ISO строка для API
    val endDateTime   = MutableStateFlow("")   // ISO строка для API

    // Отображаемые строки дат для UI
    val startDisplay = MutableStateFlow("")
    val endDisplay   = MutableStateFlow("")

    // ── UI состояние ───────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    /**
     * Заполнить форму из текущих данных мероприятия.
     * Вызывается при открытии экрана редактирования.
     * [startIso] и [endIso] — ISO строки из Event (уже в локальном времени).
     */
    fun prefill(
        title: String,
        announcement: String,
        description: String,
        startIso: String,
        endIso: String,
        startDisplayStr: String,
        endDisplayStr: String
    ) {
        this.title.value        = title
        this.announcement.value = announcement
        this.description.value  = description
        this.startDateTime.value = startIso
        this.endDateTime.value   = endIso
        this.startDisplay.value  = startDisplayStr
        this.endDisplay.value    = endDisplayStr
    }

    /**
     * Проверка валидности формы.
     */
    val isFormValid: Boolean
        get() = title.value.trim().length >= 2
                && announcement.value.trim().length >= 2
                && description.value.trim().length >= 2
                && startDateTime.value.isNotBlank()
                && endDateTime.value.isNotBlank()

    fun clearError() { _error.value = null }
    fun resetSuccess() { _success.value = false }

    /**
     * Отправить изменения на сервер.
     * Передаём только те поля, которые изменились (null = не трогать).
     * [eventId] — UUID мероприятия.
     * [originalTitle], [originalAnnouncement] и т.д. — оригинальные значения для сравнения.
     */
    fun submitUpdate(
        eventId: String,
        originalTitle: String,
        originalAnnouncement: String,
        originalDescription: String,
        originalStartDateTime: String,
        originalEndDateTime: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())

                // Передаём только изменённые поля
                val newTitle        = title.value.trim().takeIf { it != originalTitle }?.toBody()
                val newAnnouncement = announcement.value.trim().takeIf { it != originalAnnouncement }?.toBody()
                val newDescription  = description.value.trim().takeIf { it != originalDescription }?.toBody()
                val newStart        = startDateTime.value.takeIf { it != originalStartDateTime }?.toBody()
                val newEnd          = endDateTime.value.takeIf { it != originalEndDateTime }?.toBody()

                remoteDataSource.updateEvent(
                    id           = eventId,
                    title        = newTitle,
                    announcement = newAnnouncement,
                    description  = newDescription,
                    startDateTime = newStart,
                    endDateTime   = newEnd
                )
                _success.value = true
            } catch (e: retrofit2.HttpException) {
                _error.value = when (e.code()) {
                    400  -> "Неверные данные. Проверь заполненные поля."
                    404  -> "Мероприятие не найдено."
                    else -> "Ошибка сервера: ${e.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}