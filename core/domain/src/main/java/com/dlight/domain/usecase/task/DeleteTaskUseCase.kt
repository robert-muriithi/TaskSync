package com.dlight.domain.usecase.task

import com.dlight.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        require(taskId.isNotBlank()) { "Task ID cannot be blank" }
        taskRepository.deleteTask(taskId)
    }
}
