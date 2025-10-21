package com.dlight.domain.repository

import com.dlight.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observePendingTaskCount(): Flow<Int>
    suspend fun getTaskById(id: String): Task?
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
    suspend fun syncTasks(): Boolean
    suspend fun getPendingTasks(): List<Task>
}
