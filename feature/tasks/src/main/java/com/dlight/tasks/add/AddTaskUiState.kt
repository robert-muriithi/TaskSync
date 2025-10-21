package com.dlight.tasks.add

data class AddTaskUiState(
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    val isTaskCreated: Boolean = false,
    val errorMessage: String? = null
)
