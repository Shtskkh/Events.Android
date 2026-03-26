package com.events.app.ui.views.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.ShortEventDto
import com.events.app.data.remote.dto.UserDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val selectedTab    by viewModel.selectedTab.collectAsState()
    val error          by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(error, successMessage) {
        if (error != null || successMessage != null) {
            delay(4000); viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = MaterialTheme.colorScheme.primary) {
            AdminViewModel.AdminTab.entries.forEach { tab ->
                Tab(selected = selectedTab == tab, onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.label, fontSize = 12.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(tab.icon, null, modifier = Modifier.size(18.dp)) })
            }
        }

        AnimatedVisibility(visible = error != null || successMessage != null,
            enter = fadeIn(), exit = fadeOut()) {
            val isError = error != null
            Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isError) MaterialTheme.colorScheme.errorContainer
                else Color(0xFF22C55E).copy(alpha = 0.15f)) {
                Row(modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                        null,
                        tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp))
                    Text(error ?: successMessage ?: "",
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF166534),
                        fontSize = 13.sp)
                }
            }
        }

        when (selectedTab) {
            AdminViewModel.AdminTab.EVENTS    -> EventsTab(viewModel, onEventClick)
            AdminViewModel.AdminTab.USERS     -> UsersTab(viewModel)
            AdminViewModel.AdminTab.LOCATIONS -> LocationsTab(viewModel)
        }
    }
}

val AdminViewModel.AdminTab.label get() = when (this) {
    AdminViewModel.AdminTab.EVENTS    -> "Мероприятия"
    AdminViewModel.AdminTab.USERS     -> "Пользователи"
    AdminViewModel.AdminTab.LOCATIONS -> "Локации"
}
val AdminViewModel.AdminTab.icon: ImageVector get() = when (this) {
    AdminViewModel.AdminTab.EVENTS    -> Icons.Outlined.Event
    AdminViewModel.AdminTab.USERS     -> Icons.Outlined.People
    AdminViewModel.AdminTab.LOCATIONS -> Icons.Outlined.LocationOn
}

// ═════════════════════════════════════════════════════════════════
// МЕРОПРИЯТИЯ
// ═════════════════════════════════════════════════════════════════

@Composable
private fun EventsTab(
    viewModel: AdminViewModel,
    onEventClick: (String) -> Unit
) {
    val events    by viewModel.events.collectAsState()
    val isLoading by viewModel.eventsLoading.collectAsState()
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    var searchQuery   by remember { mutableStateOf("") }
    var searchJob     by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var eventToDelete by remember { mutableStateOf<ShortEventDto?>(null) }

    val isCloseToEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            last != null && last.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(isCloseToEnd) { if (isCloseToEnd) viewModel.loadMoreEvents() }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { q ->
                searchQuery = q
                searchJob?.cancel()
                searchJob = scope.launch { delay(500); viewModel.loadEvents(q.ifBlank { null }) }
            },
            placeholder = { Text("Поиск по названию или ID...") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = ""; viewModel.loadEvents(null) }) {
                        Icon(Icons.Outlined.Close, null)
                    }
                }
            },
            singleLine = true,
            shape    = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading && events.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                events.isEmpty() ->
                    EmptyState(Icons.Outlined.EventBusy, "Мероприятия не найдены")
                else -> LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        AdminEventCard(
                            event    = event,
                            onClick  = { onEventClick(event.id) },
                            onDelete = { eventToDelete = event }
                        )
                    }
                    if (isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    eventToDelete?.let { event ->
        ConfirmDeleteDialog(
            title = "Удалить мероприятие?",
            description = "«${event.title ?: event.id}» будет удалено безвозвратно.",
            onConfirm = { viewModel.deleteEvent(event.id); eventToDelete = null },
            onDismiss = { eventToDelete = null }
        )
    }
}

@Composable
private fun AdminEventCard(
    event: ShortEventDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Event, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title ?: "Без названия",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${event.id}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!event.type.isNullOrBlank()) {
                    Text(
                        text = event.type,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.DeleteOutline, "Удалить",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// ПОЛЬЗОВАТЕЛИ
// ═════════════════════════════════════════════════════════════════

@Composable
private fun UsersTab(viewModel: AdminViewModel) {
    val users     by viewModel.users.collectAsState()
    val isLoading by viewModel.usersLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var userToDelete     by remember { mutableStateOf<UserDto?>(null) }
    var searchQuery      by remember { mutableStateOf("") }

    // Локальная фильтрация по имени, фамилии или UUID пользователя
    val filteredUsers = remember(users, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) users
        else users.filter { user ->
            user.id.contains(q, ignoreCase = true) ||
                    user.displayName.contains(q, ignoreCase = true) ||
                    user.lastName?.contains(q, ignoreCase = true) == true ||
                    user.firstName?.contains(q, ignoreCase = true) == true ||
                    user.patronymic?.contains(q, ignoreCase = true) == true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Строка поиска ─────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск по имени или ID пользователя...") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Outlined.Close, null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (searchQuery.isBlank()) "Всего: ${users.size}"
                else "Найдено: ${filteredUsers.size} из ${users.size}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Outlined.PersonAdd, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать", fontSize = 14.sp)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                filteredUsers.isEmpty() -> EmptyState(
                    icon = if (searchQuery.isBlank()) Icons.Outlined.PeopleOutline
                    else Icons.Outlined.SearchOff,
                    text = if (searchQuery.isBlank()) "Пользователи не найдены"
                    else "Ничего не найдено по запросу «$searchQuery»"
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { user ->
                        AdminUserCard(user, onDelete = { userToDelete = user })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            isLoading = viewModel.isLoading.collectAsState().value,
            onDismiss = { showCreateDialog = false },
            onConfirm = { firstName, lastName, patronymic, email, password ->
                viewModel.createUser(firstName, lastName, email, password, patronymic) {
                    showCreateDialog = false
                }
            }
        )
    }
    userToDelete?.let { user ->
        ConfirmDeleteDialog(
            title = "Удалить пользователя?",
            description = "«${user.displayName}» будет удалён безвозвратно.",
            onConfirm = { viewModel.deleteUser(user.id); userToDelete = null },
            onDismiss = { userToDelete = null }
        )
    }
}

@Composable
private fun AdminUserCard(user: UserDto, onDelete: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("ID: ${user.id}", fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.DeleteOutline, "Удалить",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// ЛОКАЦИИ
// ═════════════════════════════════════════════════════════════════

@Composable
private fun LocationsTab(viewModel: AdminViewModel) {
    val locations by viewModel.locations.collectAsState()
    val isLoading by viewModel.locationsLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var locationToDelete by remember { mutableStateOf<LocationDto?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Всего: ${locations.size}", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { showCreateDialog = true }, shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Icon(Icons.Outlined.AddLocation, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать", fontSize = 14.sp)
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                locations.isEmpty() -> EmptyState(Icons.Outlined.LocationOff, "Локации не найдены")
                else -> LazyColumn(modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(locations, key = { it.id }) { location ->
                        AdminLocationCard(location, onDelete = { locationToDelete = location })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateLocationDialog(
            isLoading = viewModel.isLoading.collectAsState().value,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, address ->
                viewModel.createLocation(title, address) { showCreateDialog = false }
            }
        )
    }
    locationToDelete?.let { loc ->
        ConfirmDeleteDialog(
            title = "Удалить локацию?",
            description = "«${loc.title ?: "ID ${loc.id}"}» будет удалена безвозвратно.",
            onConfirm = { viewModel.deleteLocation(loc.id); locationToDelete = null },
            onDismiss = { locationToDelete = null }
        )
    }
}

@Composable
private fun AdminLocationCard(location: LocationDto, onDelete: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(location.title ?: "Локация ${location.id}", fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!location.address.isNullOrBlank()) {
                    Text(location.address, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.DeleteOutline, "Удалить",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// ДИАЛОГИ
// ═════════════════════════════════════════════════════════════════

@Composable
private fun ConfirmDeleteDialog(title: String, description: String,
                                onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text  = { Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            Button(onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape  = RoundedCornerShape(10.dp)) {
                Text("Удалить", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Отмена") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUserDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (firstName: String, lastName: String, patronymic: String,
                email: String, password: String) -> Unit
) {
    var firstName  by remember { mutableStateOf("") }
    var lastName   by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
    var email      by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isValid = firstName.trim().isNotBlank()
            && lastName.trim().isNotBlank()
            && email.trim().isNotBlank()
            && password.isNotBlank()

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.PersonAdd, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Text("Новый пользователь", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(20.dp))
                AdminTextField(lastName,   { lastName = it },   "Фамилия *",  Icons.Outlined.Person, !isLoading)
                Spacer(Modifier.height(10.dp))
                AdminTextField(firstName,  { firstName = it },  "Имя *",      Icons.Outlined.Person, !isLoading)
                Spacer(Modifier.height(10.dp))
                AdminTextField(patronymic, { patronymic = it }, "Отчество",   Icons.Outlined.Person, !isLoading)
                Spacer(Modifier.height(10.dp))
                AdminTextField(email,      { email = it },      "Email *",    Icons.Outlined.Email,  !isLoading)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Пароль *") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility, null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), enabled = !isLoading
                )
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading) { Text("Отмена") }
                    Button(
                        onClick = {
                            if (isValid) onConfirm(firstName.trim(), lastName.trim(),
                                patronymic.trim(), email.trim(), password)
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
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

@Composable
private fun CreateLocationDialog(isLoading: Boolean, onDismiss: () -> Unit,
                                 onConfirm: (title: String, address: String) -> Unit) {
    var locationTitle   by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    val isValid = locationTitle.trim().isNotBlank() && locationAddress.trim().isNotBlank()

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.AddLocation, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Text("Новая локация", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(20.dp))
                AdminTextField(locationTitle,   { locationTitle = it },   "Название *",
                    Icons.Outlined.LocationOn, !isLoading)
                Spacer(Modifier.height(12.dp))
                AdminTextField(locationAddress, { locationAddress = it }, "Адрес *",
                    Icons.Outlined.Map, !isLoading)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading) { Text("Отмена") }
                    Button(
                        onClick = { if (isValid) onConfirm(locationTitle.trim(), locationAddress.trim()) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
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

@Composable
private fun AdminTextField(value: String, onValueChange: (String) -> Unit,
                           label: String, icon: ImageVector, enabled: Boolean = true) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label) }, leadingIcon = { Icon(icon, null) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), enabled = enabled)
}

@Composable
private fun EmptyState(icon: ImageVector, text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 14.sp)
        }
    }
}