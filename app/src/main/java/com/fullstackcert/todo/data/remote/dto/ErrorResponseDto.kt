package com.fullstackcert.todo.data.remote.dto

data class ErrorResponseDto(
    val message: String,
    val errors: Map<String, List<String>>?
)
