package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDto(
    val id: String,
    val lastName: String? = null,
    val firstName: String? = null,
    val patronymic: String? = null,
    val registrationTime: String? = null
) {
    val displayName: String get() {
        val parts = listOfNotNull(
            lastName?.trim(),
            firstName?.trim(),
            patronymic?.trim()
        ).filter { it.isNotEmpty() }
        return if (parts.isEmpty()) "Пользователь" else parts.joinToString(" ")
    }

    val initials: String get() {
        val l = lastName?.trim()?.firstOrNull()?.uppercaseChar()
        val f = firstName?.trim()?.firstOrNull()?.uppercaseChar()
        return when {
            l != null && f != null -> "$l$f"
            l != null -> "$l"
            f != null -> "$f"
            else -> "?"
        }
    }
}