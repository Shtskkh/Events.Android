package com.events.app.ui.navigation

import kotlinx.serialization.Serializable

sealed interface NavigationRoute {

    @Serializable
    data object Login : NavigationRoute

    @Serializable
    data object Main : NavigationRoute

    @Serializable
    data object Events : NavigationRoute

    @Serializable
    data object CreateEventGraph : NavigationRoute

    @Serializable
    data object CreateEvent : NavigationRoute

    @Serializable
    data object CreateEventStep2 : NavigationRoute

    @Serializable
    data object Settings : NavigationRoute

    @Serializable
    data object Account : NavigationRoute

    @Serializable
    data class EventDetails(
        val id: String
    ) : NavigationRoute

    @Serializable
    data object Filters : NavigationRoute

    @Serializable
    data object Statistics : NavigationRoute

    @Serializable
    data object Admin : NavigationRoute   // ← только для ADMIN
}