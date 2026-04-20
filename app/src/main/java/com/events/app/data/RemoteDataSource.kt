package com.events.app.data

import com.events.app.data.remote.EventsApi
import com.events.app.data.remote.dto.EquipmentDto
import com.events.app.data.remote.dto.EquipmentTypeDto
import com.events.app.data.remote.dto.EventAnalyticDto
import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.EventsCountAnalyticsDto
import com.events.app.data.remote.dto.FormatAnalyticsItemDto
import com.events.app.data.remote.dto.LocationAnalyticsItemDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.ParticipantDto
import com.events.app.data.remote.dto.PlaceAnalyticsItemDto
import com.events.app.data.remote.dto.PlaceAvailabilityDto
import com.events.app.data.remote.dto.PlaceDto
import com.events.app.data.remote.dto.PlaceTypeDto
import com.events.app.data.remote.dto.ShortEventDto
import com.events.app.data.remote.dto.TagAnalyticsItemDto
import com.events.app.data.remote.dto.TagDto
import com.events.app.data.remote.dto.TypeAnalyticsItemDto
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
        size: Int = 20,
        page: Int = 1,
        text: String? = null,
        startDateTime: String? = null,
        endDateTime: String? = null,
        typeId: Int? = null,
        formatId: Int? = null,
        locationId: Int? = null,
        placeId: Int? = null,
        userId: String? = null,
        createdAfter: String? = null,
        createdBefore: String? = null
    ): List<ShortEventDto> = api.getEvents(
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
    )

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
        maxParticipants: RequestBody? = null,
        locationId: RequestBody? = null,
        placeId: RequestBody? = null,
        placeholder: RequestBody? = null,
        preview: MultipartBody.Part? = null,
        tagIds: List<Int> = emptyList()
    ): String {
        val tagParts = tagIds.map { id ->
            MultipartBody.Part.createFormData("TagsIds", id.toString())
        }
        return api.createEvent(
            userId, title, announcement, description, startDateTime, endDateTime,
            eventTypeId, eventFormatId, needsRegistration, maxParticipants,
            locationId, placeId, placeholder, preview, tagParts
        )
    }

    suspend fun updateEvent(
        id: String, title: RequestBody? = null, announcement: RequestBody? = null,
        description: RequestBody? = null, startDateTime: RequestBody? = null,
        endDateTime: RequestBody? = null
    ) = api.updateEvent(id, title, announcement, description, startDateTime, endDateTime)

    // ── Analytics ─────────────────────────────────────────────────

    suspend fun getEventsAnalytics(): EventsCountAnalyticsDto = api.getEventsAnalytics()
    suspend fun getEventAnalytics(id: String): EventAnalyticDto = api.getEventAnalytics(id)
    suspend fun getTagsAnalytics(from: String? = null, to: String? = null, top: Int? = null): List<TagAnalyticsItemDto> =
        api.getTagsAnalytics(from, to, top)
    suspend fun getTypesAnalytics(start: String? = null, end: String? = null): List<TypeAnalyticsItemDto> =
        api.getTypesAnalytics(start, end)
    suspend fun getFormatsAnalytics(start: String? = null, end: String? = null): List<FormatAnalyticsItemDto> =
        api.getFormatsAnalytics(start, end)
    suspend fun getLocationsAnalytics(from: String? = null, to: String? = null): List<LocationAnalyticsItemDto> =
        api.getLocationsAnalytics(from, to)
    suspend fun getPlacesAnalytics(from: String? = null, to: String? = null, top: Int? = null): List<PlaceAnalyticsItemDto> =
        api.getPlacesAnalytics(from, to, top)

    // ── Tags ──────────────────────────────────────────────────────

    suspend fun getTags(titleLike: String? = null, size: Int = 50, page: Int = 1): List<TagDto> =
        api.getTags(titleLike, size, page)

    suspend fun createTag(name: String): Int {
        val body = "\"$name\"".toRequestBody("application/json".toMediaTypeOrNull())
        return api.createTag(body)
    }

    suspend fun deleteTag(id: Int) = api.deleteTag(id)

    suspend fun addTagToEvent(eventId: String, tagId: Int) = api.addTagToEvent(eventId, tagId)

    suspend fun removeTagFromEvent(eventId: String, tagId: Int) = api.removeTagFromEvent(eventId, tagId)

    // ── Participants ──────────────────────────────────────────────

    suspend fun registerForEvent(eventId: String, participantId: String, accessToken: String? = null) =
        api.registerForEvent(eventId, participantId, accessToken?.let { "Bearer $it" })

    suspend fun leaveEvent(eventId: String, participantId: String, accessToken: String? = null) =
        api.leaveEvent(eventId, accessToken?.let { "Bearer $it" })

    suspend fun getParticipants(eventId: String): List<ParticipantDto> =
        api.getParticipants(eventId)

    // ── Locations ─────────────────────────────────────────────────

    suspend fun getLocations(): List<LocationDto> = api.getLocations()
    suspend fun getLocationById(id: Int): LocationDto = api.getLocationById(id)

    suspend fun createLocation(
        title: RequestBody,
        address: RequestBody,
        photos: List<MultipartBody.Part> = emptyList()
    ): Int = api.createLocation(title, address, photos)

    suspend fun updateLocation(
        id: Int,
        title: RequestBody? = null,
        address: RequestBody? = null,
        newPhotoBytes: List<ByteArray> = emptyList()
    ) {
        val photoParts = mutableListOf<MultipartBody.Part>()
        newPhotoBytes.forEachIndexed { i, bytes ->
            photoParts.add(MultipartBody.Part.createFormData("Photos[$i].existingFilename", ""))
            val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            photoParts.add(MultipartBody.Part.createFormData("Photos[$i].newFile", "photo_${i + 1}.jpg", body))
        }
        api.updateLocation(id, title, address, photoParts)
    }

    suspend fun deleteLocation(id: Int) = api.deleteLocation(id)

    // ── Places ────────────────────────────────────────────────────

    suspend fun getPlacesByLocation(locationId: Int): List<PlaceDto> =
        api.getPlacesByLocation(locationId)

    suspend fun getPlacesAvailability(
        locationId: Int, start: String? = null, end: String? = null
    ): List<PlaceAvailabilityDto> = api.getPlacesAvailability(locationId, start, end)

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
        title: RequestBody? = null, type: RequestBody? = null, capacity: RequestBody? = null,
        newPhotoBytes: List<ByteArray> = emptyList()
    ): PlaceDto {
        val photoParts = mutableListOf<MultipartBody.Part>()
        newPhotoBytes.forEachIndexed { i, bytes ->
            photoParts.add(MultipartBody.Part.createFormData("Photos[$i].existingFilename", ""))
            val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            photoParts.add(MultipartBody.Part.createFormData("Photos[$i].newFile", "photo_${i + 1}.jpg", body))
        }
        return api.updatePlace(locationId, placeId, title, type, capacity, photoParts)
    }

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

    suspend fun updateUser(
        id: String,
        firstName: RequestBody? = null,
        lastName: RequestBody? = null,
        patronymic: RequestBody? = null,
        roleId: RequestBody? = null,
        avatarBytes: ByteArray? = null
    ) {
        val avatarPart = avatarBytes?.let {
            val body = it.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("Avatar", "avatar.jpg", body)
        }
        api.updateUser(id, firstName, lastName, patronymic, roleId, avatarPart)
    }

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String) {
        fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
        api.changePassword(userId, oldPassword.toBody(), newPassword.toBody())
    }

    suspend fun getRecentEvents(userId: String): List<ShortEventDto> =
        api.getRecentEvents(userId)
}