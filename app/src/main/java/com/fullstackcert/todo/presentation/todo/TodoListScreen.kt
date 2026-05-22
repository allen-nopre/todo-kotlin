package com.fullstackcert.todo.presentation.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fullstackcert.todo.R
import com.fullstackcert.todo.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.fullstackcert.todo.domain.model.Priority
import com.fullstackcert.todo.domain.model.Todo
import com.fullstackcert.todo.domain.model.TodoStatus
import com.fullstackcert.todo.presentation.auth.AuthViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filterPriority: Priority?,
    filterStatus: TodoStatus?,
    onApply: (Priority?, TodoStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    var tempPriority by remember { mutableStateOf(filterPriority) }
    var tempStatus by remember { mutableStateOf(filterStatus) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))
            Text("Filter", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onDismiss) {
                Icon(painterResource(R.drawable.cancel), contentDescription = "Close")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Priority",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                listOf(
                    null to "All",
                    Priority.LOW to "Low",
                    Priority.HIGH to "High",
                    Priority.CRITICAL to "Critical"
                ).forEach { (p, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = tempPriority == p, onClick = { tempPriority = p })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Status",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                listOf(
                    null to "All",
                    TodoStatus.NOT_STARTED to "Not Started",
                    TodoStatus.IN_PROGRESS to "In Progress",
                    TodoStatus.COMPLETED to "Completed",
                    TodoStatus.CANCELLED to "Cancelled"
                ).forEach { (s, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = tempStatus == s, onClick = { tempStatus = s })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Button(
            onClick = { onApply(tempPriority, tempStatus); onDismiss() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Apply")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToAdd: () -> Unit,
    onLogout: () -> Unit,
    refreshTrigger: NavBackStackEntry? = null,
    todoViewModel: TodoListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by todoViewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) todoViewModel.loadTodos()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            todoViewModel.clearError()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onLogout()
                }) { Text(stringResource(R.string.sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Tasks") },
            text = { Text("${state.selectedIds.size} task${if (state.selectedIds.size != 1) "s" else ""} will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    todoViewModel.deleteSelected()
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filterPriority = state.filterPriority,
            filterStatus = state.filterStatus,
            onApply = { p, s ->
                todoViewModel.setFilterPriority(p)
                todoViewModel.setFilterStatus(s)
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.my_todos)) },
                    actions = {                    }
                )
                if (true) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                painterResource(R.drawable.filter),
                                contentDescription = stringResource(R.string.filter)
                            )
                        }
                        state.filterPriority?.let { p ->
                            FilterChip(
                                selected = true,
                                onClick = { todoViewModel.setFilterPriority(null) },
                                label = {
                                    Text(
                                        p.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF757575)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        painterResource(R.drawable.cancel),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF757575)
                                    )
                                },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        state.filterStatus?.let { s ->
                            FilterChip(
                                selected = true,
                                onClick = { todoViewModel.setFilterStatus(null) },
                                label = {
                                    Text(
                                        s.name.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF757575)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        painterResource(R.drawable.cancel),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF757575)
                                    )
                                },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    painterResource(R.drawable.sort),
                                    contentDescription = stringResource(R.string.sort)
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }) {
                                SortBy.entries.forEach { sort ->
                                    DropdownMenuItem(
                                        text = { Text(sort.name.replace("_", " ")) },
                                        onClick = {
                                            todoViewModel.setSortBy(sort); showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                BottomAppBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = state.isSelectionMode,
                        onClick = { if (state.isSelectionMode) showDeleteDialog = true },
                        icon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painterResource(if (state.isSelectionMode) R.drawable.delete_active else R.drawable.delete_inactive),
                                    contentDescription = stringResource(R.string.home)
                                )
                                if (state.isSelectionMode) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Blue
                                    ) {
                                        Text(
                                            "${state.selectedIds.size}",
                                            color = White,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        label = null
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToAdd,
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_todo),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        },
                        label = null
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { showLogoutDialog = true },
                        icon = {
                            Icon(
                                painterResource(R.drawable.avatar),
                                contentDescription = stringResource(R.string.sign_out)
                            )
                        },
                        label = null
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.todos.isEmpty() -> Text(
                    stringResource(R.string.no_todos),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.todos, key = { it.id }) { todo ->
                        TodoItem(
                            todo = todo,
                            isSelected = state.selectedIds.contains(todo.id),
                            isSelectionMode = state.isSelectionMode,
                            onToggleSelect = { todoViewModel.toggleSelection(todo.id) },
                            onTitleClick = { onNavigateToDetail(todo.id) },
                            onLongPress = { todoViewModel.toggleSelection(todo.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoItem(
    todo: Todo,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelect: () -> Unit,
    onTitleClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val highlightColor = getHighlightColor(todo)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelect() },
                onLongClick = onLongPress
            )
            .then(
                if (isSelected) Modifier.background(OffWhite)
                else if (highlightColor != null) Modifier.background(highlightColor)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(32.dp))
            PriorityChip(todo.priority)
            Spacer(modifier = Modifier.width(16.dp))
            StatusChip(todo.status)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            todo.title,
            style = MaterialTheme.typography.titleSmall.copy(textDecoration = TextDecoration.Underline),
            color = CharcoalDark,
            modifier = Modifier
                .padding(start = 64.dp, top = 8.dp)
                .clickable { onTitleClick() }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 64.dp)
        ) {
            Icon(
                painterResource(R.drawable.due_date),
                contentDescription = stringResource(R.string.due_date),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                formatDate(todo.dueDate),
                style = MaterialTheme.typography.bodySmall,
                color = GraySecondary
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun PriorityChip(priority: Priority) {
    val color = when (priority) {
        Priority.LOW -> PriorityGreen
        Priority.HIGH -> PriorityYellow
        Priority.CRITICAL -> PriorityRed
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, color)
    ) {
        Text(
            priority.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StatusChip(status: TodoStatus) {

    val icon = when (status) {
        TodoStatus.NOT_STARTED -> R.drawable.not_done
        TodoStatus.IN_PROGRESS -> R.drawable.in_progress
        TodoStatus.COMPLETED -> R.drawable.done
        TodoStatus.CANCELLED -> R.drawable.cancelled
    }
    val label = when (status) {
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
            painterResource(icon),
            contentDescription = null,
            tint = Blue,
            modifier = Modifier.size(14.dp)
        )
        Text(
            label,
            color = GraySecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun getHighlightColor(todo: Todo): Color? {
    if (todo.status == TodoStatus.COMPLETED || todo.status == TodoStatus.CANCELLED) return null
    val now = Instant.now()
    val due = try {
        Instant.parse(todo.dueDate)
    } catch (e: Exception) {
        return null
    }
    val hoursUntilDue = ChronoUnit.HOURS.between(now, due)

    return when {
        hoursUntilDue < 0 -> PalePink
        todo.priority == Priority.CRITICAL && hoursUntilDue <= 48 -> PaleYellow
        hoursUntilDue <= 24 -> PaleGreen
        else -> null
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val local = instant.atZone(ZoneId.systemDefault())
        local.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    } catch (e: Exception) {
        isoDate
    }
}
