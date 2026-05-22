package com.fullstackcert.todo.domain.repository

import com.fullstackcert.todo.domain.model.AuthResult
import com.fullstackcert.todo.domain.model.User
import com.fullstackcert.todo.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(username: String, password: String): Resource<User>
    suspend fun login(username: String, password: String, rememberMe: Boolean = false): Resource<AuthResult>
    suspend fun getSavedCredentials(): Pair<String, Boolean>
    suspend fun logout(): Resource<Unit>
    fun getToken(): Flow<String?>
    suspend fun isSessionValid(): Boolean
}
