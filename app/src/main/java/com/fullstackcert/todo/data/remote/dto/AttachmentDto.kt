package com.fullstackcert.todo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AttachmentDto(
    val id: Int,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_path") val filePath: String,
    @SerializedName("mime_type") val mimeType: String?,
    @SerializedName("file_size") val fileSize: Long?,
    val url: String
)
