package com.events.app.domain.usecases.events

import com.events.app.domain.models.events.Event
import com.events.app.domain.repositories.events.EventRepository
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(
        size: Int = 20,
        page: Int = 1,
        text: String? = null,
        startDateTime: String? = null,
        endDateTime: String? = null,
        typeId: Int? = null,
        formatId: Int? = null,
        placeId: Int? = null,
        userId: String? = null,
        createdAfter: String? = null,
        createdBefore: String? = null
    ): List<Event> {
        return repository.getEvents(
            size          = size,
            page          = page,
            text          = text,
            startDateTime = startDateTime,
            endDateTime   = endDateTime,
            typeId        = typeId,
            formatId      = formatId,
            placeId       = placeId,
            userId        = userId,
            createdAfter  = createdAfter,
            createdBefore = createdBefore
        )
    }
}