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

enum class SortBy { NONE, DUE_DATE, PRIORITY, STATUS }
enum class SortOrder { ASCENDING, DESCENDING }

data class TodoListState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortBy: SortBy = SortBy.NONE,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val filterPriority: Priority? = null,
    val filterStatus: TodoStatus? = null,
    val selectedIds: Set<Int> = emptySet(),
    val isSelectionMode: Boolean = false,
    val toast: String? = null
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
                    _state.update { applyFiltersAndSort(it.copy(isLoading = false)) }
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun setSortBy(sort: SortBy) {
        _state.update { applyFiltersAndSort(it.copy(sortBy = sort)) }
    }

    fun setSortOrder(order: SortOrder) {
        _state.update { applyFiltersAndSort(it.copy(sortOrder = order)) }
    }

    fun setSortByAndOrder(sort: SortBy, order: SortOrder) {
        _state.update { applyFiltersAndSort(it.copy(sortBy = sort, sortOrder = order)) }
    }

    fun setFilterPriority(priority: Priority?) {
        _state.update { applyFiltersAndSort(it.copy(filterPriority = priority)) }
    }

    fun setFilterStatus(status: TodoStatus?) {
        _state.update { applyFiltersAndSort(it.copy(filterStatus = status)) }
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
            val count = ids.size
            when (bulkDeleteTodosUseCase(ids)) {
                is Resource.Success -> {
                    clearSelection()
                    loadTodos()
                    _state.update { it.copy(toast = "$count task${if (count != 1) "s" else ""} deleted") }
                }
                is Resource.Error -> _state.update { it.copy(error = "Failed to delete items") }
            }
        }
    }

    private fun applyFiltersAndSort(state: TodoListState): TodoListState {
        var filtered = allTodos

        state.filterPriority?.let { p -> filtered = filtered.filter { it.priority == p } }
        state.filterStatus?.let { s -> filtered = filtered.filter { it.status == s } }

        val sorted = when (state.sortBy) {
            SortBy.NONE -> filtered
            SortBy.DUE_DATE -> if (state.sortOrder == SortOrder.ASCENDING) filtered.sortedBy { it.dueDate } else filtered.sortedByDescending { it.dueDate }
            SortBy.PRIORITY -> if (state.sortOrder == SortOrder.ASCENDING) filtered.sortedBy { it.priority.ordinal } else filtered.sortedByDescending { it.priority.ordinal }
            SortBy.STATUS -> if (state.sortOrder == SortOrder.ASCENDING) filtered.sortedBy { it.status.ordinal } else filtered.sortedByDescending { it.status.ordinal }
        }

        return state.copy(todos = sorted)
    }

    fun clearError() { _state.update { it.copy(error = null) } }
    fun clearToast() { _state.update { it.copy(toast = null) } }
}
