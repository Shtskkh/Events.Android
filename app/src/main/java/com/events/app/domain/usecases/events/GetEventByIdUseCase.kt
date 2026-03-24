package com.events.app.domain.usecases.events

import com.events.app.domain.models.events.Event
import com.events.app.domain.repositories.events.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) {
    /**
     * [accessToken] — передаётся в заголовок Authorization чтобы бэкенд
     * записал просмотр мероприятия на конкретного пользователя.
     */
    suspend operator fun invoke(id: String, accessToken: String? = null): Event {
        return repository.getEventById(id, accessToken)
    }
}