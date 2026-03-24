package com.events.app.domain.repositories.events

import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.domain.models.events.Event

interface EventRepository {
    suspend fun getEvents(
        size: Int = 20,
        page: Int = 1,
        text: String? = null,
        startDateTime: String? = null,
        endDateTime: String? = null,
        typeId: Int? = null,
        formatId: Int? = null
    ): List<Event>

    /**
     * [accessToken] передаётся чтобы бэкенд записал просмотр на пользователя.
     */
    suspend fun getEventById(id: String, accessToken: String? = null): Event

    /**
     * Получить аналитику мероприятия.
     */
    suspend fun getEventAnalytics(id: String): EventAnalyticDto

    /**
     * Последние 10 просмотренных мероприятий для данного пользователя.
     */
    suspend fun getRecentEvents(userId: String): List<Event>
}