package com.events.app.ui.views.createevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventStep2Screen(
    viewModel: CreateEventViewModel,
    onSuccess: () -> Unit = {}
) {
    val eventTypes by viewModel.eventTypes.collectAsState()
    val eventFormats by viewModel.eventFormats.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val places by viewModel.places.collectAsState()
    val needsRegistration by viewModel.needsRegistration.collectAsState()
    val maxParticipants by viewModel.maxParticipants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val showCreateLocationDialog by viewModel.showCreateLocationDialog.collectAsState()
    val startDateTime by viewModel.startDateTime.collectAsState()
    val endDateTime by viewModel.endDateTime.collectAsState()
    val selectedType by viewModel.selectedTypeId.collectAsState()
    val selectedFormat by viewModel.selectedFormatId.collectAsState()
    val selectedLocation by viewModel.selectedLocationId.collectAsState()
    val selectedPlace by viewModel.selectedPlaceId.collectAsState()

    var startDisplay by remember { mutableStateOf("") }
    var endDisplay by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }
    var placeExpanded by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var tempStartDateMillis by remember { mutableStateOf(0L) }
    var tempEndDateMillis by remember { mutableStateOf(0L) }

    val startDatePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(is24Hour = true)
    val endDatePickerState = rememberDatePickerState()
    val endTimePickerState = rememberTimePickerState(is24Hour = true)

    LaunchedEffect(success) {
        if (success) onSuccess()
    }

    error?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    val isFormValid = startDateTime.isNotBlank()
            && endDateTime.isNotBlank()
            && selectedType != null
            && selectedFormat != null

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Дата начала ───────────────────────────────────────
        OutlinedButton(
            onClick = { showStartDatePicker = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (startDisplay.isBlank()) "Дата и время начала *" else startDisplay,
                color = if (startDisplay.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Дата окончания ────────────────────────────────────
        OutlinedButton(
            onClick = { showEndDatePicker = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (endDisplay.isBlank()) "Дата и время окончания *" else endDisplay,
                color = if (endDisplay.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Тип мероприятия ───────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = eventTypes.find { it.id == selectedType }?.title ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип мероприятия *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                eventTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.title ?: "Тип ${type.id}") },
                        onClick = { viewModel.selectedTypeId.value = type.id; typeExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Формат мероприятия ────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = formatExpanded,
            onExpandedChange = { formatExpanded = it }
        ) {
            OutlinedTextField(
                value = eventFormats.find { it.id == selectedFormat }?.title ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Формат мероприятия *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                eventFormats.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(format.title ?: "Формат ${format.id}") },
                        onClick = { viewModel.selectedFormatId.value = format.id; formatExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Локация ───────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = locationExpanded,
            onExpandedChange = { locationExpanded = it }
        ) {
            OutlinedTextField(
                value = locations.find { it.id == selectedLocation }
                    ?.let { "${it.title ?: ""} — ${it.address ?: ""}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Локация (необязательно)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                if (selectedLocation != null) {
                    DropdownMenuItem(
                        text = { Text("Не указывать", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = {
                            viewModel.selectedLocationId.value = null
                            viewModel.selectedPlaceId.value = null
                            locationExpanded = false
                        }
                    )
                    HorizontalDivider()
                }
                if (locations.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Нет доступных локаций", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { locationExpanded = false }
                    )
                } else {
                    locations.forEach { loc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        loc.title ?: "Локация ${loc.id}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (!loc.address.isNullOrBlank()) {
                                        Text(
                                            loc.address,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.selectedLocationId.value = loc.id
                                viewModel.loadPlaces(loc.id) // загружаем помещения
                                locationExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // ── Помещение — появляется если выбрана локация и есть помещения ──
        if (selectedLocation != null && places.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = placeExpanded,
                onExpandedChange = { placeExpanded = it }
            ) {
                OutlinedTextField(
                    value = places.find { it.id == selectedPlace }
                        ?.let { "${it.title ?: "Помещение"} №${it.number ?: ""} (вместимость: ${it.capacity})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Помещение") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = placeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = placeExpanded,
                    onDismissRequest = { placeExpanded = false }
                ) {
                    if (selectedPlace != null) {
                        DropdownMenuItem(
                            text = { Text("Не указывать", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            onClick = { viewModel.selectedPlaceId.value = null; placeExpanded = false }
                        )
                        HorizontalDivider()
                    }
                    places.forEach { place ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        "${place.title ?: "Помещение"} №${place.number ?: ""}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "Вместимость: ${place.capacity}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { viewModel.selectedPlaceId.value = place.id; placeExpanded = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.openCreateLocationDialog() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Outlined.AddLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать новую локацию", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Регистрация ───────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Требуется регистрация",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = needsRegistration,
                    onCheckedChange = { viewModel.needsRegistration.value = it }
                )
            }
        }

        // ── Макс. участников — только если регистрация включена ──
        if (needsRegistration) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = maxParticipants,
                onValueChange = { viewModel.maxParticipants.value = it.filter { c -> c.isDigit() } },
                label = { Text("Макс. количество участников *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // ── Ошибка ────────────────────────────────────────────
        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.submitEvent() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Создать мероприятие", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── DatePicker начало ─────────────────────────────────────────
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempStartDateMillis = startDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showStartDatePicker = false
                    showStartTimePicker = true
                }) { Text("Далее") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Отмена") }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    // ── TimePicker начало ─────────────────────────────────────────
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val dt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(tempStartDateMillis), ZoneId.systemDefault()
                ).withHour(startTimePickerState.hour).withMinute(startTimePickerState.minute)
                viewModel.startDateTime.value = dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                startDisplay = "%02d.%02d.%d %02d:%02d".format(
                    dt.dayOfMonth, dt.monthValue, dt.year, dt.hour, dt.minute
                )
                showStartTimePicker = false
            },
            timePickerState = startTimePickerState
        )
    }

    // ── DatePicker конец ──────────────────────────────────────────
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempEndDateMillis = endDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showEndDatePicker = false
                    showEndTimePicker = true
                }) { Text("Далее") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Отмена") }
            }
        ) { DatePicker(state = endDatePickerState) }
    }

    // ── TimePicker конец ──────────────────────────────────────────
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                val dt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(tempEndDateMillis), ZoneId.systemDefault()
                ).withHour(endTimePickerState.hour).withMinute(endTimePickerState.minute)
                viewModel.endDateTime.value = dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                endDisplay = "%02d.%02d.%d %02d:%02d".format(
                    dt.dayOfMonth, dt.monthValue, dt.year, dt.hour, dt.minute
                )
                showEndTimePicker = false
            },
            timePickerState = endTimePickerState
        )
    }

    // ── Диалог создания локации ───────────────────────────────────
    if (showCreateLocationDialog) {
        CreateLocationDialog(
            onDismiss = { viewModel.closeCreateLocationDialog() },
            onConfirm = { title, address -> viewModel.createLocation(title, address) {} },
            isLoading = viewModel.isCreatingLocation.collectAsState().value,
            error = viewModel.createLocationError.collectAsState().value
        )
    }
}

// ── TimePicker диалог с ручным вводом ────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выберите время",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                TimeInput(state = timePickerState)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

// ── Диалог создания локации ───────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, address: String) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var locationTitle by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    val isValid = locationTitle.trim().isNotBlank() && locationAddress.trim().isNotBlank()

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Новая локация", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = locationTitle,
                    onValueChange = { locationTitle = it },
                    label = { Text("Название *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = locationAddress,
                    onValueChange = { locationAddress = it },
                    label = { Text("Адрес *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) { Text("Отмена") }
                    Button(
                        onClick = { if (isValid) onConfirm(locationTitle.trim(), locationAddress.trim()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isValid && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Создать", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}