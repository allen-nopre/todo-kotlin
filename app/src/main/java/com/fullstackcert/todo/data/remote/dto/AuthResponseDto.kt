package com.fullstackcert.todo.data.remote.dto

data class AuthResponseDto(
    val message: String,
    val user: UserDto,
    val token: String?
)
