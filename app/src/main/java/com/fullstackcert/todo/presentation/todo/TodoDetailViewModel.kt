package com.fullstackcert.todo.presentation.todo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.usecase.DeleteTodoUseCase
import com.fullstackcert.todo.domain.usecase.GetTodoByIdUseCase
import com.fullstackcert.todo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodoDetailState(
    val todo: Todo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TodoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTodoByIdUseCase: GetTodoByIdUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase
) : ViewModel() {

    private val todoId: Int = savedStateHandle["todoId"] ?: -1
    private val _state = MutableStateFlow(TodoDetailState())
    val state = _state.asStateFlow()

    init { loadTodo() }

    fun loadTodo() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getTodoByIdUseCase(todoId)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, todo = result.data) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun deleteTodo() {
        viewModelScope.launch {
            when (deleteTodoUseCase(todoId)) {
                is Resource.Success -> _state.update { it.copy(isDeleted = true) }
                is Resource.Error -> _state.update { it.copy(error = "Failed to delete") }
            }
        }
    }
}
