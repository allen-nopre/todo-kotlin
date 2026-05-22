package com.fullstackcert.todo.presentation.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.usecase.CreateTodoUseCase
import com.fullstackcert.todo.domain.usecase.GetTodoByIdUseCase
import com.fullstackcert.todo.domain.usecase.UpdateTodoUseCase
import com.fullstackcert.todo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditState(
    val title: String = "",
    val details: String = "",
    val dueDate: String = "",
    val priority: String = "low",
    val status: String = "not_started",
    val completedDate: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val existingTodo: Todo? = null
)

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    private val createTodoUseCase: CreateTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTodoByIdUseCase: GetTodoByIdUseCase
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
                            isEditing = true,
                            title = todo.title,
                            details = todo.details ?: "",
                            dueDate = todo.dueDate,
                            priority = todo.priority.name.lowercase(),
                            status = todo.status.name.lowercase(),
                            completedDate = todo.completedDate,
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
    fun updateStatus(value: String) { _state.update { it.copy(status = value) } }
    fun updateCompletedDate(value: String?) { _state.update { it.copy(completedDate = value) } }

    fun save() {
        val s = _state.value
        if (s.title.isBlank() || s.dueDate.isBlank()) {
            _state.update { it.copy(error = "Title and due date are required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (s.isEditing) {
                updateTodoUseCase(
                    id = s.existingTodo!!.id,
                    details = s.details.ifBlank { null },
                    dueDate = s.dueDate,
                    completedDate = s.completedDate,
                    status = s.status
                )
            } else {
                createTodoUseCase(
                    title = s.title,
                    details = s.details.ifBlank { null },
                    dueDate = s.dueDate,
                    priority = s.priority,
                    status = s.status
                )
            }

            when (result) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, isSaved = true) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}
