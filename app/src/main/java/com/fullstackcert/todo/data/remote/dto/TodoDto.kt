package com.fullstackcert.todo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TodoDto(
    val id: Int,
    val title: String,
    val details: String?,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("completed_date") val completedDate: String?,
    val priority: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
