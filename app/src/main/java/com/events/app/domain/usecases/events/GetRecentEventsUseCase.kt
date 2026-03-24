package com.events.app.domain.usecases.events

import com.events.app.domain.models.events.Event
import com.events.app.domain.repositories.events.EventRepository
import javax.inject.Inject

class GetRecentEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(userId: String): List<Event> {
        return repository.getRecentEvents(userId)
    }
}