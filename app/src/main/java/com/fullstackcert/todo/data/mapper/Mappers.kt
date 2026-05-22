package com.fullstackcert.todo.data.mapper

import com.fullstackcert.todo.data.remote.dto.AttachmentDto
import com.fullstackcert.todo.data.remote.dto.SubtaskDto
import com.fullstackcert.todo.data.remote.dto.TodoDto
import com.fullstackcert.todo.data.remote.dto.UserDto
import com.fullstackcert.todo.domain.model.*

fun UserDto.toDomain() = User(id = id, username = username)

fun SubtaskDto.toDomain() = Subtask(id = id, title = title, isDone = isDone)

fun AttachmentDto.toDomain() = Attachment(
    id = id, fileName = fileName, filePath = filePath,
    mimeType = mimeType, fileSize = fileSize, url = url
)

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
    updatedAt = updatedAt,
    subtasks = subtasks.map { it.toDomain() },
    attachments = attachments.map { it.toDomain() }
)
