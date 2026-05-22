package com.fullstackcert.todo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubtaskDto(
    val id: Int?,
    val title: String,
    @SerializedName("is_done") val isDone: Boolean
)
