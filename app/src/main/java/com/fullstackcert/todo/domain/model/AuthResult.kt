package com.fullstackcert.todo.domain.model

data class AuthResult(
    val user: User,
    val token: String
)
