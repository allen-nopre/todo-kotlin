package com.fullstackcert.todo.domain.repository

import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.utils.Resource

interface TodoRepository {
    suspend fun getTodos(): Resource<List<Todo>>
    suspend fun getTodoById(id: Int): Resource<Todo>
    suspend fun createTodo(
        title: String,
        details: String?,
        dueDate: String,
        priority: String,
        status: String
    ): Resource<Todo>
    suspend fun updateTodo(
        id: Int,
        details: String?,
        dueDate: String?,
        completedDate: String?,
        status: String?
    ): Resource<Todo>
    suspend fun deleteTodo(id: Int): Resource<Unit>
    suspend fun bulkDelete(ids: List<Int>): Resource<Unit>
}
