package com.events.app.data.remote

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
        @Query("LocationId")    locationId: Int? = null,
        @Query("PlaceId")       placeId: Int? = null,
        @Query("UserId")        userId: String? = null,
        @Query("CreatedAfter")  createdAfter: String? = null,
        @Query("CreatedBefore") createdBefore: String? = null
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
        @Part("LocationId")        locationId: RequestBody? = null,
        @Part("PlaceId")           placeId: RequestBody? = null,
        @Part("Placeholder")       placeholder: RequestBody? = null,
        @Part                      preview: MultipartBody.Part? = null,
        @Part                      tagIds: List<MultipartBody.Part> = emptyList()
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

    @GET("api/v/1/events/analytics")
    suspend fun getEventsAnalytics(): EventsCountAnalyticsDto

    @GET("api/v/1/events/{id}/analytics")
    suspend fun getEventAnalytics(@Path("id") id: String): EventAnalyticDto

    @GET("api/v/1/events/tags/analytics")
    suspend fun getTagsAnalytics(
        @Query("from") from: String? = null,
        @Query("to")   to: String? = null,
        @Query("top")  top: Int? = null
    ): List<TagAnalyticsItemDto>

    @GET("api/v/1/events/types/analytics")
    suspend fun getTypesAnalytics(
        @Query("start") start: String? = null,
        @Query("end")   end: String? = null
    ): List<TypeAnalyticsItemDto>

    @GET("api/v/1/events/formats/analytics")
    suspend fun getFormatsAnalytics(
        @Query("start") start: String? = null,
        @Query("end")   end: String? = null
    ): List<FormatAnalyticsItemDto>

    @GET("api/v/1/events/locations/analytics")
    suspend fun getLocationsAnalytics(
        @Query("from") from: String? = null,
        @Query("to")   to: String? = null
    ): List<LocationAnalyticsItemDto>

    @GET("api/v/1/events/places/analytics")
    suspend fun getPlacesAnalytics(
        @Query("from") from: String? = null,
        @Query("to")   to: String? = null,
        @Query("top")  top: Int? = null
    ): List<PlaceAnalyticsItemDto>

    // ── Tags ──────────────────────────────────────────────────────

    @GET("api/v/1/tags")
    suspend fun getTags(
        @Query("TitleLike") titleLike: String? = null,
        @Query("Size")      size: Int = 50,
        @Query("Page")      page: Int = 1
    ): List<TagDto>

    @POST("api/v/1/tags")
    suspend fun createTag(@Body name: RequestBody): Int

    @DELETE("api/v/1/tags/{id}")
    suspend fun deleteTag(@Path("id") id: Int)

    @POST("api/v/1/events/{eventId}/tags/{tagId}")
    suspend fun addTagToEvent(
        @Path("eventId") eventId: String,
        @Path("tagId")   tagId: Int
    )

    @DELETE("api/v/1/events/{eventId}/tags/{tagId}")
    suspend fun removeTagFromEvent(
        @Path("eventId") eventId: String,
        @Path("tagId")   tagId: Int
    )

    // ── Participants ──────────────────────────────────────────────

    @POST("api/v/1/events/{eventId}/participants")
    suspend fun registerForEvent(
        @Path("eventId")         eventId: String,
        @Query("participantId")  participantId: String,
        @Header("Authorization") authorization: String? = null
    )

    @DELETE("api/v/1/events/{eventId}/participants")
    suspend fun leaveEvent(
        @Path("eventId")         eventId: String,
        @Header("Authorization") authorization: String? = null
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
        @Part("Address") address: RequestBody,
        @Part           photos: List<MultipartBody.Part> = emptyList()
    ): Int

    @Multipart
    @PATCH("api/v/1/locations/{id}")
    suspend fun updateLocation(
        @Path("id")      id: Int,
        @Part("Title")   title: RequestBody? = null,
        @Part("Address") address: RequestBody? = null,
        @Part           photos: List<MultipartBody.Part> = emptyList()
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
        @Part("Capacity")   capacity: RequestBody? = null,
        @Part              photos: List<MultipartBody.Part> = emptyList()
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
    @PATCH("api/v/1/users/{id}")
    suspend fun updateUser(
        @Path("id")         id: String,
        @Part("FirstName")  firstName: RequestBody? = null,
        @Part("LastName")   lastName: RequestBody? = null,
        @Part("Patronymic") patronymic: RequestBody? = null,
        @Part("RoleId")     roleId: RequestBody? = null,
        @Part              avatar: MultipartBody.Part? = null
    )

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