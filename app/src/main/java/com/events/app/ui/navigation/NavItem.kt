package com.events.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

sealed class NavItem(
    val route: NavigationRoute,
    val title: String,
    val iconOutlined: @Composable (() -> Unit)? = null,
    val iconSelected: @Composable (() -> Unit)? = null
) {
    object Login : NavItem(
        route = NavigationRoute.Login,
        title = "Вход",
    )

    object Main : NavItem(
        route = NavigationRoute.Main,
        title = "Главная",
        iconOutlined = { Icon(Icons.Outlined.Home, "Главная") },
        iconSelected = { Icon(Icons.Filled.Home, "Главная") }
    )

    object Events : NavItem(
        route = NavigationRoute.Events,
        title = "Мероприятия",
        iconOutlined = { Icon(Icons.Outlined.Event, "Мероприятия") },
        iconSelected = { Icon(Icons.Filled.Event, "Мероприятия") }
    )

    object CreateEvent : NavItem(
        route = NavigationRoute.CreateEventGraph,
        title = "Создать мероприятие",
        iconOutlined = { Icon(Icons.Outlined.Add, "Создать мероприятие") },
        iconSelected = { Icon(Icons.Filled.Add, "Создать мероприятие") }
    )

    object Statistics : NavItem(
        route = NavigationRoute.Statistics,
        title = "Статистика",
        iconOutlined = { Icon(Icons.Outlined.BarChart, "Статистика") },
        iconSelected = { Icon(Icons.Filled.BarChart, "Статистика") }
    )

    object Settings : NavItem(
        route = NavigationRoute.Settings,
        title = "Настройки",
        iconOutlined = { Icon(Icons.Outlined.Settings, "Настройки") },
        iconSelected = { Icon(Icons.Filled.Settings, "Настройки") }
    )

    object Account : NavItem(
        route = NavigationRoute.Account,
        title = "Аккаунт",
        iconOutlined = { Icon(Icons.Outlined.AccountCircle, "Аккаунт") },
        iconSelected = { Icon(Icons.Filled.AccountCircle, "Аккаунт") }
    )

    // Только для ADMIN — добавляется в Drawer условно
    object Admin : NavItem(
        route = NavigationRoute.Admin,
        title = "Администрирование",
        iconOutlined = { Icon(Icons.Outlined.AdminPanelSettings, "Администрирование") },
        iconSelected = { Icon(Icons.Filled.AdminPanelSettings, "Администрирование") }
    )
}