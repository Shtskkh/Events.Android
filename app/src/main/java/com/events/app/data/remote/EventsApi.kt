package com.events.app.data.remote

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
import retrofit2.http.*

interface EventsApi {

    // ── Events ────────────────────────────────────────────────────

    @GET("api/v/1/events")
    suspend fun getEvents(
        @Query("Size") size: Int = 20,       // max 30 по Scalar!
        @Query("Page") page: Int = 1,
        @Query("Text") text: String? = null,
        @Query("StartDateTime") startDateTime: String? = null,
        @Query("EndDateTime") endDateTime: String? = null,
        @Query("TypeId") typeId: Int? = null,
        @Query("FormatId") formatId: Int? = null
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
        @Part("UserId") userId: RequestBody,
        @Part("Title") title: RequestBody,
        @Part("Announcement") announcement: RequestBody,
        @Part("Description") description: RequestBody,
        @Part("StartDateTime") startDateTime: RequestBody,
        @Part("EndDateTime") endDateTime: RequestBody,
        @Part("EventTypeId") eventTypeId: RequestBody,
        @Part("EventFormatId") eventFormatId: RequestBody,
        @Part("NeedsRegistration") needsRegistration: RequestBody,
        @Part("MaxParticipants") maxParticipants: RequestBody? = null,
        @Part("PlaceId") placeId: RequestBody? = null,
        @Part("Placeholder") placeholder: RequestBody? = null,
        @Part preview: MultipartBody.Part? = null
    ): String

    @Multipart
    @PATCH("api/v/1/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Part("Title") title: RequestBody? = null,
        @Part("Announcement") announcement: RequestBody? = null,
        @Part("Description") description: RequestBody? = null,
        @Part("StartDateTime") startDateTime: RequestBody? = null,
        @Part("EndDateTime") endDateTime: RequestBody? = null
    )

    // ── Analytics ─────────────────────────────────────────────────
    @GET("api/v/1/events/{id}/analytics")
    suspend fun getEventAnalytics(@Path("id") id: String): EventAnalyticDto

    // ── Participants ──────────────────────────────────────────────

    @POST("api/v/1/events/{eventId}/participants")
    suspend fun registerForEvent(
        @Path("eventId") eventId: String,
        @Query("participantId") participantId: String
    )

    @DELETE("api/v/1/events/{eventId}/participants")
    suspend fun leaveEvent(
        @Path("eventId") eventId: String,
        @Query("participantId") participantId: String
    )

    // ── Locations ─────────────────────────────────────────────────

    @GET("api/v/1/locations")
    suspend fun getLocations(): List<LocationDto>

    @GET("api/v/1/locations/{locationId}/places")
    suspend fun getPlacesByLocation(@Path("locationId") locationId: Int): List<PlaceDto>

    @Multipart
    @POST("api/v/1/locations")
    suspend fun createLocation(
        @Part("Title") title: RequestBody,
        @Part("Address") address: RequestBody
    ): Int

    @DELETE("api/v/1/locations/{id}")
    suspend fun deleteLocation(@Path("id") id: Int)

    // ── Users ─────────────────────────────────────────────────────

    @GET("api/v/1/users")
    suspend fun getUsers(
        @Query("Size") size: Int = 20,
        @Query("Page") page: Int = 1
    ): List<UserDto>

    @Multipart
    @POST("api/v/1/users")
    suspend fun createUser(
        @Part("FirstName") firstName: RequestBody,
        @Part("LastName") lastName: RequestBody,
        @Part("Email") email: RequestBody,
        @Part("Password") password: RequestBody,
        @Part("Patronymic") patronymic: RequestBody? = null
    ): String   // возвращает UUID созданного пользователя

    @DELETE("api/v/1/users/{id}")
    suspend fun deleteUser(@Path("id") id: String)

    @GET("api/v/1/users/{id}/events/recent")
    suspend fun getRecentEvents(@Path("id") id: String): List<ShortEventDto>
}