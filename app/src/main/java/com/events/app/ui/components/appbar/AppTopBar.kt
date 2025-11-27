package com.events.app.ui.components.appbar

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    drawerState: DrawerState,
    onNavigationToAccount: () -> Unit,
    content: @Composable () -> Unit
) {

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        CenterAlignedTopAppBar(  // Используем CenterAligned для лучшего центрирования
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis  // Если текст длинный, обрезает
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Меню"
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigationToAccount) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Профиль"
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )

        content()
    }
}