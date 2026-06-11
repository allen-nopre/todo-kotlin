package com.fullstackcert.todo.presentation.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fullstackcert.todo.domain.model.Attachment
import com.fullstackcert.todo.domain.model.Subtask
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.usecase.CreateTodoUseCase
import com.fullstackcert.todo.domain.usecase.DeleteAttachmentUseCase
import com.fullstackcert.todo.domain.usecase.GetTodoByIdUseCase
import com.fullstackcert.todo.domain.usecase.UpdateTodoUseCase
import com.fullstackcert.todo.domain.usecase.UploadAttachmentUseCase
import com.fullstackcert.todo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import javax.inject.Inject

data class SubtaskInput(val title: String = "", val isDone: Boolean = false, val id: Int? = null)
data class PendingAttachment(val file: File, val mimeType: String, val uri: android.net.Uri)

data class AddEditState(
    val title: String = "",
    val details: String = "",
    val dueDate: String = "",
    val priority: String = "low",
    val status: String = "not_started",
    val completedDate: String? = null,
    val createdDate: String = Instant.now().toString(),
    val subtasks: List<SubtaskInput> = listOf(SubtaskInput(), SubtaskInput(), SubtaskInput()),
    val attachments: List<Attachment> = emptyList(),
    val pendingAttachments: List<PendingAttachment> = emptyList(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val toast: String? = null,
    val error: String? = null,
    val existingTodo: Todo? = null
)

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    private val createTodoUseCase: CreateTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTodoByIdUseCase: GetTodoByIdUseCase,
    private val uploadAttachmentUseCase: UploadAttachmentUseCase,
    private val deleteAttachmentUseCase: DeleteAttachmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditState())
    val state = _state.asStateFlow()

    fun loadTodo(todoId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getTodoByIdUseCase(todoId)) {
                is Resource.Success -> {
                    val todo = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isEditing = true,
                            title = todo.title,
                            details = todo.details ?: "",
                            dueDate = todo.dueDate,
                            priority = todo.priority.name.lowercase(),
                            status = todo.status.name.lowercase(),
                            completedDate = todo.completedDate,
                            createdDate = todo.createdAt,
                            subtasks = todo.subtasks.map { s -> SubtaskInput(s.title, s.isDone, s.id) },
                            attachments = todo.attachments,
                            existingTodo = todo
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun updateTitle(value: String) { if (value.length <= 25) _state.update { it.copy(title = value) } }
    fun updateDetails(value: String) { if (value.length <= 300) _state.update { it.copy(details = value) } }
    fun updateDueDate(value: String) { _state.update { it.copy(dueDate = value) } }
    fun updatePriority(value: String) { _state.update { it.copy(priority = value) } }
    fun updateStatus(value: String) {
        _state.update {
            val completedDate = if (value == "completed") Instant.now().toString()
                                else if (it.status == "completed") null
                                else it.completedDate
            it.copy(status = value, completedDate = completedDate)
        }
    }
    fun updateCompletedDate(value: String?) { _state.update { it.copy(completedDate = value) } }

    fun addSubtask() { if (_state.value.subtasks.size < 10) _state.update { it.copy(subtasks = it.subtasks + SubtaskInput()) } }
    fun updateSubtaskTitle(index: Int, title: String) {
        _state.update {
            val list = it.subtasks.toMutableList()
            list[index] = list[index].copy(title = title)
            it.copy(subtasks = list)
        }
    }
    fun toggleSubtaskDone(index: Int) {
        _state.update {
            val list = it.subtasks.toMutableList()
            list[index] = list[index].copy(isDone = !list[index].isDone)
            val nonEmpty = list.filter { s -> s.title.isNotBlank() }
            val allDone = nonEmpty.isNotEmpty() && nonEmpty.all { s -> s.isDone }
            val newStatus = if (allDone) "completed" else if (it.status == "completed") "in_progress" else it.status
            val newCompletedDate = if (allDone) Instant.now().toString() else if (it.status == "completed") null else it.completedDate
            it.copy(subtasks = list, status = newStatus, completedDate = newCompletedDate)
        }
    }
    fun removeSubtask(index: Int) {
        _state.update { it.copy(subtasks = it.subtasks.toMutableList().also { l -> l.removeAt(index) }) }
    }

    fun uploadAttachment(file: File, mimeType: String, uri: android.net.Uri) {
        val todoId = _state.value.existingTodo?.id
        if (todoId == null) {
            _state.update { it.copy(pendingAttachments = it.pendingAttachments + PendingAttachment(file, mimeType, uri)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = uploadAttachmentUseCase(todoId, file, mimeType)) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, attachments = it.attachments + result.data)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun deleteAttachment(attachmentId: Int) {
        val todoId = _state.value.existingTodo?.id ?: return
        viewModelScope.launch {
            when (deleteAttachmentUseCase(todoId, attachmentId)) {
                is Resource.Success -> _state.update {
                    it.copy(attachments = it.attachments.filter { a -> a.id != attachmentId })
                }
                is Resource.Error -> _state.update { it.copy(error = "Failed to delete attachment") }
            }
        }
    }

    fun save() {
        val s = _state.value
        if (s.title.isBlank() || s.dueDate.isBlank()) {
            _state.update { it.copy(error = "Title and due date are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val subtasks = s.subtasks.filter { it.title.isNotBlank() }
                .map { Subtask(id = it.id, title = it.title, isDone = it.isDone) }
            val result = if (s.isEditing) {
                updateTodoUseCase(
                    id = s.existingTodo!!.id,
                    title = s.title,
                    details = s.details.ifBlank { null },
                    dueDate = s.dueDate,
                    completedDate = s.completedDate,
                    status = s.status,
                    priority = s.priority,
                    subtasks = subtasks
                )
            } else {
                createTodoUseCase(
                    title = s.title,
                    details = s.details.ifBlank { null },
                    dueDate = s.dueDate,
                    priority = s.priority,
                    status = s.status,
                    subtasks = subtasks,
                    completedDate = s.completedDate?.ifBlank { null }
                )
            }
            when (result) {
                is Resource.Success -> {
                    val savedTodoId = result.data.id
                    val pending = _state.value.pendingAttachments
                    val toastMsg = if (s.isEditing) "Todo updated" else "Todo created"
                    _state.update { it.copy(isLoading = false, isSaved = pending.isEmpty(), pendingAttachments = emptyList(), toast = toastMsg) }
                    if (pending.isNotEmpty()) {
                        pending.forEach { p ->
                            uploadAttachmentUseCase(savedTodoId, p.file, p.mimeType)
                        }
                        _state.update { it.copy(isSaved = true) }
                    }
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
    fun clearToast() { _state.update { it.copy(toast = null) } }
}
