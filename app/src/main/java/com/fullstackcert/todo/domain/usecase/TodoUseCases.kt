package com.fullstackcert.todo.domain.usecase

import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.repository.TodoRepository
import com.fullstackcert.todo.utils.Resource
import javax.inject.Inject

class GetTodosUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(): Resource<List<Todo>> = repo.getTodos()
}

class GetTodoByIdUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(id: Int): Resource<Todo> = repo.getTodoById(id)
}

class CreateTodoUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(
        title: String, details: String?, dueDate: String, priority: String, status: String
    ): Resource<Todo> = repo.createTodo(title, details, dueDate, priority, status)
}

class UpdateTodoUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(
        id: Int, details: String?, dueDate: String?, completedDate: String?, status: String?
    ): Resource<Todo> = repo.updateTodo(id, details, dueDate, completedDate, status)
}

class DeleteTodoUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(id: Int): Resource<Unit> = repo.deleteTodo(id)
}

class BulkDeleteTodosUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(ids: List<Int>): Resource<Unit> = repo.bulkDelete(ids)
}
