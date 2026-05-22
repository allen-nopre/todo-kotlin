package com.fullstackcert.todo.data.repository

import com.fullstackcert.todo.data.mapper.toDomain
import com.fullstackcert.todo.data.remote.api.TodoApiService
import com.fullstackcert.todo.data.remote.dto.*
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.repository.TodoRepository
import com.fullstackcert.todo.utils.Resource
import com.fullstackcert.todo.utils.safeApiCall
import com.google.gson.Gson
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val api: TodoApiService
) : TodoRepository {

    override suspend fun getTodos(): Resource<List<Todo>> = safeApiCall {
        val response = api.getTodos()
        if (response.isSuccessful) {
            Resource.Success(response.body()!!.data.map { it.toDomain() })
        } else {
            Resource.Error(parseError(response.errorBody()?.string()))
        }
    }

    override suspend fun getTodoById(id: Int): Resource<Todo> = safeApiCall {
        val response = api.getTodoById(id)
        if (response.isSuccessful) {
            Resource.Success(response.body()!!.data.toDomain())
        } else {
            Resource.Error(parseError(response.errorBody()?.string()))
        }
    }

    override suspend fun createTodo(
        title: String, details: String?, dueDate: String, priority: String, status: String
    ): Resource<Todo> = safeApiCall {
        val response = api.createTodo(CreateTodoRequestDto(title, details, dueDate, priority, status))
        if (response.isSuccessful) {
            Resource.Success(response.body()!!.data.toDomain())
        } else {
            Resource.Error(parseError(response.errorBody()?.string()))
        }
    }

    override suspend fun updateTodo(
        id: Int, details: String?, dueDate: String?, completedDate: String?, status: String?
    ): Resource<Todo> = safeApiCall {
        val response = api.updateTodo(id, UpdateTodoRequestDto(details, dueDate, completedDate, status))
        if (response.isSuccessful) {
            Resource.Success(response.body()!!.data.toDomain())
        } else {
            Resource.Error(parseError(response.errorBody()?.string()))
        }
    }

    override suspend fun deleteTodo(id: Int): Resource<Unit> = safeApiCall {
        val response = api.deleteTodo(id)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(parseError(response.errorBody()?.string()))
    }

    override suspend fun bulkDelete(ids: List<Int>): Resource<Unit> = safeApiCall {
        val response = api.bulkDelete(BulkDeleteRequestDto(ids))
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(parseError(response.errorBody()?.string()))
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Unknown error"
        return try {
            val error = Gson().fromJson(errorBody, ErrorResponseDto::class.java)
            error.errors?.values?.flatten()?.joinToString("\n") ?: error.message
        } catch (e: Exception) {
            "An error occurred"
        }
    }
}
