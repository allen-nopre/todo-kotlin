package com.fullstackcert.todo.domain.model

data class Attachment(
    val id: Int,
    val fileName: String,
    val filePath: String,
    val mimeType: String?,
    val fileSize: Long?,
    val url: String
)
