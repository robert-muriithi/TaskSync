package com.dlight.domain.usecase.task

import com.dlight.domain.repository.TaskRepository
import javax.inject.Inject

class SyncTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(): Boolean {
        return try {
            taskRepository.syncTasks()
        } catch (_: Exception) {
            false
        }
    }
}
