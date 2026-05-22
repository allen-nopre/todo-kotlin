package com.fullstackcert.todo.domain.model

data class Todo(
    val id: Int,
    val title: String,
    val details: String?,
    val dueDate: String,
    val completedDate: String?,
    val priority: Priority,
    val status: TodoStatus,
    val createdAt: String,
    val updatedAt: String
)
