package com.events.app.ui.components.appbar

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsTopBar(
    title: String = "Детали мероприятия",  // Новый параметр с дефолтным значением
    onNavigationToAccount: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit  // Оставляем как есть
) {
    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Column(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,  // Теперь используем переданный title
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigationToAccount) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Профиль"
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )

        content()
    }
}