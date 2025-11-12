package com.events.app.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

// Класс для пути в приложении
sealed class NavigationRoute(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
) {
    object Main : NavigationRoute(
        route = "main",
        title = "Главная"
    )

    object Events : NavigationRoute(
        route = "events",
        title = "Мероприятия"
    )

    object Settings : NavigationRoute(
        route = "settings",
        title = "Настройки"
    )

    object Account : NavigationRoute(
        route = "account",
        title = "Аккаунт"
    )
}

// Функция получения названия экрана по его пути
fun getTitleForRoute(route: String): String {
    return when (route) {
        NavigationRoute.Main.route -> NavigationRoute.Main.title
        NavigationRoute.Events.route -> NavigationRoute.Events.title
        NavigationRoute.Settings.route -> NavigationRoute.Settings.title
        NavigationRoute.Account.route -> NavigationRoute.Account.title
        else -> "Приложение"
    }
}