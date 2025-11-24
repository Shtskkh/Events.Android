package com.events.app.ui.navigation

import androidx.compose.runtime.Composable

// Функция получения названия экрана по его пути
@Composable
fun getTitleForRoute(currentRoute: NavigationRoute?): String {
    return when (currentRoute) {
        is NavigationRoute.Main -> "Главная"
        is NavigationRoute.Events -> "Мероприятия"
        is NavigationRoute.Settings -> "Настройки"
        is NavigationRoute.Account -> "Аккаунт"
        else -> "Приложение"
    }
}

@Composable
fun getNavItemForRoute(currentRoute: NavigationRoute?): NavItem? {
    return when (currentRoute) {
        is NavigationRoute.Main -> NavItem.Main
        is NavigationRoute.Events -> NavItem.Events
        is NavigationRoute.Settings -> NavItem.Settings
        is NavigationRoute.Account -> NavItem.Account
        else -> null
    }
}