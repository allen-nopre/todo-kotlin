package com.fullstackcert.todo.domain.usecase

import com.fullstackcert.todo.domain.model.Attachment
import com.fullstackcert.todo.domain.model.Subtask
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.repository.TodoRepository
import com.fullstackcert.todo.utils.Resource
import java.io.File
import javax.inject.Inject

class GetTodosUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(): Resource<List<Todo>> = repo.getTodos()
}

class GetTodoByIdUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(id: Int): Resource<Todo> = repo.getTodoById(id)
}

class CreateTodoUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(
        title: String, details: String?, dueDate: String,
        priority: String, status: String, subtasks: List<Subtask>,
        completedDate: String = ""
    ): Resource<Todo> = repo.createTodo(title, details, dueDate, priority, status, subtasks, completedDate)
}

class UpdateTodoUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(
        id: Int, details: String?, dueDate: String?,
        completedDate: String?, status: String?, subtasks: List<Subtask>?
    ): Resource<Todo> = repo.updateTodo(id, details, dueDate, completedDate, status, subtasks)
}

class DeleteTodoUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(id: Int): Resource<Unit> = repo.deleteTodo(id)
}

class BulkDeleteTodosUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(ids: List<Int>): Resource<Unit> = repo.bulkDelete(ids)
}

class UploadAttachmentUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(todoId: Int, file: File, mimeType: String): Resource<Attachment> =
        repo.uploadAttachment(todoId, file, mimeType)
}

class DeleteAttachmentUseCase @Inject constructor(private val repo: TodoRepository) {
    suspend operator fun invoke(todoId: Int, attachmentId: Int): Resource<Unit> =
        repo.deleteAttachment(todoId, attachmentId)
}
