package com.fullstackcert.todo.presentation.todo

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import com.fullstackcert.todo.ui.theme.GrayLight
import com.fullstackcert.todo.ui.theme.GraySecondary
import com.fullstackcert.todo.ui.theme.OffWhite
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fullstackcert.todo.presentation.auth.AuthViewModel
import coil.compose.AsyncImage
import com.fullstackcert.todo.R
import com.fullstackcert.todo.presentation.common.NoConnectionScreen
import com.fullstackcert.todo.presentation.common.isNetworkError
import com.fullstackcert.todo.ui.theme.Blue
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    todoId: Int?,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: AddEditTodoViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    val dueDateMillis = runCatching { Instant.parse(state.dueDate).toEpochMilli() }.getOrNull()

    val totalAttachments = state.attachments.size + state.pendingAttachments.size

    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                if (totalAttachments >= 5) {
                    Toast.makeText(
                        context,
                        "Maximum of 5 image attachments allowed.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@let
                }

                val mimeType = context.contentResolver.getType(it) ?: ""
                if (!mimeType.startsWith("image/")) {
                    Toast.makeText(context, "Only image files are allowed.", Toast.LENGTH_LONG)
                        .show()
                    return@let
                }

                val fileSize = context.contentResolver.query(
                    it, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getLong(0) else 0L
                } ?: 0L

                if (fileSize > 10 * 1024 * 1024) {
                    Toast.makeText(context, "File size must not exceed 10MB.", Toast.LENGTH_LONG)
                        .show()
                    return@let
                }

                val inputStream = context.contentResolver.openInputStream(it) ?: return@let
                val fileName = it.lastPathSegment ?: "attachment"
                val tempFile = File(context.cacheDir, fileName)
                FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
                viewModel.uploadAttachment(tempFile, mimeType, it)
            }
        }

    LaunchedEffect(todoId) { todoId?.let { viewModel.loadTodo(it) } }
    LaunchedEffect(state.toast) {
        state.toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearToast()
        }
    }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    LaunchedEffect(state.error) {
        state.error?.let {
            if (!isNetworkError(it)) {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    if (showDueDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let {
                        viewModel.updateDueDate(Instant.ofEpochMilli(it).toString())
                    }
                    showDueDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDueDatePicker = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        ) { DatePicker(state = dpState) }
    }

    if (state.error != null && isNetworkError(state.error)) {
        NoConnectionScreen(onRetry = { todoId?.let { viewModel.loadTodo(it) } })
        return
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                BottomAppBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = {

                        },
                        label = null
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.save() },
                                    enabled = !state.isLoading,
                                ) {
                                    if (state.isLoading) CircularProgressIndicator(
                                        modifier = Modifier.size(
                                            20.dp
                                        ), color = White, strokeWidth = 2.dp
                                    )
                                    else Text(stringResource(if (state.isEditing) R.string.save else R.string.save))
                                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Priority + Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var expandedPriority by remember { mutableStateOf(false) }
                if (!state.isEditing) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPriority,
                        onExpandedChange = { expandedPriority = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.priority.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_priority)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }) {
                            listOf("low", "high", "critical").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.uppercase()) },
                                    onClick = {
                                        viewModel.updatePriority(p); expandedPriority = false
                                    })
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = state.priority.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(stringResource(R.string.label_priority)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                var expandedStatus by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.status.replace("_", " ").uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_status)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }) {
                        listOf(
                            "not_started",
                            "in_progress",
                            "completed",
                            "cancelled"
                        ).forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.replace("_", " ").uppercase()) },
                                onClick = { viewModel.updateStatus(s); expandedStatus = false })
                        }
                    }
                }
            }

            // Completion date — shown as read-only when status is completed
            if (state.status == "completed") {
                OutlinedTextField(
                    value = if (!state.completedDate.isNullOrBlank()) formatForDisplay(state.completedDate!!) else formatForDisplay(
                        Instant.now().toString()
                    ),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.set_completed_date)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text(stringResource(R.string.title_hint)) },
                enabled = !state.isEditing,
                isError = state.titleError != null,
                minLines = 2,
                maxLines = 3,
                supportingText = {
                    if (state.titleError != null) Text(
                        state.titleError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    else Text("${state.title.length}/25")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Created + Due date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formatForDisplay(state.createdDate),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.label_created)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (state.dueDate.isNotBlank()) formatForDisplay(state.dueDate) else "",
                    onValueChange = {},
                    readOnly = true,
                    isError = state.dueDateError != null,
                    label = { Text(stringResource(R.string.label_due_date)) },
                    placeholder = { Text("Select due date") },
                    supportingText = if (state.dueDateError != null) {
                        { Text(state.dueDateError!!, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    trailingIcon = {
                        IconButton(onClick = { showDueDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDueDatePicker = true }
                )
            }

            // Details
            OutlinedTextField(
                value = state.details,
                onValueChange = { viewModel.updateDetails(it) },
                label = { Text(stringResource(R.string.details_hint)) },
                minLines = 3,
                maxLines = 6,
                supportingText = { Text("${state.details.length}/300") },
                modifier = Modifier.fillMaxWidth()
            )

            // Attachments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.attachments),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${state.attachments.size + state.pendingAttachments.size}/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (totalAttachments >= 5) MaterialTheme.colorScheme.error else GraySecondary
                )
            }
            if (totalAttachments < 5) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.filter),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Browse images to attach (${5 - totalAttachments} remaining)",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingIcon = {
                            if (state.isLoading && state.existingTodo != null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { filePicker.launch("image/*") }
                    )
                }
            }
            val allAttachments = state.attachments
            val pendingAttachments = state.pendingAttachments
            if (allAttachments.isNotEmpty() || pendingAttachments.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allAttachments.forEach { attachment ->
                        AttachmentRow(
                            name = attachment.fileName,
                            size = attachment.fileSize,
                            mimeType = attachment.mimeType,
                            previewUrl = attachment.url,
                            previewUri = null,
                            onDelete = { viewModel.deleteAttachment(attachment.id) }
                        )
                    }
                    pendingAttachments.forEachIndexed { index, pending ->
                        AttachmentRow(
                            name = pending.file.name,
                            size = null,
                            mimeType = pending.mimeType,
                            previewUrl = null,
                            previewUri = pending.uri,
                            onDelete = { viewModel.removePendingAttachment(index) }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Subtasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.subtasks), style = MaterialTheme.typography.titleSmall)
                Text(
                    "${state.subtasks.size}/10",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.subtasks.size >= 10) MaterialTheme.colorScheme.error else GraySecondary
                )
                OutlinedButton(
                    onClick = { viewModel.addSubtask() },
                    enabled = state.status != "completed" && state.subtasks.size < 10
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.new_subtask))
                }
            }

            var subtaskToDelete by remember { mutableStateOf<Pair<Int, String>?>(null) }

            subtaskToDelete?.let { (index, name) ->
                AlertDialog(
                    onDismissRequest = { subtaskToDelete = null },
                    title = { Text("Delete Subtask") },
                    text = { Text("Delete this subtask?\n\"${name.ifBlank { "Untitled" }}\"") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.removeSubtask(index)
                            subtaskToDelete = null
                        }) {
                            Text(
                                stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { subtaskToDelete = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            state.subtasks.forEachIndexed { index, subtask ->
                if (index == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.size(40.dp))
                        Text(
                            stringResource(R.string.title_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = GraySecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            stringResource(R.string.status_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = GraySecondary
                        )
                    }
                }
                SubtaskRow(
                    subtask = subtask,
                    onTitleChange = { viewModel.updateSubtaskTitle(index, it) },
                    onToggleDone = { viewModel.toggleSubtaskDone(index) },
                    onDelete = { subtaskToDelete = index to subtask.title }
                )
            }
            HorizontalDivider()
            if (state.subtasks.isNotEmpty() && state.subtasks.all { it.isDone }) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    buildAnnotatedString {
                        append("All Subtask are ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Done")
                        }
                        append("!")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: SubtaskInput,
    onTitleChange: (String) -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onDelete) {
            Icon(
                painterResource(R.drawable.delete_active),
                contentDescription = "Remove subtask",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        OutlinedTextField(
            value = subtask.title,
            onValueChange = onTitleChange,
            placeholder = { Text(stringResource(R.string.subtask_title_hint)) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = if (subtask.isDone) OffWhite else androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = if (subtask.isDone) OffWhite else Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            modifier = Modifier.padding(8.dp),
            checked = subtask.isDone,
            onCheckedChange = { checked -> if (checked != subtask.isDone) onToggleDone() }
        )
    }
}

@Composable
private fun AttachmentRow(
    name: String,
    size: Long?,
    mimeType: String?,
    previewUrl: String?,
    previewUri: Uri?,
    onDelete: (() -> Unit)?
) {
    val isImage = mimeType?.startsWith("image/") == true
    val imageModel: Any? = when {
        previewUri != null -> previewUri
        previewUrl != null -> previewUrl
        else -> null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GrayLight, MaterialTheme.shapes.extraSmall)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .border(0.5.dp, GrayLight, MaterialTheme.shapes.extraSmall)
        ) {
            if (isImage && imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painterResource(R.drawable.filter),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = GraySecondary
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(
                size?.let { formatFileSize(it) } ?: "Pending upload",
                style = MaterialTheme.typography.bodySmall,
                color = GraySecondary
            )
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatForDisplay(isoDate: String): String {
    return try {
        Instant.parse(isoDate).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
    } catch (e: Exception) {
        isoDate
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
}
