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
        size: Int,
        page: Int,
        text: String?,
        startDateTime: String?,
        endDateTime: String?,
        typeId: Int?,
        formatId: Int?,
        placeId: Int?,
        userId: String?   // ДОБАВЛЕНО
    ): List<Event> {
        return remoteDataSource.getEvents(
            size, page, text, startDateTime, endDateTime, typeId, formatId, placeId, userId
        ).map { it.toDomain() }
    }

    override suspend fun getEventById(id: String, accessToken: String?): Event {
        return remoteDataSource.getEventById(id, accessToken).toDomain()
    }

    override suspend fun getEventAnalytics(id: String): EventAnalyticDto {
        return remoteDataSource.getEventAnalytics(id)
    }

    override suspend fun getRecentEvents(userId: String): List<Event> {
        return remoteDataSource.getRecentEvents(userId).map { it.toDomain() }
    }
}