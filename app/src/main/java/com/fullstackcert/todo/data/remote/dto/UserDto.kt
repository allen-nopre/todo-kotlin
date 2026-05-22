package com.fullstackcert.todo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val username: String,
    @SerializedName("created_at") val createdAt: String?
)
