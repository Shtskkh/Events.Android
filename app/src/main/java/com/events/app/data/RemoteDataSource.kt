package com.events.app.data

import com.events.app.data.remote.EventsApi
import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.ShortEventDto
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: EventsApi
) {
    suspend fun getEvents(size: Int = 20, page: Int = 1): List<ShortEventDto> {
        return api.getEvents(size = size, page = page)
    }

    suspend fun getEventById(id: String): EventDetailDto {
        return api.getEventById(id)
    }
}