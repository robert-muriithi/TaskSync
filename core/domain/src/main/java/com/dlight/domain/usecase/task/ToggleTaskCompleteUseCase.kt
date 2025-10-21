package com.dlight.domain.usecase.task

import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import java.time.Instant
import javax.inject.Inject

class ToggleTaskCompleteUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        val updatedTask = task.copy(
            completed = !task.completed,
            updatedAt = Instant.now()
        )
        
        taskRepository.updateTask(updatedTask)
    }
}
