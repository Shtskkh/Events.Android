package com.events.app.ui.views.user

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.events.app.domain.models.users.UserRole
import com.events.app.ui.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountScreen() {
    val viewModel: AppViewModel = hiltViewModel()
    val user             by viewModel.authRepository.currentUser.collectAsState()
    val userDetail       by viewModel.userDetail.collectAsState()
    val avatarUri        by viewModel.avatarUri.collectAsState()
    val scope            = rememberCoroutineScope()
    val scrollState      = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context          = LocalContext.current

    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? -> if (uri != null) viewModel.saveAvatar(uri)
    }

    // Наблюдаем за результатом смены пароля
    val passwordSuccess by viewModel.passwordChangeSuccess.collectAsState()
    LaunchedEffect(passwordSuccess) {
        if (passwordSuccess) {
            Toast.makeText(context, "Пароль успешно изменён", Toast.LENGTH_SHORT).show()
            viewModel.clearPasswordChangeState()
            showChangePasswordDialog = false
        }
    }

    val (roleLabel, roleColor) = when (user?.role) {
        UserRole.ADMIN -> "Администратор" to Color(0xFF7C3AED)
        UserRole.USER  -> "Пользователь"  to Color(0xFF0284C7)
        else           -> "Гость"          to Color(0xFF6B7280)
    }

    fun copyToClipboard(label: String, value: String) {
        clipboardManager.setText(AnnotatedString(value))
        Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // ── Аватар и имя ──────────────────────────────────────────
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(140.dp).clip(CircleShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model              = avatarUri,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle, null,
                            modifier = Modifier.fillMaxSize(),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    Modifier.size(30.dp).align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CameraAlt, null,
                                tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Информация о пользователе ─────────────────────────────
        Surface(
            Modifier.fillMaxWidth(),
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(Modifier.padding(4.dp)) {
                val fullName = userDetail?.let { u ->
                    listOfNotNull(u.lastName?.trim(), u.firstName?.trim(), u.patronymic?.trim())
                        .filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { null }
                } ?: "—"
                CopyableInfoRow(Icons.Outlined.Person, "ФИО", fullName,
                    onCopy = { copyToClipboard("ФИО", fullName) })
                RowDivider()
                CopyableInfoRow(Icons.Outlined.Email, "Email", user?.email ?: "—",
                    onCopy = { copyToClipboard("Email", user?.email ?: "") })
                RowDivider()
                CopyableInfoRow(Icons.Outlined.Shield, "Роль", roleLabel,
                    valueColor = roleColor,
                    onCopy = { copyToClipboard("Роль", roleLabel) })
                RowDivider()
                CopyableInfoRow(Icons.Outlined.Key, "ID пользователя", user?.id ?: "—",
                    valueFontSize = 12,
                    onCopy = { copyToClipboard("ID", user?.id ?: "") })
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Действия ──────────────────────────────────────────────
        Surface(
            Modifier.fillMaxWidth(),
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(Modifier.padding(4.dp)) {
                ActionRow(Icons.Outlined.PhotoCamera, "Загрузить фото профиля") {
                    photoPickerLauncher.launch("image/*")
                }
                RowDivider()
                ActionRow(Icons.Outlined.AlternateEmail, "Сменить почту") {
                    // TODO: реализовать смену почты
                }
                RowDivider()
                // Кнопка открывает диалог смены пароля
                ActionRow(Icons.Outlined.Lock, "Сменить пароль") {
                    viewModel.clearPasswordChangeState()
                    showChangePasswordDialog = true
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Выйти ─────────────────────────────────────────────────
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .clickable { scope.launch { viewModel.authRepository.logout() } }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Outlined.Logout, null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp))
                Text(
                    "Выйти из аккаунта",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // ── Диалог смены пароля ───────────────────────────────────────
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isLoading    = viewModel.passwordChangeLoading.collectAsState().value,
            errorMessage = viewModel.passwordChangeError.collectAsState().value,
            onDismiss    = {
                viewModel.clearPasswordChangeState()
                showChangePasswordDialog = false
            },
            onConfirm    = { oldPwd, newPwd ->
                viewModel.changePassword(oldPwd, newPwd)
            }
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// Диалог смены пароля
// Поля: текущий пароль, новый пароль, подтверждение нового пароля.
// Кнопка активна только когда все поля заполнены и новые пароли совпадают.
// ═════════════════════════════════════════════════════════════════

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (oldPassword: String, newPassword: String) -> Unit
) {
    var oldPassword     by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showOld     by remember { mutableStateOf(false) }
    var showNew     by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val passwordsMatch = newPassword == confirmPassword
    val isValid = oldPassword.isNotBlank()
            && newPassword.length >= 6
            && passwordsMatch

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
            ) {
                // Заголовок
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Icon(Icons.Outlined.Lock, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Text("Смена пароля",
                        fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }

                // Текущий пароль
                OutlinedTextField(
                    value         = oldPassword,
                    onValueChange = { oldPassword = it },
                    label         = { Text("Текущий пароль *") },
                    leadingIcon   = { Icon(Icons.Outlined.LockOpen, null) },
                    trailingIcon  = {
                        IconButton(onClick = { showOld = !showOld }) {
                            Icon(
                                if (showOld) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility, null
                            )
                        }
                    },
                    visualTransformation = if (showOld) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    enabled    = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                // Новый пароль
                OutlinedTextField(
                    value         = newPassword,
                    onValueChange = { newPassword = it },
                    label         = { Text("Новый пароль *") },
                    leadingIcon   = { Icon(Icons.Outlined.Lock, null) },
                    trailingIcon  = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                if (showNew) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility, null
                            )
                        }
                    },
                    visualTransformation = if (showNew) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine     = true,
                    modifier       = Modifier.fillMaxWidth(),
                    shape          = RoundedCornerShape(12.dp),
                    enabled        = !isLoading,
                    isError        = newPassword.isNotEmpty() && newPassword.length < 6,
                    supportingText = {
                        if (newPassword.isNotEmpty() && newPassword.length < 6)
                            Text("Минимум 6 символов", color = MaterialTheme.colorScheme.error)
                    }
                )

                Spacer(Modifier.height(10.dp))

                // Подтверждение нового пароля
                OutlinedTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label         = { Text("Повторите новый пароль *") },
                    leadingIcon   = { Icon(Icons.Outlined.Lock, null) },
                    trailingIcon  = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                if (showConfirm) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility, null
                            )
                        }
                    },
                    visualTransformation = if (showConfirm) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine     = true,
                    modifier       = Modifier.fillMaxWidth(),
                    shape          = RoundedCornerShape(12.dp),
                    enabled        = !isLoading,
                    isError        = confirmPassword.isNotEmpty() && !passwordsMatch,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && !passwordsMatch)
                            Text("Пароли не совпадают", color = MaterialTheme.colorScheme.error)
                    }
                )

                // Сообщение об ошибке с сервера
                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            errorMessage,
                            modifier = Modifier.padding(10.dp),
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Кнопки
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = !isLoading
                    ) { Text("Отмена") }

                    Button(
                        onClick  = { if (isValid) onConfirm(oldPassword, newPassword) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = isValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Сохранить", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// Вспомогательные компоненты
// ═════════════════════════════════════════════════════════════════

@Composable
private fun CopyableInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    valueFontSize: Int = 15,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }
    val scope  = rememberCoroutineScope()
    val bgColor by animateColorAsState(
        if (copied) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else Color.Transparent, label = "copy"
    )
    Surface(Modifier.fillMaxWidth(), color = bgColor, shape = RoundedCornerShape(12.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .clickable { onCopy(); scope.launch { copied = true; delay(300); copied = false } }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
                Text(value, fontSize = valueFontSize.sp, fontWeight = FontWeight.SemiBold,
                    color = if (valueColor == Color.Unspecified)
                        MaterialTheme.colorScheme.onSurface else valueColor)
            }
            Icon(
                if (copied) Icons.Outlined.CheckCircle else Icons.Outlined.ContentCopy,
                null,
                tint     = if (copied) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape    = RoundedCornerShape(10.dp),
            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.Outlined.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}