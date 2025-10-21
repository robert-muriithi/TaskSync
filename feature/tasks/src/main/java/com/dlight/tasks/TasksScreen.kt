package com.dlight.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToTaskDetails: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingTaskCount.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TasksTopBar(
                pendingCount = pendingCount,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                modifier = Modifier.testTag("tasksFab")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_task)
                )
            }
        },
        modifier = Modifier.testTag("tasksScreen")
    ) { padding ->
        TasksScreenContent(
            modifier = Modifier.padding(padding),
            syncState = syncState,
            pendingCount = pendingCount,
            onNavigateToTaskDetails = onNavigateToTaskDetails,
            onRefresh = {
                viewModel.refresh()
            },
            isRefreshing = isRefreshing,
            tasks = tasks,
            onToggleTaskComplete = { task ->
                viewModel.toggleTaskComplete(
                    task
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreenContent(
    modifier: Modifier = Modifier,
    syncState: String,
    pendingCount: Int,
    tasks: List<Task> = emptyList(),
    isRefreshing: Boolean = false,
    onNavigateToTaskDetails: (String) -> Unit = { },
    onRefresh: () -> Unit = { },
    onToggleTaskComplete: (Task) -> Unit = { }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        SyncStatusBanner(
            syncState = syncState,
            pendingCount = pendingCount
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                onRefresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("tasksRefreshBox")
        ) {
            if (tasks.isEmpty() && !isRefreshing) {
                EmptyTasksPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("tasksList"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggleComplete = { onToggleTaskComplete(task) },
                            onClick = { onNavigateToTaskDetails(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksTopBar(
    pendingCount: Int,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Column(modifier = Modifier.testTag("tasksTopBarTitle")) {
                Text(stringResource(R.string.tasks_title))
                if (pendingCount > 0) {
                    Text(
                        text = stringResource(
                            if (pendingCount == 1) R.string.tasks_pending_sync_single
                            else R.string.tasks_pending_sync_plural,
                            pendingCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("tasksPendingCount")
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onLogout,
                modifier = Modifier.testTag("tasksLogoutButton")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.cd_logout)
                )
            }
        },
        modifier = Modifier.testTag("tasksTopBar")
    )
}

@Composable
private fun SyncStatusBanner(
    syncState: String,
    pendingCount: Int
) {
    if (syncState == "SYNCING" || pendingCount > 0) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tasksSyncBanner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (syncState == "SYNCING") {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .testTag("tasksSyncIndicator"),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.tasks_syncing),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (pendingCount > 0) {
                    Text(
                        text = stringResource(
                            R.string.task_waiting_to_sync,
                            pendingCount,
                            if (pendingCount > 1) "s" else ""
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("tasksSyncBannerText")
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasksEmptyState"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.tasks_empty),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("tasksEmptyTitle")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tasks_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("tasksEmptySubtitle")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("taskItem_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.testTag("taskItemCheckbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("taskItemTitle_${task.id}")
                    )
                    when (task.syncStatus) {
                        SyncStatus.PENDING -> {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.testTag("taskItemSyncBadge_${task.id}")
                            ) {
                                Text(
                                    text = stringResource(R.string.sync_status_pending),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        SyncStatus.SYNCING -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("taskItemSyncIndicator_${task.id}"),
                                strokeWidth = 2.dp
                            )
                        }

                        SyncStatus.CONFLICT -> {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.testTag("taskItemConflictBadge_${task.id}")
                            ) {
                                Text(
                                    text = "Conflict",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }

                        else -> {}
                    }
                }

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("taskItemDescription_${task.id}")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(task.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("taskItemTimestamp_${task.id}")
                )
            }
        }
    }
}

private fun formatTimestamp(instant: Instant): String {
    val formatter = DateTimeFormatter
        .ofPattern("MMM dd, yyyy 'at' hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}


@Preview
@Composable
private fun TaskItemPreview() {
    Scaffold(
        modifier = Modifier
    ) { paddingValues ->
        TasksScreen(
            onNavigateToAddTask = {},
            onNavigateToTaskDetails = {},
            onLogout = {},
        )
    }
}