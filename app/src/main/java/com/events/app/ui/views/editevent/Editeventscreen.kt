package com.events.app.ui.views.editevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.domain.models.events.Event
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: Event,
    viewModel: EditEventViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {

    val isoFormatter     = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val displayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    LaunchedEffect(event.id) {
        viewModel.prefill(
            title            = event.title,
            announcement     = event.announcement,
            description      = event.description,
            startIso         = event.startDate.format(isoFormatter),
            endIso           = event.endDate.format(isoFormatter),
            startDisplayStr  = event.startDate.format(displayFormatter),
            endDisplayStr    = event.endDate.format(displayFormatter)
        )
    }

    val title        by viewModel.title.collectAsState()
    val announcement by viewModel.announcement.collectAsState()
    val description  by viewModel.description.collectAsState()
    val startDisplay by viewModel.startDisplay.collectAsState()
    val endDisplay   by viewModel.endDisplay.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val error        by viewModel.error.collectAsState()
    val success      by viewModel.success.collectAsState()

    LaunchedEffect(success) { if (success) { viewModel.resetSuccess(); onSuccess() } }

    // Состояние пикеров
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker   by remember { mutableStateOf(false) }
    var showEndTimePicker   by remember { mutableStateOf(false) }
    var tempStartMillis     by remember { mutableStateOf(0L) }
    var tempEndMillis       by remember { mutableStateOf(0L) }
    val startDatePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(is24Hour = true)
    val endDatePickerState   = rememberDatePickerState()
    val endTimePickerState   = rememberTimePickerState(is24Hour = true)

    val isFormValid = title.trim().length >= 2
            && announcement.trim().length >= 2
            && description.trim().length >= 2
            && viewModel.startDateTime.collectAsState().value.isNotBlank()
            && viewModel.endDateTime.collectAsState().value.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Название ──────────────────────────────────────────────
        OutlinedTextField(
            value         = title,
            onValueChange = { viewModel.title.value = it },
            label         = { Text("Название *") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            isError       = title.trim().length < 2 && title.isNotEmpty(),
            supportingText = {
                if (title.trim().length < 2 && title.isNotEmpty())
                    Text("Минимум 2 символа")
            }
        )

        Spacer(Modifier.height(12.dp))

        // ── Анонс ─────────────────────────────────────────────────
        OutlinedTextField(
            value         = announcement,
            onValueChange = { viewModel.announcement.value = it },
            label         = { Text("Анонс *") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            isError       = announcement.trim().length < 2 && announcement.isNotEmpty(),
            supportingText = {
                if (announcement.trim().length < 2 && announcement.isNotEmpty())
                    Text("Минимум 2 символа")
                else
                    Text("${announcement.length}/64")
            }
        )

        Spacer(Modifier.height(12.dp))

        // ── Описание ──────────────────────────────────────────────
        OutlinedTextField(
            value         = description,
            onValueChange = { viewModel.description.value = it },
            label         = { Text("Описание *") },
            minLines      = 3,
            maxLines      = 6,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            isError       = description.trim().length < 2 && description.isNotEmpty(),
            supportingText = {
                if (description.trim().length < 2 && description.isNotEmpty())
                    Text("Минимум 2 символа")
                else
                    Text("${description.length}/512")
            }
        )

        Spacer(Modifier.height(12.dp))

        // ── Дата начала ───────────────────────────────────────────
        OutlinedButton(
            onClick  = { showStartDatePicker = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text  = if (startDisplay.isBlank()) "Дата и время начала *" else startDisplay,
                color = if (startDisplay.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Дата окончания ────────────────────────────────────────
        OutlinedButton(
            onClick  = { showEndDatePicker = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text  = if (endDisplay.isBlank()) "Дата и время окончания *" else endDisplay,
                color = if (endDisplay.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Ошибка ────────────────────────────────────────────────
        error?.let {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape  = RoundedCornerShape(12.dp),
                color  = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Кнопки ────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick  = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !isLoading
            ) { Text("Отмена") }

            Button(
                onClick  = {
                    viewModel.submitUpdate(
                        eventId              = event.id,
                        originalTitle        = event.title,
                        originalAnnouncement = event.announcement,
                        originalDescription  = event.description,
                        originalStartDateTime = event.startDate.format(isoFormatter),
                        originalEndDateTime   = event.endDate.format(isoFormatter)
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = isFormValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // ── DatePicker начало ──────────────────────────────────────────
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempStartMillis = startDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showStartDatePicker = false; showStartTimePicker = true
                }) { Text("Далее") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val dt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(tempStartMillis), ZoneId.systemDefault()
                ).withHour(startTimePickerState.hour).withMinute(startTimePickerState.minute)
                viewModel.startDateTime.value = dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                viewModel.startDisplay.value  = dt.format(displayFormatter)
                showStartTimePicker = false
            },
            timePickerState = startTimePickerState
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempEndMillis = endDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showEndDatePicker = false; showEndTimePicker = true
                }) { Text("Далее") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = endDatePickerState) }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                val dt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(tempEndMillis), ZoneId.systemDefault()
                ).withHour(endTimePickerState.hour).withMinute(endTimePickerState.minute)
                viewModel.endDateTime.value = dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                viewModel.endDisplay.value  = dt.format(displayFormatter)
                showEndTimePicker = false
            },
            timePickerState = endTimePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Выберите время", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
                TimeInput(state = timePickerState)
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}