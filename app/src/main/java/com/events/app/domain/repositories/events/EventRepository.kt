package com.events.app.domain.repositories.events

import com.events.app.domain.models.events.Event

interface EventRepository {
    suspend fun getEvents(size: Int = 20, page: Int = 1): List<Event>
    suspend fun getEventById(id: String): Event   // UUID — строка
}