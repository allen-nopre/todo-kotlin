package com.fullstackcert.todo.presentation.todo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fullstackcert.todo.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    todoId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditTodoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCompletedDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(todoId) { todoId?.let { viewModel.loadTodo(it) } }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.updateDueDate(Instant.ofEpochMilli(millis).toString())
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showCompletedDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCompletedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.updateCompletedDate(Instant.ofEpochMilli(millis).toString())
                    }
                    showCompletedDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showCompletedDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isEditing) R.string.edit_todo else R.string.new_todo)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text(stringResource(R.string.title_hint)) },
                enabled = !state.isEditing,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.details,
                onValueChange = { viewModel.updateDetails(it) },
                label = { Text(stringResource(R.string.details_hint)) },
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (state.dueDate.isNotBlank()) stringResource(R.string.due_date_selected, formatForDisplay(state.dueDate))
                    else stringResource(R.string.select_due_date)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (!state.isEditing) {
                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedPriority, onExpandedChange = { expandedPriority = it }) {
                    OutlinedTextField(
                        value = state.priority.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_priority)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedPriority, onDismissRequest = { expandedPriority = false }) {
                        listOf("low", "high", "critical").forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.uppercase()) },
                                onClick = { viewModel.updatePriority(p); expandedPriority = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            var expandedStatus by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = it }) {
                OutlinedTextField(
                    value = state.status.replace("_", " ").uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_status)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                    listOf("not_started", "in_progress", "completed", "cancelled").forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.replace("_", " ").uppercase()) },
                            onClick = { viewModel.updateStatus(s); expandedStatus = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (state.isEditing) {
                OutlinedButton(onClick = { showCompletedDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (state.completedDate != null) stringResource(R.string.completed_date_selected, formatForDisplay(state.completedDate!!))
                        else stringResource(R.string.set_completed_date)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.save() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text(stringResource(if (state.isEditing) R.string.update else R.string.create))
            }
        }
    }
}

private fun formatForDisplay(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) { isoDate }
}
