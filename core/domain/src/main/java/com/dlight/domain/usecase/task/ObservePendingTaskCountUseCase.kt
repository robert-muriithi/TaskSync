package com.dlight.domain.usecase.task

import com.dlight.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePendingTaskCountUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<Int> {
        return taskRepository.observePendingTaskCount()
    }
}
