package com.events.app.domain.repositories.events

import com.events.app.domain.models.events.Event

interface EventRepository {
    suspend fun getEvents(): List<Event>
}
//определяет, какие методы должны быть (getEvents), но не реализует