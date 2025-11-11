package com.events.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())  // Для фиксации при скролле

    CenterAlignedTopAppBar(  // Используем CenterAligned для лучшего центрирования
        title = {
            Text(
                text = "Главная",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis  // Если текст длинный, обрезает
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* TODO: Добавьте действие */ }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Меню"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Добавьте действие */ }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Профиль"
                )
            }
        },
        scrollBehavior = scrollBehavior  // Фиксирует бар при скролле
    )
}