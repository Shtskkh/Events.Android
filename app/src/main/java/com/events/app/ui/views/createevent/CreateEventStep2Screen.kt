package com.events.app.ui.views.createevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventStep2Screen(
    viewModel: CreateEventViewModel,
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    val eventTypes by viewModel.eventTypes.collectAsState()
    val eventFormats by viewModel.eventFormats.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val needsRegistration by viewModel.needsRegistration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val showCreateLocationDialog by viewModel.showCreateLocationDialog.collectAsState()
    val startDateTime by viewModel.startDateTime.collectAsState()
    val endDateTime by viewModel.endDateTime.collectAsState()

    var startDisplay by remember { mutableStateOf("") }
    var endDisplay by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val selectedType by viewModel.selectedTypeId.collectAsState()
    val selectedFormat by viewModel.selectedFormatId.collectAsState()
    val selectedLocation by viewModel.selectedLocationId.collectAsState()

    LaunchedEffect(success) {
        if (success) onSuccess()
    }

    error?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    fun pickDateTime(onResult: (String, String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            TimePickerDialog(context, { _, h, min ->
                val dt = LocalDateTime.of(y, m + 1, d, h, min)
                val iso = dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val display = "%02d.%02d.%d %02d:%02d".format(d, m + 1, y, h, min)
                onResult(iso, display)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
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

        // Дата начала
        OutlinedButton(
            onClick = {
                pickDateTime { iso, display ->
                    viewModel.startDateTime.value = iso
                    startDisplay = display
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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

        // Дата окончания
        OutlinedButton(
            onClick = {
                pickDateTime { iso, display ->
                    viewModel.endDateTime.value = iso
                    endDisplay = display
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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

        // Тип мероприятия
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                eventTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.title ?: "Тип ${type.id}") },
                        onClick = {
                            viewModel.selectedTypeId.value = type.id
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Формат мероприятия
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = formatExpanded,
                onDismissRequest = { formatExpanded = false }
            ) {
                eventFormats.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(format.title ?: "Формат ${format.id}") },
                        onClick = {
                            viewModel.selectedFormatId.value = format.id
                            formatExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Локация
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                if (selectedLocation != null) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Не указывать",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            viewModel.selectedLocationId.value = null
                            locationExpanded = false
                        }
                    )
                    HorizontalDivider()
                }
                if (locations.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Нет доступных локаций",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { locationExpanded = false }
                    )
                } else {
                    locations.forEach { loc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = loc.title ?: "Локация ${loc.id}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (!loc.address.isNullOrBlank()) {
                                        Text(
                                            text = loc.address,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.selectedLocationId.value = loc.id
                                locationExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка создания новой локации
        OutlinedButton(
            onClick = { viewModel.openCreateLocationDialog() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.AddLocation,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать новую локацию", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Нужна ли регистрация
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

        // Ошибка
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
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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

    // Диалог создания новой локации
    if (showCreateLocationDialog) {
        CreateLocationDialog(
            onDismiss = { viewModel.closeCreateLocationDialog() },
            onConfirm = { title, address ->
                viewModel.createLocation(title, address) { }
            },
            isLoading = viewModel.isCreatingLocation.collectAsState().value,
            error = viewModel.createLocationError.collectAsState().value
        )
    }
}

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AddLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Новая локация",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Заполните данные о месте проведения",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = locationTitle,
                    onValueChange = { locationTitle = it },
                    label = { Text("Название *") },
                    placeholder = { Text("Например: Главный кампус") },
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
                    placeholder = { Text("Например: ул. Пушкина, 10") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
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
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            if (isValid) onConfirm(
                                locationTitle.trim(),
                                locationAddress.trim()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Создать", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}