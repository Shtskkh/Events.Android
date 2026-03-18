package com.events.app.data.remote

import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.EventFormatDto
import com.events.app.data.remote.dto.EventTypeDto
import com.events.app.data.remote.dto.LocationDto
import com.events.app.data.remote.dto.ShortEventDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface EventsApi {

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
    suspend fun getEventById(
        @Path("id") id: String
    ): EventDetailDto

    @GET("api/v/1/events/placeholders")
    suspend fun getPlaceholders(): List<String>

    @GET("api/v/1/events/types")
    suspend fun getEventTypes(): List<EventTypeDto>

    @GET("api/v/1/events/formats")
    suspend fun getEventFormats(): List<EventFormatDto>

    @GET("api/v/1/locations")
    suspend fun getLocations(): List<LocationDto>

    @Multipart
    @POST("api/v/1/locations")
    suspend fun createLocation(
        @Part("Title") title: RequestBody,
        @Part("Address") address: RequestBody
    ): Int  // возвращает id созданной локации

    @Multipart
    @POST("api/v/1/events")
    suspend fun createEvent(
        @Part("Title") title: RequestBody,
        @Part("Announcement") announcement: RequestBody,
        @Part("Description") description: RequestBody,
        @Part("StartDateTime") startDateTime: RequestBody,
        @Part("EndDateTime") endDateTime: RequestBody,
        @Part("EventTypeId") eventTypeId: RequestBody,
        @Part("EventFormatId") eventFormatId: RequestBody,
        @Part("NeedsRegistration") needsRegistration: RequestBody,
        @Part("Placeholder") placeholder: RequestBody? = null,
        @Part preview: MultipartBody.Part? = null
    ): String
}