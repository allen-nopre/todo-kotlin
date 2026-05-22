package com.fullstackcert.todo.data.mapper

import com.fullstackcert.todo.data.remote.dto.TodoDto
import com.fullstackcert.todo.data.remote.dto.UserDto
import com.fullstackcert.todo.domain.model.*

fun UserDto.toDomain() = User(id = id, username = username)

fun TodoDto.toDomain() = Todo(
    id = id,
    title = title,
    details = details,
    dueDate = dueDate,
    completedDate = completedDate,
    priority = when (priority) {
        "high" -> Priority.HIGH
        "critical" -> Priority.CRITICAL
        else -> Priority.LOW
    },
    status = when (status) {
        "in_progress" -> TodoStatus.IN_PROGRESS
        "completed" -> TodoStatus.COMPLETED
        "cancelled" -> TodoStatus.CANCELLED
        else -> TodoStatus.NOT_STARTED
    },
    createdAt = createdAt,
    updatedAt = updatedAt
)
