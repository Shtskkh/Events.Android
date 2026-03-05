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
fun EventsTopBar(
    drawerState: DrawerState,
    onNavigationToAccount: () -> Unit,
    onFiltersClick: () -> Unit = {},   // оставляем параметр чтобы не ломать AppGraph
    content: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Мероприятия",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Меню")
                }
            },
            actions = {
                // Фильтры убраны — только аккаунт
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