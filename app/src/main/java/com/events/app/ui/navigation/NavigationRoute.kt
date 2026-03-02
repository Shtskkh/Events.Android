package com.events.app.ui.navigation

import kotlinx.serialization.Serializable

/*
* В данном интерфейсе прописываются параметры,
* необходимые для перехода на экран.
* Актуально для вложенных экранов.
*/
sealed interface NavigationRoute {

    @Serializable
    data object Login : NavigationRoute

    @Serializable
    data object Main : NavigationRoute

    @Serializable
    data object Events : NavigationRoute

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
        val id: String   // было Int — меняем на String
    ) : NavigationRoute

    @Serializable
    data object Filters : NavigationRoute

}