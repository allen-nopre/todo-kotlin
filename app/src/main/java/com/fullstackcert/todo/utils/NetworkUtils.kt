package com.fullstackcert.todo.utils

import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> Resource<T>): Resource<T> {
    return try {
        call()
    } catch (e: IOException) {
        Resource.Error("Network error. Please check your connection.")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "An unexpected error occurred.")
    }
}
