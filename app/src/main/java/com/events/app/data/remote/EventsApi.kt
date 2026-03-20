package com.events.app.data.remote

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
        @Query("Size") size: Int = 20,
        @Query("Page") page: Int = 1,
        @Query("Text") text: String? = null,
        @Query("StartDateTime") startDateTime: String? = null,
        @Query("EndDateTime") endDateTime: String? = null,
        @Query("TypeId") typeId: Int? = null,
        @Query("FormatId") formatId: Int? = null
    ): List<ShortEventDto>

    @GET("api/v/1/events/{id}")
    suspend fun getEventById(@Path("id") id: String): EventDetailDto

    @GET("api/v/1/events/placeholders")
    suspend fun getPlaceholders(): List<String>

    @GET("api/v/1/events/types")
    suspend fun getEventTypes(): List<EventTypeDto>

    @GET("api/v/1/events/formats")
    suspend fun getEventFormats(): List<EventFormatDto>

    @DELETE("api/v/1/events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)

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

    // ── Events create ─────────────────────────────────────────────

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

    // ── Users (Admin) ─────────────────────────────────────────────

    @GET("api/v/1/users")
    suspend fun getUsers(): List<UserDto>

    @Multipart
    @POST("api/v/1/users/register")
    suspend fun createUser(
        @Part("Email") email: RequestBody,
        @Part("Password") password: RequestBody,
        @Part("Name") name: RequestBody,
        @Part("Role") role: RequestBody
    ): String

    @DELETE("api/v/1/users/{id}")
    suspend fun deleteUser(@Path("id") id: String)
}