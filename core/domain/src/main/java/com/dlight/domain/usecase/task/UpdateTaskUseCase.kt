package com.dlight.domain.usecase.task

import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import java.time.Instant
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        require(task.id.isNotBlank()) { "Task ID cannot be blank" }
        require(task.title.isNotBlank()) { "Task title cannot be blank" }
        val updatedTask = task.copy(updatedAt = Instant.now())
        taskRepository.updateTask(updatedTask)
    }
}
