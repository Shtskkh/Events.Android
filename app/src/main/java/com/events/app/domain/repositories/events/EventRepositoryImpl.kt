package com.events.app.domain.repositories.events

import com.events.app.data.LocalDataSource
import com.events.app.domain.models.events.Event
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : EventRepository {
    override suspend fun getEvents(): List<Event> {
        return localDataSource.getEvents()
    }
}
//берёт данные из LocalDataSource и возвращает в удобном формате