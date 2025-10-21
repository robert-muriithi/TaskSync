package com.dlight.data.repository

import com.dlight.data.local.UserPreferences
import com.dlight.data.mapper.toDomain
import com.dlight.data.mapper.toDomainFromDto
import com.dlight.data.mapper.toDto
import com.dlight.data.mapper.toEntity
import com.dlight.database.dao.TaskDao
import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import com.dlight.network.api.TaskApiService
import com.dlight.domain.sync.ConflictResolution
import com.dlight.domain.sync.ConflictResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository with offline-first architecture
 *
 * Strategy:
 * 1. All write operations go to Room immediately
 * 2. Tasks are marked as PENDING when modified locally
 * 3. Sync operation pushes pending changes and pulls remote updates
 * 4. Conflict resolution uses "last updated wins" strategy
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskApi: TaskApiService,
    private val userPreferences: UserPreferences,
    private val conflictResolver: ConflictResolver
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> {
        return taskDao.observeAllTaks().map { it.toDomain() }
    }

    override fun observePendingTaskCount(): Flow<Int> {
        return taskDao.observeAllTaks().map { entities ->
            entities.count {
                it.syncStatus == SyncStatus.PENDING.name ||
                        it.syncStatus == SyncStatus.SYNCING.name
            }
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getById(id)?.toDomain()
    }

    override suspend fun createTask(task: Task) {
        val now = Instant.now()
        val taskToSave = task.copy(
            syncStatus = SyncStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )
        taskDao.upsert(taskToSave.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        val taskToSave = task.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = Instant.now()
        )
        taskDao.upsert(taskToSave.toEntity())
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteById(taskId)
    }

    override suspend fun getPendingTasks(): List<Task> {
        return taskDao.getPendingTasks().toDomain()
    }

    /**
     * Sync tasks with the remote server
     *
     * Process:
     * 1. Push pending local changes to server
     * 2. Pull remote changes since last sync
     * 3. Resolve conflicts (last updated wins)
     * 4. Update local database
     */
    override suspend fun syncTasks(): Boolean {
        return try {
            pushPendingTasks()
            pullRemoteTasks()
            true
        } catch (e: Exception) {
            Timber.e(e, "Sync failed with exception")
            false
        }
    }

    private suspend fun pushPendingTasks() {
        val pendingTasks = taskDao.getPendingTasks()

        pendingTasks.forEach { taskEntity ->
            try {
                taskDao.updateSyncStatus(taskEntity.id, SyncStatus.SYNCING.name)
                val task = taskEntity.toDomain()

                if (isNewTask(task)) {
                    pushNewTask(task)
                } else {
                    pushUpdatedTask(task)
                }
            } catch (e: Exception) {
                taskDao.updateSyncStatus(taskEntity.id, SyncStatus.PENDING.name)
                Timber.e(e, "Error syncing task ${taskEntity.id}")
            }
        }
    }

    private fun isNewTask(task: Task) = task.createdAt == task.updatedAt

    private suspend fun pushNewTask(task: Task) {
        val response = taskApi.createTask(task.toDto())

        when {
            response.isSuccessful -> {
                updateTaskFromResponse(task.id, response.body())
            }
            response.code() == 409 -> {
                pushUpdatedTask(task)
            }
            else -> {
                taskDao.updateSyncStatus(task.id, SyncStatus.PENDING.name)
            }
        }
    }

    private suspend fun pushUpdatedTask(task: Task) {
        val response = taskApi.updateTask(task.id, task.toDto())

        when {
            response.isSuccessful -> {
                updateTaskFromResponse(task.id, response.body())
            }
            response.code() == 404 -> {
                pushNewTask(task)
            }
            else -> {
                taskDao.updateSyncStatus(task.id, SyncStatus.PENDING.name)
            }
        }
    }

    private suspend fun updateTaskFromResponse(localId: String, serverTask: com.dlight.network.model.TaskDto?) {
        if (serverTask != null) {
            taskDao.deleteById(localId)
            val syncedTask = serverTask.toDomain().toEntity().copy(
                syncStatus = SyncStatus.SYNCED.name
            )
            taskDao.upsert(syncedTask)
        } else {
            taskDao.updateSyncStatus(localId, SyncStatus.SYNCED.name)
        }
    }

    private suspend fun pullRemoteTasks(): Boolean {
        val lastSyncTime = userPreferences.getLastSyncTime()
        val response = fetchRemoteTasks(lastSyncTime)

        if (!response.isSuccessful || response.body() == null) {
            Timber.e("Failed to fetch remote tasks: ${response.code()}")
            return false
        }

        val remoteTasks = response.body()!!.toDomainFromDto()
        remoteTasks.forEach { remoteTask ->
            mergeRemoteTask(remoteTask)
        }

        userPreferences.updateLastSyncTime(Instant.now().toEpochMilli())
        return true
    }

    private suspend fun fetchRemoteTasks(lastSyncTime: Long) =
        if (lastSyncTime > 0) {
            val sinceParam = formatInstant(Instant.ofEpochMilli(lastSyncTime))
            taskApi.getTasksSince(sinceParam)
        } else {
            taskApi.getTasks()
        }

    private fun formatInstant(instant: Instant): String {
        return DateTimeFormatter.ISO_INSTANT.format(instant)
    }

    private suspend fun mergeRemoteTask(remoteTask: Task) {
        val localTask = taskDao.getById(remoteTask.id)

        when {
            localTask == null -> {
                insertRemoteTask(remoteTask)
            }
            isLocalTaskPending(localTask) -> {}
            else -> {
                resolveAndMerge(localTask.toDomain(), remoteTask)
            }
        }
    }

    private suspend fun insertRemoteTask(remoteTask: Task) {
        val taskToInsert = remoteTask.copy(syncStatus = SyncStatus.SYNCED)
        taskDao.upsert(taskToInsert.toEntity())
    }

    private fun isLocalTaskPending(localTask: com.dlight.database.entity.TaskEntity): Boolean {
        val status = localTask.syncStatus
        return status == SyncStatus.PENDING.name || status == SyncStatus.SYNCING.name
    }

    private suspend fun resolveAndMerge(localTask: Task, remoteTask: Task) {
        if (!conflictResolver.hasConflict(localTask, remoteTask)) {
            if (remoteTask.updatedAt.isAfter(localTask.updatedAt)) {
                updateWithRemoteTask(remoteTask)
            }
            return
        }

        when (val resolution = conflictResolver.resolveConflict(localTask, remoteTask)) {
            is ConflictResolution.KeepLocal -> {
                taskDao.updateSyncStatus(localTask.id, SyncStatus.PENDING.name)
            }
            is ConflictResolution.KeepRemote -> {
                updateWithRemoteTask(resolution.task)
            }
        }
    }

    private suspend fun updateWithRemoteTask(remoteTask: Task) {
        val taskToUpdate = remoteTask.copy(syncStatus = SyncStatus.SYNCED)
        taskDao.upsert(taskToUpdate.toEntity())
    }
}