package com.fullstackcert.todo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateTodoRequestDto(
    val title: String?,
    val details: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("completed_date") val completedDate: String?,
    val status: String?,
    val priority: String? = null,
    val subtasks: List<SubtaskDto>? = null
)
