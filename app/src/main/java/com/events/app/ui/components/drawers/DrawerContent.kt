package com.events.app.ui.components.drawers


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.events.app.ui.navigation.NavItem
import com.events.app.ui.navigation.NavigationRoute
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
                route = NavItem.Main.route,
                label = NavItem.Main.title,
                iconOutlined = NavItem.Main.iconOutlined,
                iconSelected = NavItem.Main.iconSelected,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            DrawerNavItem(
                route = NavItem.Events.route,
                label = NavItem.Events.title,
                iconOutlined = NavItem.Events.iconOutlined,
                iconSelected = NavItem.Events.iconSelected,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            DrawerNavItem(
                route = NavItem.CreateEvent.route,
                label = NavItem.CreateEvent.title,
                iconOutlined = NavItem.CreateEvent.iconOutlined,
                iconSelected = NavItem.CreateEvent.iconSelected,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            DrawerNavItem(
                route = NavItem.Statistics.route,
                label = NavItem.Statistics.title,
                iconOutlined = NavItem.Statistics.iconOutlined,
                iconSelected = NavItem.Statistics.iconSelected,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(8.dp)
            )

            DrawerNavItem(
                route = NavItem.Account.route,
                label = NavItem.Account.title,
                iconOutlined = NavItem.Account.iconOutlined,
                iconSelected = NavItem.Account.iconSelected,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

            DrawerNavItem(
                route = NavItem.Settings.route,
                label = NavItem.Settings.title,
                iconOutlined = NavItem.Settings.iconOutlined,
                iconSelected = NavItem.Settings.iconSelected,
                currentRoute = currentRoute,
                navController = navController,
                onClick = { closeDrawer() },
            )

        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Версия 0.3.0",
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
    label: String,
    currentRoute: String?,
    iconOutlined: @Composable (() -> Unit)? = null,
    iconSelected: @Composable (() -> Unit)? = null,
    navController: NavController,
    onClick: () -> Unit
) {
    val selected = currentRoute == route::class.qualifiedName

    NavigationDrawerItem(
        label = { Text(label) },
        selected = selected,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        icon = {
            if (selected) {
                iconSelected?.invoke()
            } else
                iconOutlined?.invoke()
        },
        onClick = {
            val popped = navController.popBackStack(route, inclusive = false)
            if (!popped) {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            onClick()
        }
    )
}