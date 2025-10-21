package com.dlight.tasks.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlight.domain.usecase.task.DeleteTaskUseCase
import com.dlight.domain.usecase.task.GetTaskByIdUseCase
import com.dlight.domain.usecase.task.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val taskId: String = savedStateHandle.get<String>("taskId")
        ?: throw IllegalArgumentException("Task ID is required")

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val task = getTaskByIdUseCase(taskId)

                if (task != null) {
                    _uiState.update {
                        it.copy(
                            task = task,
                            title = task.title,
                            description = task.description,
                            isCompleted = task.completed,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false, errorMessage = "Task not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false, errorMessage = e.message ?: "Failed to load task"
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update {
            it.copy(
                title = title, titleError = null
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update {
            it.copy(
                description = description
            )
        }
    }

    fun onCompletedChange(completed: Boolean) {
        _uiState.update {
            it.copy(
                isCompleted = completed
            )
        }
    }

    fun onSaveClick() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            return
        }

        val task = currentState.task ?: return

        updateTask(
            task = task.copy(
                title = currentState.title,
                description = currentState.description,
                completed = currentState.isCompleted
            )
        )
    }

    private fun updateTask(task: com.dlight.domain.model.Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                updateTaskUseCase(task)
                _uiState.update {
                    it.copy(
                        isSaving = false, isTaskUpdated = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false, errorMessage = e.message ?: "Failed to update task"
                    )
                }
            }
        }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }


    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        _uiState.update { it.copy(showDeleteDialog = false) }
        deleteTask()
    }

    private fun deleteTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            try {
                deleteTaskUseCase(taskId)

                _uiState.update {
                    it.copy(
                        isDeleting = false, isTaskDeleted = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false, errorMessage = e.message ?: "Failed to delete task"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
