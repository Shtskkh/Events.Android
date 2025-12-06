package com.events.app.data

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime

class LocalDataSource {
    fun getEvents(): List<Event> {
        return listOf(
            Event(
                1,
                "Ближайшая Конференция 1",
                "Описание конференции, lorem ipsum dolor sit amet.",
                LocalDateTime.of(2025, 12, 12, 12, 0),
                false
            ),
            Event(
                2,
                "Встреча Команды 2",
                "Ещё описание встречи, consectetur adipiscing elit.",
                LocalDateTime.of(2025, 12, 15, 14, 0),
                false
            ),
            Event(
                3,
                "Семинар по Теме 3",
                "Описание семинара, sed do eiusmod tempor.",
                LocalDateTime.of(2025, 12, 18, 10, 0),
                false
            ),
            Event(
                4,
                "Завершённый Воркшоп 1",
                "Описание воркшопа, incididunt ut labore et dolore.",
                LocalDateTime.of(2025, 11, 1, 9, 0),
                true
            ),
            Event(
                5,
                "Прошлый Митап 2",
                "Ещё завершённый митап, magna aliqua.",
                LocalDateTime.of(2025, 10, 25, 16, 0),
                true
            ),
            Event(
                6,
                "Онлайн Вебинар 4",
                "Описание вебинара, ut enim ad minim veniam.",
                LocalDateTime.of(2025, 12, 20, 15, 30),
                false
            ),
            Event(
                7,
                "Корпоративный Тимбилдинг",
                "Описание тимбилдинга, quis nostrud exercitation ullamco.",
                LocalDateTime.of(2025, 11, 10, 11, 0),
                true
            ),
            Event(
                8,
                "Лекция по Инновациям",
                "Описание лекции, laboris nisi ut aliquip ex ea.",
                LocalDateTime.of(2025, 12, 5, 18, 0),
                false
            ),
            Event(
                9,
                "Фестиваль Идей 5",
                "Описание фестиваля, commodo consequat.",
                LocalDateTime.of(2025, 10, 15, 13, 0),
                true
            ),
            Event(
                10,
                "Брейнсторм Сессия",
                "Описание сессии, duis aute irure dolor in reprehenderit.",
                LocalDateTime.of(2025, 12, 22, 10, 0),
                false
            ),
            Event(
                11,
                "Прошлый Хакатон 3",
                "Описание хакатона, in voluptate velit esse cillum.",
                LocalDateTime.of(2025, 9, 30, 9, 0),
                true
            ),
            Event(
                12,
                "Мастер-Класс 6",
                "Описание мастер-класса, dolore eu fugiat nulla pariatur.",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                false
            ),
            Event(
                13,
                "Конференция Разработчиков",
                "Описание конференции, excepteur sint occaecat cupidatat non proident.",
                LocalDateTime.of(2025, 11, 20, 16, 0),
                true
            ),
            Event(
                14,
                "Воркшоп по Дизайну",
                "Описание воркшопа, sunt in culpa qui officia deserunt.",
                LocalDateTime.of(2025, 12, 10, 11, 30),
                false
            ),
            Event(
                15,
                "Митап Стартапов 7",
                "Описание митапа, mollit anim id est laborum.",
                LocalDateTime.of(2025, 10, 5, 17, 0),
                true
            ),
            Event(
                16,
                "Семинар по Маркетингу",
                "Описание семинара, sed ut perspiciatis unde omnis iste.",
                LocalDateTime.of(2025, 12, 28, 13, 0),
                false
            ),
            Event(
                17,
                "Прошлая Выставка 4",
                "Описание выставки, natus error sit voluptatem accusantium.",
                LocalDateTime.of(2025, 11, 15, 10, 0),
                true
            ),
            Event(
                18,
                "Вебинар по Технологиям",
                "Описание вебинара, doloremque laudantium, totam rem aperiam.",
                LocalDateTime.of(2025, 12, 30, 15, 0),
                false
            ),
            Event(
                19,
                "Тимбилдинг Команды 8",
                "Описание тимбилдинга, ipsam voluptatem quia voluptas sit aspernatur.",
                LocalDateTime.of(2025, 9, 20, 12, 0),
                true
            ),
            Event(
                20,
                "Лекция по Бизнесу",
                "Описание лекции, aut odit aut fugit, sed quia consequuntur.",
                LocalDateTime.of(2025, 12, 8, 19, 0),
                false
            ),
            Event(
                21,
                "Фестиваль Инноваций 9",
                "Описание фестиваля, magni dolores eos qui ratione voluptatem sequi.",
                LocalDateTime.of(2025, 10, 10, 14, 0),
                true
            ),
            Event(
                22,
                "Брейнсторм по Проектам",
                "Описание брейнсторма, nesciunt. Neque porro quisquam est, qui dolorem.",
                LocalDateTime.of(2025, 12, 17, 11, 0),
                false
            ),
            Event(
                23,
                "Хакатон Разработчиков 10",
                "Описание хакатона, ipsum quia dolor sit amet, consectetur.",
                LocalDateTime.of(2025, 11, 5, 9, 30),
                true
            ),
            Event(
                24,
                "Мастер-Класс по Кодингу",
                "Описание мастер-класса, adipisci velit, sed quia non numquam eius.",
                LocalDateTime.of(2025, 12, 3, 16, 0),
                false
            ),
            Event(
                25,
                "Выставка Технологий 11",
                "Описание выставки, modi tempora incidunt ut labore et dolore.",
                LocalDateTime.of(2025, 10, 1, 18, 0),
                true
            )
        )
    }
}