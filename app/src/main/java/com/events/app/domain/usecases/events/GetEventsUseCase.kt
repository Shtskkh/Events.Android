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
        formatId: Int? = null
    ): List<Event> {
        return repository.getEvents(size, page, text, startDateTime, endDateTime, typeId, formatId)
    }
}