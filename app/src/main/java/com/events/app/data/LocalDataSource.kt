package com.events.app.data

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime

class LocalDataSource {
    fun getEvents(): List<Event> {
        return listOf(
            Event(
                1,
                "Ближайшее Мероприятие 1",
                "Описание ближайшего мероприятия, приму any lorem ipsum dolor sit amet.",
                LocalDateTime.of(2025, 12, 12, 12, 0),
                false
            ),
            Event(
                2,
                "Ближайшее Мероприятие 2",
                "Ещё одно описание для ближайшего, consectetur adipiscing elit.",
                LocalDateTime.of(2025, 12, 15, 14, 0),
                false
            ),
            Event(
                3,
                "Ближайшее Мероприятие 3",
                "Описание с датой, sed do eiusmod tempor.",
                LocalDateTime.of(2025, 12, 18, 10, 0),
                false
            ),
            Event(
                4,
                "Завершённое Мероприятие 1",
                "Описание завершённого, incididunt ut labore et dolore.",
                LocalDateTime.of(2025, 11, 1, 9, 0),
                true
            ),
            Event(
                5,
                "Завершённое Мероприятие 2",
                "Ещё завершённое, magna aliqua.",
                LocalDateTime.of(2025, 10, 25, 16, 0),
                true
            )
        )
    }
}