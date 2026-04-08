package com.events.app.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальный кэш последних просмотренных мероприятий.
 * Хранит до MAX_SIZE ID в порядке от новейшего к старейшему.
 */
@Singleton
class RecentEventsCache @Inject constructor() {

    private val MAX_SIZE = 10
    private val ids = LinkedHashSet<String>()

    /** Записать просмотр — перемещает eventId в начало */
    fun recordView(eventId: String) {
        ids.remove(eventId)
        val newSet = LinkedHashSet<String>()
        newSet.add(eventId)
        newSet.addAll(ids)
        ids.clear()
        ids.addAll(newSet.take(MAX_SIZE))
    }

    /** Список ID от новейшего к старейшему */
    fun getRecentIds(): List<String> = ids.toList()

    fun clear() = ids.clear()
}