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

    suspend fun getEventById(id: String, accessToken: String? = null): Event

    suspend fun getEventAnalytics(id: String): EventAnalyticDto

    suspend fun getRecentEvents(userId: String): List<Event>
}