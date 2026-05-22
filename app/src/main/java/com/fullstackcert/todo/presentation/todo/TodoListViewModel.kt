package com.fullstackcert.todo.presentation.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fullstackcert.todo.domain.model.Priority
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.model.TodoStatus
import com.fullstackcert.todo.domain.usecase.BulkDeleteTodosUseCase
import com.fullstackcert.todo.domain.usecase.DeleteTodoUseCase
import com.fullstackcert.todo.domain.usecase.GetTodosUseCase
import com.fullstackcert.todo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortBy { DUE_DATE, PRIORITY, STATUS }

data class TodoListState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortBy: SortBy = SortBy.DUE_DATE,
    val filterPriority: Priority? = null,
    val filterStatus: TodoStatus? = null,
    val selectedIds: Set<Int> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val getTodosUseCase: GetTodosUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val bulkDeleteTodosUseCase: BulkDeleteTodosUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodoListState())
    val state = _state.asStateFlow()

    private var allTodos: List<Todo> = emptyList()

    init { loadTodos() }

    fun loadTodos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getTodosUseCase()) {
                is Resource.Success -> {
                    allTodos = result.data
                    _state.update { it.copy(isLoading = false) }
                    applyFiltersAndSort()
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun setSortBy(sort: SortBy) {
        _state.update { it.copy(sortBy = sort) }
        applyFiltersAndSort()
    }

    fun setFilterPriority(priority: Priority?) {
        _state.update { it.copy(filterPriority = priority) }
        applyFiltersAndSort()
    }

    fun setFilterStatus(status: TodoStatus?) {
        _state.update { it.copy(filterStatus = status) }
        applyFiltersAndSort()
    }

    fun toggleSelection(id: Int) {
        _state.update { state ->
            val newSelected = state.selectedIds.toMutableSet().apply {
                if (contains(id)) remove(id) else add(id)
            }
            state.copy(selectedIds = newSelected, isSelectionMode = newSelected.isNotEmpty())
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _state.value.selectedIds.toList()
            when (bulkDeleteTodosUseCase(ids)) {
                is Resource.Success -> {
                    clearSelection()
                    loadTodos()
                }
                is Resource.Error -> _state.update { it.copy(error = "Failed to delete items") }
            }
        }
    }

    private fun applyFiltersAndSort() {
        val state = _state.value
        var filtered = allTodos

        state.filterPriority?.let { p -> filtered = filtered.filter { it.priority == p } }
        state.filterStatus?.let { s -> filtered = filtered.filter { it.status == s } }

        val sorted = when (state.sortBy) {
            SortBy.DUE_DATE -> filtered.sortedBy { it.dueDate }
            SortBy.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
            SortBy.STATUS -> filtered.sortedBy { it.status.ordinal }
        }

        _state.update { it.copy(todos = sorted) }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}
