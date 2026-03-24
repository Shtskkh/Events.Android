package com.events.app.data

import com.events.app.data.remote.EventsApi
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.ShortEventDto
import com.events.app.data.remote.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: EventsApi
) {
    // ── Events ────────────────────────────────────────────────────

    suspend fun getEvents(
        size: Int = 20, page: Int = 1, text: String? = null,
        startDateTime: String? = null, endDateTime: String? = null,
        typeId: Int? = null, formatId: Int? = null
    ): List<ShortEventDto> =
        api.getEvents(size, page, text, startDateTime, endDateTime, typeId, formatId)

    /**
     * Получить мероприятие по ID.
     * [accessToken] — токен пользователя для фиксации просмотра на бэкенде.
     * Передаётся как "Bearer {token}". Если null — просмотр не записывается на пользователя.
     */
    suspend fun getEventById(id: String, accessToken: String? = null): EventDetailDto {
        val authHeader = accessToken?.let { "Bearer $it" }
        return api.getEventById(id, authHeader)
    }

    suspend fun getPlaceholders(): List<String> = api.getPlaceholders()
    suspend fun getEventTypes(): List<EventTypeDto> = api.getEventTypes()
    suspend fun getEventFormats(): List<EventFormatDto> = api.getEventFormats()
    suspend fun deleteEvent(id: String) = api.deleteEvent(id)

    suspend fun createEvent(
        userId: RequestBody, title: RequestBody, announcement: RequestBody,
        description: RequestBody, startDateTime: RequestBody, endDateTime: RequestBody,
        eventTypeId: RequestBody, eventFormatId: RequestBody, needsRegistration: RequestBody,
        maxParticipants: RequestBody? = null, placeId: RequestBody? = null,
        placeholder: RequestBody? = null, preview: MultipartBody.Part? = null
    ): String = api.createEvent(
        userId, title, announcement, description, startDateTime, endDateTime,
        eventTypeId, eventFormatId, needsRegistration, maxParticipants, placeId, placeholder, preview
    )

    // ── Analytics ─────────────────────────────────────────────────

    suspend fun getEventAnalytics(id: String): EventAnalyticDto = api.getEventAnalytics(id)

    suspend fun updateEvent(
        id: String,
        title: RequestBody? = null,
        announcement: RequestBody? = null,
        description: RequestBody? = null,
        startDateTime: RequestBody? = null,
        endDateTime: RequestBody? = null
    ) = api.updateEvent(id, title, announcement, description, startDateTime, endDateTime)

    // ── Participants ──────────────────────────────────────────────

    suspend fun registerForEvent(eventId: String, participantId: String) =
        api.registerForEvent(eventId, participantId)

    suspend fun leaveEvent(eventId: String, participantId: String) =
        api.leaveEvent(eventId, participantId)

    // ── Locations ─────────────────────────────────────────────────

    suspend fun getLocations(): List<LocationDto> = api.getLocations()
    suspend fun getPlacesByLocation(locationId: Int): List<PlaceDto> =
        api.getPlacesByLocation(locationId)

    suspend fun createLocation(title: RequestBody, address: RequestBody): Int =
        api.createLocation(title, address)

    suspend fun deleteLocation(id: Int) = api.deleteLocation(id)

    // ── Users ─────────────────────────────────────────────────────

    suspend fun getUsers(size: Int = 20, page: Int = 1): List<UserDto> =
        api.getUsers(size, page)

    suspend fun createUser(
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        password: RequestBody,
        patronymic: RequestBody? = null
    ): String = api.createUser(firstName, lastName, email, password, patronymic)

    suspend fun deleteUser(id: String) = api.deleteUser(id)

    suspend fun getRecentEvents(userId: String): List<ShortEventDto> =
        api.getRecentEvents(userId)
}