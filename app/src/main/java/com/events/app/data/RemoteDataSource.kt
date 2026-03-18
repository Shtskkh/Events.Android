package com.events.app.data

import com.events.app.data.remote.EventsApi
import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.ShortEventDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: EventsApi
) {
    suspend fun getEvents(
        size: Int = 20,
        page: Int = 1,
        text: String? = null,
        startDateTime: String? = null,
        endDateTime: String? = null,
        typeId: Int? = null,
        formatId: Int? = null
    ): List<ShortEventDto> = api.getEvents(size, page, text, startDateTime, endDateTime, typeId, formatId)

    suspend fun getEventById(id: String): EventDetailDto =
        api.getEventById(id)

    suspend fun getPlaceholders(): List<String> =
        api.getPlaceholders()

    suspend fun getEventTypes(): List<EventTypeDto> =
        api.getEventTypes()

    suspend fun getEventFormats(): List<EventFormatDto> =
        api.getEventFormats()

    suspend fun getLocations(): List<LocationDto> =
        api.getLocations()

    suspend fun createLocation(
        title: RequestBody,
        address: RequestBody
    ): Int = api.createLocation(title, address)

    suspend fun createEvent(
        title: RequestBody,
        announcement: RequestBody,
        description: RequestBody,
        startDateTime: RequestBody,
        endDateTime: RequestBody,
        eventTypeId: RequestBody,
        eventFormatId: RequestBody,
        needsRegistration: RequestBody,
        placeholder: RequestBody? = null,
        preview: MultipartBody.Part? = null
    ): String = api.createEvent(
        title, announcement, description,
        startDateTime, endDateTime,
        eventTypeId, eventFormatId, needsRegistration,
        placeholder, preview
    )
}