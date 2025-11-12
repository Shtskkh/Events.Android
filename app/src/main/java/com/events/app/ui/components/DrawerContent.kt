package com.events.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.events.app.ui.navigation.NavigationRoute
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    navController: NavController,
    currentRoute: String,
) {
    // Здесь прописываются пути для бокового меню
    val routes = listOf(
        NavigationRoute.Main,
        NavigationRoute.Events,
        NavigationRoute.Settings
    )

    val scope = rememberCoroutineScope()

    ModalDrawerSheet {
        routes.forEach { route ->
            NavigationDrawerItem(
                icon = { route.icon?.let { Icon(it, contentDescription = null) } },
                label = { Text(route.title) },
                selected = currentRoute == route.route,
                onClick = {
                    navController.navigate(route.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}