package com.dlight.domain.usecase.task

import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.observeTasks()
    }
}
