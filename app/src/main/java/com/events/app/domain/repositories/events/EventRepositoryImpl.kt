package com.events.app.domain.repositories.events

import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.data.remote.dto.toDomain
import com.events.app.domain.models.events.Event
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : EventRepository {

    override suspend fun getEvents(
        size: Int, page: Int, text: String?,
        startDateTime: String?, endDateTime: String?,
        typeId: Int?, formatId: Int?, locationId: Int?, placeId: Int?,
        userId: String?,
        createdAfter: String?, createdBefore: String?
    ): List<Event> {
        return remoteDataSource.getEvents(
            size          = size,
            page          = page,
            text          = text,
            startDateTime = startDateTime,
            endDateTime   = endDateTime,
            typeId        = typeId,
            formatId      = formatId,
            locationId    = locationId,
            placeId       = placeId,
            userId        = userId,
            createdAfter  = createdAfter,
            createdBefore = createdBefore
        ).map { it.toDomain() }
    }

    override suspend fun getEventById(id: String, accessToken: String?): Event =
        remoteDataSource.getEventById(id, accessToken).toDomain()

    override suspend fun getEventAnalytics(id: String): EventAnalyticDto =
        remoteDataSource.getEventAnalytics(id)

    override suspend fun getRecentEvents(userId: String): List<Event> =
        remoteDataSource.getRecentEvents(userId).map { it.toDomain() }
}