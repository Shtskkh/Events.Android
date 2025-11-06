package com.events.app.domain.repository

import com.events.app.domain.model.Event

interface EventRepository {
    suspend fun getEvents(): List<Event>
}
//определяет, какие методы должны быть (getEvents), но не реализует