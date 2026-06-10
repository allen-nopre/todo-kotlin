package com.fullstackcert.todo.data.repository

import com.fullstackcert.todo.data.mapper.toDomain
import com.fullstackcert.todo.data.remote.api.TodoApiService
import com.fullstackcert.todo.data.remote.dto.*
import com.fullstackcert.todo.domain.model.Attachment
import com.fullstackcert.todo.domain.model.Subtask
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.repository.TodoRepository
import com.fullstackcert.todo.utils.Resource
import com.fullstackcert.todo.utils.safeApiCall
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val api: TodoApiService
) : TodoRepository {

    override suspend fun getTodos(): Resource<List<Todo>> = safeApiCall {
        val r = api.getTodos()
        if (r.isSuccessful) Resource.Success(r.body()!!.data.map { it.toDomain() })
        else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun getTodoById(id: Int): Resource<Todo> = safeApiCall {
        val r = api.getTodoById(id)
        if (r.isSuccessful) Resource.Success(r.body()!!.data.toDomain())
        else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun createTodo(
        title: String, details: String?, dueDate: String,
        priority: String, status: String, subtasks: List<Subtask>,
        completedDate: String?
    ): Resource<Todo> = safeApiCall {
        val dto = CreateTodoRequestDto(
            title = title, details = details, dueDate = dueDate,
            priority = priority, status = status,
            subtasks = subtasks.map { SubtaskDto(id = it.id, title = it.title, isDone = it.isDone) },
            completedDate = completedDate?.ifBlank { null }
        )
        val r = api.createTodo(dto)
        if (r.isSuccessful) Resource.Success(r.body()!!.data.toDomain())
        else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun updateTodo(
        id: Int, title: String?, details: String?, dueDate: String?,
        completedDate: String?, status: String?, priority: String?, subtasks: List<Subtask>?
    ): Resource<Todo> = safeApiCall {
        val dto = UpdateTodoRequestDto(
            title = title,
            details = details, dueDate = dueDate,
            completedDate = completedDate, status = status, priority = priority,
            subtasks = subtasks?.map { SubtaskDto(id = it.id, title = it.title, isDone = it.isDone) }
        )
        val r = api.updateTodo(id, dto)
        if (r.isSuccessful) Resource.Success(r.body()!!.data.toDomain())
        else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun deleteTodo(id: Int): Resource<Unit> = safeApiCall {
        val r = api.deleteTodo(id)
        if (r.isSuccessful) Resource.Success(Unit) else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun bulkDelete(ids: List<Int>): Resource<Unit> = safeApiCall {
        val r = api.bulkDelete(BulkDeleteRequestDto(ids))
        if (r.isSuccessful) Resource.Success(Unit) else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun uploadAttachment(todoId: Int, file: File, mimeType: String): Resource<Attachment> = safeApiCall {
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val r = api.uploadAttachment(todoId, part)
        if (r.isSuccessful) Resource.Success(r.body()!!.data.toDomain())
        else Resource.Error(parseError(r.errorBody()?.string()))
    }

    override suspend fun deleteAttachment(todoId: Int, attachmentId: Int): Resource<Unit> = safeApiCall {
        val r = api.deleteAttachment(todoId, attachmentId)
        if (r.isSuccessful) Resource.Success(Unit) else Resource.Error(parseError(r.errorBody()?.string()))
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Unknown error"
        return try {
            val error = Gson().fromJson(errorBody, ErrorResponseDto::class.java)
            error.errors?.values?.flatten()?.joinToString("\n") ?: error.message
        } catch (e: Exception) { "An error occurred" }
    }
}
