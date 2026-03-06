package com.events.app.domain.repositories.events

import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.toDomain
import com.events.app.domain.models.events.Event
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : EventRepository {

    override suspend fun getEvents(
        size: Int,
        page: Int,
        text: String?,
        startDateTime: String?,
        endDateTime: String?,
        typeId: Int?,
        formatId: Int?
    ): List<Event> {
        return remoteDataSource.getEvents(
            size, page, text, startDateTime, endDateTime, typeId, formatId
        ).map { it.toDomain() }
    }

    override suspend fun getEventById(id: String): Event {
        return remoteDataSource.getEventById(id).toDomain()
    }
}