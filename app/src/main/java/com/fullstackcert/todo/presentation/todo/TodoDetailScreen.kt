package com.fullstackcert.todo.presentation.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fullstackcert.todo.R
import com.fullstackcert.todo.domain.model.Priority
import com.fullstackcert.todo.domain.model.TodoStatus
import com.fullstackcert.todo.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    refreshTrigger: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: TodoDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadTodo()
            onRefreshConsumed()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_todo)) },
            text = { Text(stringResource(R.string.delete_todo_confirm)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteTodo() }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.view_task)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    state.todo?.let {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete))
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                BottomAppBar(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { showDeleteDialog = true },
                        icon = {
                            Icon(
                                painterResource(if (showDeleteDialog) R.drawable.delete_active else R.drawable.delete_inactive),
                                contentDescription = stringResource(R.string.delete)
                            )
                        },
                        label = null
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { state.todo?.let { onNavigateToEdit(it.id) } },
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.edit),
                                    contentDescription = stringResource(R.string.edit),
                                    tint = White,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        },
                        label = null
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {
                            Icon(
                                painterResource(R.drawable.avatar),
                                contentDescription = null,
                                tint = GraySecondary
                            )
                        },
                        label = null
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    state.error!!,
                    modifier = Modifier.align(Alignment.Center)
                )

                state.todo != null -> {
                    val todo = state.todo!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Priority chip + Status icon+text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val priorityColor = when (todo.priority) {
                                Priority.LOW -> PriorityGreen
                                Priority.HIGH -> PriorityYellow
                                Priority.CRITICAL -> PriorityRed
                            }
                            Surface(
                                color = priorityColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    priorityColor
                                )
                            ) {
                                Text(
                                    todo.priority.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = priorityColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            val statusColor = when (todo.status) {
                                TodoStatus.NOT_STARTED -> GraySecondary
                                TodoStatus.IN_PROGRESS -> Blue
                                TodoStatus.COMPLETED -> PriorityGreen
                                TodoStatus.CANCELLED -> Pink
                            }
                            val statusIcon = when (todo.status) {
                                TodoStatus.NOT_STARTED -> R.drawable.not_done
                                TodoStatus.IN_PROGRESS -> R.drawable.in_progress
                                TodoStatus.COMPLETED -> R.drawable.done
                                TodoStatus.CANCELLED -> R.drawable.cancelled
                            }
                            val statusLabel = when (todo.status) {
                                TodoStatus.NOT_STARTED -> "Not Started"
                                TodoStatus.IN_PROGRESS -> "In Progress"
                                TodoStatus.COMPLETED -> "Completed"
                                TodoStatus.CANCELLED -> "Cancelled"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painterResource(statusIcon),
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    statusLabel,
                                    color = statusColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            todo.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CharcoalDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Created - Due date on same row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    formatDateTime(todo.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                            }
                            Text(
                                "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = GraySecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    formatDateTime(todo.dueDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Details
                        Text(
                            todo.details ?: stringResource(R.string.no_details),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (todo.details != null) CharcoalDark else GraySecondary
                        )

                        if (todo.subtasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = GrayLight, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                stringResource(R.string.subtasks),
                                style = MaterialTheme.typography.titleSmall,
                                color = CharcoalDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            todo.subtasks.forEach { subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        subtask.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CharcoalDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            painterResource(if (subtask.isDone) R.drawable.done else R.drawable.not_done),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (subtask.isDone) stringResource(R.string.done) else stringResource(
                                                R.string.not_done
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color =  GraySecondary
                                        )
                                    }
                                }
                                HorizontalDivider(color = GrayLight, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateTime(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        instant.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
    } catch (e: Exception) {
        isoDate
    }
}
