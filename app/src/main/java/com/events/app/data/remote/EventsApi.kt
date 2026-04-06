package com.events.app.data.remote

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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface EventsApi {

    // ── Events ────────────────────────────────────────────────────

    @GET("api/v/1/events")
    suspend fun getEvents(
        @Query("Size")          size: Int = 20,
        @Query("Page")          page: Int = 1,
        @Query("Text")          text: String? = null,
        @Query("StartDateTime") startDateTime: String? = null,
        @Query("EndDateTime")   endDateTime: String? = null,
        @Query("TypeId")        typeId: Int? = null,
        @Query("FormatId")      formatId: Int? = null,
        @Query("PlaceId")       placeId: Int? = null,
        @Query("UserId")        userId: String? = null
    ): List<ShortEventDto>

    @GET("api/v/1/events/{id}")
    suspend fun getEventById(
        @Path("id") id: String,
        @Header("Authorization") authorization: String? = null
    ): EventDetailDto

    @GET("api/v/1/events/placeholders")
    suspend fun getPlaceholders(): List<String>

    @GET("api/v/1/events/types")
    suspend fun getEventTypes(): List<EventTypeDto>

    @GET("api/v/1/events/formats")
    suspend fun getEventFormats(): List<EventFormatDto>

    @DELETE("api/v/1/events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)

    @Multipart
    @POST("api/v/1/events")
    suspend fun createEvent(
        @Part("UserId")            userId: RequestBody,
        @Part("Title")             title: RequestBody,
        @Part("Announcement")      announcement: RequestBody,
        @Part("Description")       description: RequestBody,
        @Part("StartDateTime")     startDateTime: RequestBody,
        @Part("EndDateTime")       endDateTime: RequestBody,
        @Part("EventTypeId")       eventTypeId: RequestBody,
        @Part("EventFormatId")     eventFormatId: RequestBody,
        @Part("NeedsRegistration") needsRegistration: RequestBody,
        @Part("MaxParticipants")   maxParticipants: RequestBody? = null,
        @Part("PlaceId")           placeId: RequestBody? = null,
        @Part("Placeholder")       placeholder: RequestBody? = null,
        @Part                      preview: MultipartBody.Part? = null
    ): String

    @Multipart
    @PATCH("api/v/1/events/{id}")
    suspend fun updateEvent(
        @Path("id")            id: String,
        @Part("Title")         title: RequestBody? = null,
        @Part("Announcement")  announcement: RequestBody? = null,
        @Part("Description")   description: RequestBody? = null,
        @Part("StartDateTime") startDateTime: RequestBody? = null,
        @Part("EndDateTime")   endDateTime: RequestBody? = null
    )

    // ── Analytics ─────────────────────────────────────────────────

    @GET("api/v/1/events/{id}/analytics")
    suspend fun getEventAnalytics(@Path("id") id: String): EventAnalyticDto

    // ── Participants ──────────────────────────────────────────────

    @POST("api/v/1/events/{eventId}/participants")
    suspend fun registerForEvent(
        @Path("eventId")        eventId: String,
        @Query("participantId") participantId: String
    )

    @DELETE("api/v/1/events/{eventId}/participants")
    suspend fun leaveEvent(
        @Path("eventId")        eventId: String,
        @Query("participantId") participantId: String
    )

    @GET("api/v/1/events/{id}/participants")
    suspend fun getParticipants(@Path("id") id: String): List<ParticipantDto>

    // ── Locations ─────────────────────────────────────────────────

    @GET("api/v/1/locations")
    suspend fun getLocations(): List<LocationDto>

    @GET("api/v/1/locations/{id}")
    suspend fun getLocationById(@Path("id") id: Int): LocationDto

    @Multipart
    @POST("api/v/1/locations")
    suspend fun createLocation(
        @Part("Title")   title: RequestBody,
        @Part("Address") address: RequestBody
    ): Int

    @Multipart
    @PATCH("api/v/1/locations/{id}")
    suspend fun updateLocation(
        @Path("id")      id: Int,
        @Part("Title")   title: RequestBody? = null,
        @Part("Address") address: RequestBody? = null
    )

    @DELETE("api/v/1/locations/{id}")
    suspend fun deleteLocation(@Path("id") id: Int)

    // ── Places ────────────────────────────────────────────────────

    @GET("api/v/1/locations/{locationId}/places")
    suspend fun getPlacesByLocation(@Path("locationId") locationId: Int): List<PlaceDto>

    @GET("api/v/1/locations/{locationId}/places/availability")
    suspend fun getPlacesAvailability(
        @Path("locationId") locationId: Int,
        @Query("start")     start: String? = null,
        @Query("end")       end: String? = null
    ): List<PlaceAvailabilityDto>

    @GET("api/v/1/locations/{locationId}/places/{placeId}")
    suspend fun getPlaceById(
        @Path("locationId") locationId: Int,
        @Path("placeId")    placeId: Int
    ): PlaceDto

    @Multipart
    @POST("api/v/1/locations/{locationId}/places")
    suspend fun createPlace(
        @Path("locationId") locationId: Int,
        @Part("Number")     number: RequestBody,
        @Part("Capacity")   capacity: RequestBody,
        @Part("Type")       type: RequestBody,
        @Part("Title")      title: RequestBody? = null,
        @Part               photos: List<MultipartBody.Part> = emptyList()
    ): Int

    @Multipart
    @PATCH("api/v/1/locations/{locationId}/places/{placeId}")
    suspend fun updatePlace(
        @Path("locationId") locationId: Int,
        @Path("placeId")    placeId: Int,
        @Part("Title")      title: RequestBody? = null,
        @Part("Type")       type: RequestBody? = null,
        @Part("Capacity")   capacity: RequestBody? = null
    ): PlaceDto

    @DELETE("api/v/1/locations/{locationId}/places/{placeId}")
    suspend fun deletePlace(
        @Path("locationId") locationId: Int,
        @Path("placeId")    placeId: Int
    )

    @GET("api/v/1/locations/places/types")
    suspend fun getPlaceTypes(): List<PlaceTypeDto>

    // ── Equipment ─────────────────────────────────────────────────

    @GET("api/v/1/equipment")
    suspend fun getEquipment(
        @Query("Size")            size: Int = 30,
        @Query("Page")            page: Int = 1,
        @Query("PlaceId")         placeId: Int? = null,
        @Query("EquipmentTypeId") equipmentTypeId: Int? = null,
        @Query("InventoryNumber") inventoryNumber: String? = null
    ): List<EquipmentDto>

    @Multipart
    @POST("api/v/1/equipment")
    suspend fun createEquipment(
        @Part("Title")           title: RequestBody,
        @Part("InventoryNumber") inventoryNumber: RequestBody,
        @Part("EquipmentTypeId") equipmentTypeId: RequestBody,
        @Part("PlaceId")         placeId: RequestBody? = null
    ): Int

    @DELETE("api/v/1/equipment/{id}")
    suspend fun deleteEquipment(@Path("id") id: Int)

    @GET("api/v/1/equipment/types")
    suspend fun getEquipmentTypes(): List<EquipmentTypeDto>

    // ── Users ─────────────────────────────────────────────────────

    @GET("api/v/1/users")
    suspend fun getUsers(
        @Query("Size") size: Int = 20,
        @Query("Page") page: Int = 1
    ): List<UserDto>

    /**
     * Получить пользователя по ID.
     * GET /api/v/1/users/{id}
     * Возвращает: id, lastName, firstName, patronymic, email, avatarInfo
     */
    @GET("api/v/1/users/{id}")
    suspend fun getUserById(@Path("id") id: String): UserDetailDto

    @Multipart
    @POST("api/v/1/users")
    suspend fun createUser(
        @Part("FirstName")  firstName: RequestBody,
        @Part("LastName")   lastName: RequestBody,
        @Part("Email")      email: RequestBody,
        @Part("Password")   password: RequestBody,
        @Part("Patronymic") patronymic: RequestBody? = null
    ): String

    @DELETE("api/v/1/users/{id}")
    suspend fun deleteUser(@Path("id") id: String)

    @Multipart
    @PATCH("api/v/1/users/{id}/password")
    suspend fun changePassword(
        @Path("id")          id: String,
        @Part("OldPassword") oldPassword: RequestBody,
        @Part("NewPassword") newPassword: RequestBody
    )

    @GET("api/v/1/users/{id}/events/recent")
    suspend fun getRecentEvents(@Path("id") id: String): List<ShortEventDto>
}