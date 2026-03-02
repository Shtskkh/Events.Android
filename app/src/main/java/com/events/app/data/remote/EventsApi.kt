package com.events.app.data.remote

import com.events.app.data.remote.dto.EventDetailDto
import com.events.app.data.remote.dto.ShortEventDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventsApi {

    @GET("api/v/1/events")
    suspend fun getEvents(
        @Query("Size") size: Int = 20,
        @Query("Page") page: Int = 1,
        @Query("Text") text: String? = null
    ): List<ShortEventDto>   // если бэкенд возвращает список напрямую

    @GET("api/v/1/events/{id}")
    suspend fun getEventById(
        @Path("id") id: String    // UUID — строка
    ): EventDetailDto
}