package com.dlight.sync

import com.dlight.common.network.NetworkMonitor
import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import com.dlight.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant


class SyncManagerTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var syncManager: SyncManager

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        networkMonitor = mockk(relaxed = true)
        
        syncManager = SyncManager(taskRepository, networkMonitor)
    }

    @Test
    fun `getPendingSyncCount returns count from repository`() = runTest {
        // Given
        val pendingTasks = listOf(
            createTask("1", syncStatus = SyncStatus.PENDING),
            createTask("2", syncStatus = SyncStatus.PENDING),
            createTask("3", syncStatus = SyncStatus.SYNCING)
        )
        coEvery { taskRepository.getPendingTasks() } returns pendingTasks

        // When
        val count = syncManager.getPendingSyncCount()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun `getPendingSyncCount with no pending tasks returns zero`() = runTest {
        // Given
        coEvery { taskRepository.getPendingTasks() } returns emptyList()

        // When
        val count = syncManager.getPendingSyncCount()

        // Then
        assertEquals(0, count)
    }

    @Test
    fun `getPendingSyncCount with repository error returns zero`() = runTest {
        // Given
        coEvery { taskRepository.getPendingTasks() } throws RuntimeException("Database error")

        // When
        val count = syncManager.getPendingSyncCount()

        // Then
        assertEquals(0, count)
    }

    @Test
    fun `getPendingSyncCount filters out synced tasks`() = runTest {
        // Given
        val mixedTasks = listOf(
            createTask("1", syncStatus = SyncStatus.PENDING),
            createTask("2", syncStatus = SyncStatus.SYNCED),
            createTask("3", syncStatus = SyncStatus.SYNCING),
            createTask("4", syncStatus = SyncStatus.SYNCED)
        )
        coEvery { taskRepository.getPendingTasks() } returns mixedTasks

        // When
        val count = syncManager.getPendingSyncCount()

        // Then
        assertEquals(4, count)
    }

    private fun createTask(
        id: String,
        title: String = "Test Task",
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ): Task {
        val now = Instant.now()
        return Task(
            id = id,
            title = title,
            description = "Description",
            completed = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = syncStatus
        )
    }
}


