package com.events.app.data

import com.events.app.data.remote.EventsApi
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EquipmentTypeDto
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.ParticipantDto
import com.events.app.data.remote.dto.PlaceAvailabilityDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.PlaceTypeDto
import com.events.app.data.remote.dto.ShortEventDto
import com.events.app.data.remote.dto.UserDetailDto
import com.events.app.data.remote.dto.UserDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: EventsApi
) {
    // ── Events ────────────────────────────────────────────────────

    suspend fun getEvents(
        size: Int = 20, page: Int = 1, text: String? = null,
        startDateTime: String? = null, endDateTime: String? = null,
        typeId: Int? = null, formatId: Int? = null, placeId: Int? = null,
        userId: String? = null
    ): List<ShortEventDto> =
        api.getEvents(size, page, text, startDateTime, endDateTime, typeId, formatId, placeId, userId)

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

    suspend fun updateEvent(
        id: String, title: RequestBody? = null, announcement: RequestBody? = null,
        description: RequestBody? = null, startDateTime: RequestBody? = null,
        endDateTime: RequestBody? = null
    ) = api.updateEvent(id, title, announcement, description, startDateTime, endDateTime)

    // ── Analytics ─────────────────────────────────────────────────

    suspend fun getEventAnalytics(id: String): EventAnalyticDto = api.getEventAnalytics(id)

    // ── Participants ──────────────────────────────────────────────

    suspend fun registerForEvent(eventId: String, participantId: String) =
        api.registerForEvent(eventId, participantId)

    suspend fun leaveEvent(eventId: String, participantId: String) =
        api.leaveEvent(eventId, participantId)

    suspend fun getParticipants(eventId: String): List<ParticipantDto> =
        api.getParticipants(eventId)

    // ── Locations ─────────────────────────────────────────────────

    suspend fun getLocations(): List<LocationDto> = api.getLocations()
    suspend fun getLocationById(id: Int): LocationDto = api.getLocationById(id)

    suspend fun createLocation(title: RequestBody, address: RequestBody): Int =
        api.createLocation(title, address)

    suspend fun updateLocation(id: Int, title: RequestBody? = null, address: RequestBody? = null) =
        api.updateLocation(id, title, address)

    suspend fun deleteLocation(id: Int) = api.deleteLocation(id)

    // ── Places ────────────────────────────────────────────────────

    suspend fun getPlacesByLocation(locationId: Int): List<PlaceDto> =
        api.getPlacesByLocation(locationId)

    suspend fun getPlacesAvailability(
        locationId: Int, start: String? = null, end: String? = null
    ): List<PlaceAvailabilityDto> =
        api.getPlacesAvailability(locationId, start, end)

    suspend fun getPlaceById(locationId: Int, placeId: Int): PlaceDto =
        api.getPlaceById(locationId, placeId)

    suspend fun createPlace(
        locationId: Int, number: RequestBody, capacity: RequestBody,
        type: RequestBody, title: RequestBody? = null, photos: List<ByteArray> = emptyList()
    ): Int {
        val photoParts = photos.mapIndexed { idx, bytes ->
            val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("Photos", "photo_${idx + 1}.jpg", body)
        }
        return api.createPlace(locationId, number, capacity, type, title, photoParts)
    }

    suspend fun updatePlace(
        locationId: Int, placeId: Int,
        title: RequestBody? = null, type: RequestBody? = null, capacity: RequestBody? = null
    ): PlaceDto = api.updatePlace(locationId, placeId, title, type, capacity)

    suspend fun deletePlace(locationId: Int, placeId: Int) =
        api.deletePlace(locationId, placeId)

    suspend fun getPlaceTypes(): List<PlaceTypeDto> = api.getPlaceTypes()

    // ── Equipment ─────────────────────────────────────────────────

    suspend fun getEquipment(
        placeId: Int? = null, equipmentTypeId: Int? = null,
        inventoryNumber: String? = null, size: Int = 30, page: Int = 1
    ): List<EquipmentDto> =
        api.getEquipment(size, page, placeId, equipmentTypeId, inventoryNumber)

    suspend fun createEquipment(
        title: RequestBody, inventoryNumber: RequestBody,
        equipmentTypeId: RequestBody, placeId: RequestBody? = null
    ): Int = api.createEquipment(title, inventoryNumber, equipmentTypeId, placeId)

    suspend fun deleteEquipment(id: Int) = api.deleteEquipment(id)
    suspend fun getEquipmentTypes(): List<EquipmentTypeDto> = api.getEquipmentTypes()

    // ── Users ─────────────────────────────────────────────────────

    suspend fun getUsers(size: Int = 20, page: Int = 1): List<UserDto> =
        api.getUsers(size, page)

    suspend fun getUserById(id: String): UserDetailDto = api.getUserById(id)

    suspend fun createUser(
        firstName: RequestBody, lastName: RequestBody, email: RequestBody,
        password: RequestBody, patronymic: RequestBody? = null
    ): String = api.createUser(firstName, lastName, email, password, patronymic)

    suspend fun deleteUser(id: String) = api.deleteUser(id)

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String) {
        fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
        api.changePassword(userId, oldPassword.toBody(), newPassword.toBody())
    }

    suspend fun getRecentEvents(userId: String): List<ShortEventDto> =
        api.getRecentEvents(userId)
}