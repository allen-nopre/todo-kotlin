package com.fullstackcert.todo.domain.repository

import com.fullstackcert.todo.domain.model.Attachment
import com.fullstackcert.todo.domain.model.Subtask
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.utils.Resource
import java.io.File

interface TodoRepository {
    suspend fun getTodos(): Resource<List<Todo>>
    suspend fun getTodoById(id: Int): Resource<Todo>
    suspend fun createTodo(
        title: String, details: String?, dueDate: String,
        priority: String, status: String, subtasks: List<Subtask>,
        completedDate: String? = null
    ): Resource<Todo>
    suspend fun updateTodo(
        id: Int, title: String?, details: String?, dueDate: String?,
        completedDate: String?, status: String?, priority: String?, subtasks: List<Subtask>?
    ): Resource<Todo>
    suspend fun deleteTodo(id: Int): Resource<Unit>
    suspend fun bulkDelete(ids: List<Int>): Resource<Unit>
    suspend fun uploadAttachment(todoId: Int, file: File, mimeType: String): Resource<Attachment>
    suspend fun deleteAttachment(todoId: Int, attachmentId: Int): Resource<Unit>
}
