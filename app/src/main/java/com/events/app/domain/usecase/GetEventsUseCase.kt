package com.events.app.domain.usecase

import com.events.app.domain.model.Event
import com.events.app.domain.repository.EventRepository
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): List<Event> {
        return repository.getEvents()
    }
}
//Вызывает репозиторий и возвращает данные