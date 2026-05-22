package com.fullstackcert.todo.data.repository

import com.fullstackcert.todo.data.local.SessionManager
import com.fullstackcert.todo.data.mapper.toDomain
import com.fullstackcert.todo.data.remote.api.TodoApiService
import com.fullstackcert.todo.data.remote.dto.LoginRequestDto
import com.fullstackcert.todo.data.remote.dto.RegisterRequestDto
import com.fullstackcert.todo.domain.model.AuthResult
import com.fullstackcert.todo.domain.model.User
import com.fullstackcert.todo.domain.repository.AuthRepository
import com.fullstackcert.todo.utils.Resource
import com.fullstackcert.todo.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: TodoApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun register(username: String, password: String): Resource<User> =
        safeApiCall {
            val response = api.register(RegisterRequestDto(username, password))
            if (response.isSuccessful) {
                Resource.Success(response.body()!!.user.toDomain())
            } else {
                Resource.Error(parseError(response.errorBody()?.string()))
            }
        }

    override suspend fun login(username: String, password: String, rememberMe: Boolean): Resource<AuthResult> =
        safeApiCall {
            val response = api.login(LoginRequestDto(username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                sessionManager.saveSession(body.token!!, rememberMe, username)
                Resource.Success(AuthResult(user = body.user.toDomain(), token = body.token))
            } else {
                Resource.Error(parseError(response.errorBody()?.string()))
            }
        }

    override suspend fun logout(): Resource<Unit> = safeApiCall {
        api.logout()
        sessionManager.clearSession()
        Resource.Success(Unit)
    }

    override suspend fun getSavedCredentials(): Pair<String, Boolean> =
        sessionManager.getSavedCredentials()

    override fun getToken(): Flow<String?> = sessionManager.token

    override suspend fun isSessionValid(): Boolean = sessionManager.isSessionValid()

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Unknown error"
        return try {
            val gson = com.google.gson.Gson()
            val error = gson.fromJson(errorBody, com.fullstackcert.todo.data.remote.dto.ErrorResponseDto::class.java)
            error.errors?.values?.flatten()?.joinToString("\n") ?: error.message
        } catch (e: Exception) {
            "An error occurred"
        }
    }
}
