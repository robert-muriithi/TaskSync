package com.dlight.tasks.details

import com.dlight.domain.model.Task

data class TaskDetailsUiState(
    val task: Task? = null,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isTaskUpdated: Boolean = false,
    val isTaskDeleted: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false
)
