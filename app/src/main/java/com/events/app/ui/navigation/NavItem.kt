package com.events.app.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

// Класс для элементов навигации
sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
) {
    object Login : NavItem(
        route = "login",
        title = "Вход",
    )

    object Main : NavItem(
        route = "main",
        title = "Главная"
    )

    object Events : NavItem(
        route = "events",
        title = "Мероприятия"
    )

    object Settings : NavItem(
        route = "settings",
        title = "Настройки"
    )

    object Account : NavItem(
        route = "account",
        title = "Аккаунт"
    )
}

// Функция получения названия экрана по его пути
fun getTitleForRoute(route: String): String {
    return when (route) {
        NavItem.Login.route -> NavItem.Login.title
        NavItem.Main.route -> NavItem.Main.title
        NavItem.Events.route -> NavItem.Events.title
        NavItem.Settings.route -> NavItem.Settings.title
        NavItem.Account.route -> NavItem.Account.title
        else -> "Приложение"
    }
}