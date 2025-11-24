package com.events.app.ui.components.drawers


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.events.app.ui.navigation.NavigationRoute
import com.events.app.ui.navigation.getNavItemForRoute
import com.events.app.ui.navigation.getTitleForRoute
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    fun closeDrawer() {
        scope.launch {
            drawerState.close()
        }
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {

            DrawerNavItem(
                route = NavigationRoute.Main,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            DrawerNavItem(
                route = NavigationRoute.Events,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            HorizontalDivider()

            DrawerNavItem(
                route = NavigationRoute.Account,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            DrawerNavItem(
                route = NavigationRoute.Settings,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Версия 0.2.0",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DrawerNavItem(
    route: NavigationRoute,
    currentRoute: String?,
    navController: NavController,
    onClick: () -> Unit
) {
    val navItem = getNavItemForRoute(route)
    val itemText = getTitleForRoute(route)

    val selected = currentRoute == route::class.qualifiedName

    NavigationDrawerItem(
        label = { Text(itemText) },
        selected = selected,
        onClick = {
            onClick()
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}