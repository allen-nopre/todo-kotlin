package com.fullstackcert.todo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateTodoRequestDto(
    val title: String,
    val details: String?,
    @SerializedName("due_date") val dueDate: String,
    val priority: String,
    val status: String
)
