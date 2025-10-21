package com.dlight.domain.usecase.task

import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Task? {
        require(taskId.isNotBlank()) { "Task ID cannot be blank" }
        return taskRepository.getTaskById(taskId)
    }
}
