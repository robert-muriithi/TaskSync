package com.dlight.domain.usecase.task

import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import java.time.Instant
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        completed: Boolean = false
    ) {
        require(title.isNotBlank()) { "Task title cannot be blank" }
        
        val now = Instant.now()
        val task = Task(
            id = generateTaskId(),
            title = title.trim(),
            description = description.trim(),
            completed = completed,
            createdAt = now,
            updatedAt = now
        )
        
        taskRepository.createTask(task)
    }
    
    private fun generateTaskId(): String {
        return "task_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
