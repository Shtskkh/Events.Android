package com.events.app.domain.repositories.events

import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.toDomain
import com.events.app.domain.models.events.Event
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : EventRepository {

    override suspend fun getEvents(size: Int, page: Int): List<Event> {
        return remoteDataSource.getEvents(size, page).map { it.toDomain() }
    }

    override suspend fun getEventById(id: String): Event {
        return remoteDataSource.getEventById(id).toDomain()
    }
}