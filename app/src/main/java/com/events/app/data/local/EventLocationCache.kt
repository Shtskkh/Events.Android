package com.events.app.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventLocationCache @Inject constructor() {

    private val cache = mutableMapOf<String, String>()

    fun put(eventId: String, locationTitle: String) {
        cache[eventId] = locationTitle
    }

    fun get(eventId: String): String? {
        return cache[eventId]
    }

    fun remove(eventId: String) {
        cache.remove(eventId)
    }

    fun clear() {
        cache.clear()
    }
}