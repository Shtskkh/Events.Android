package com.events.app.domain.models.users

import kotlinx.serialization.Serializable

enum class UserRole { ADMIN, USER, GUEST }

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.GUEST,
    val accessToken: String = ""
)