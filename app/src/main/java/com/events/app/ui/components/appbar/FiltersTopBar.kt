package com.events.app.ui.components.appbar

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersTopBar(
    onBack: () -> Unit = {},
    onResetAll: () -> Unit = {},
    content: @Composable () -> Unit
) {

    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Column(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Фильтры",
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
                TextButton(onClick = onResetAll) {
                    Text("Сбросить все")
                }
            },
            scrollBehavior = scrollBehavior
        )

        content()
    }
}