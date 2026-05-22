package com.fullstackcert.todo.domain.model

data class Subtask(
    val id: Int? = null,
    val title: String,
    val isDone: Boolean = false
)
