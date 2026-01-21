package com.events.app.ui.views.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun AccountScreen() {
    val viewModel: AppViewModel = hiltViewModel()
    val user by viewModel.authRepository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,  // Выравнивание по левому краю
        verticalArrangement = Arrangement.Top
    ) {
        // Аватар с силуэтом человека
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(1f)  // Квадратный для пропорций
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),  // Светлый фон в стиле темы
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .fillMaxSize(0.8f)  // Силуэт внутри, круглый, заполняет 80% бокса
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant  // Цвет в стиле темы
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(3f)
            ) {
                // Имя пользователя
                Text(
                    text = user?.name ?: "Иван",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface  // Цвет текста в стиле темы
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Email
                Text(
                    text = "Email: ${user?.email ?: ""}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant  // Серый в стиле темы
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка "Сменить почту"
        Button(
            onClick = { /* TODO: Логика смены почты */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,  // Фиолетовый в стиле темы
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Сменить почту")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка "Сменить пароль"
        Button(
            onClick = { /* TODO: Логика смены пароля */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,  // Фиолетовый в стиле темы
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Сменить пароль")
        }
        Spacer(modifier = Modifier.weight(1f))  // Отступ до низа

        // Кнопка "Выйти"
        Button(
            onClick = {
                scope.launch {
                    viewModel.authRepository.logout()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,  // Светло-фиолетовый в стиле темы
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("Выйти")
        }
    }
}