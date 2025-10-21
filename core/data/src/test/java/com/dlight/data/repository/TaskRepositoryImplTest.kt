package com.dlight.data.repository

import com.dlight.data.local.UserPreferences
import com.dlight.database.dao.TaskDao
import com.dlight.database.entity.TaskEntity
import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import com.dlight.domain.sync.ConflictResolver
import com.dlight.network.api.TaskApiService
import com.dlight.network.model.TaskDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.time.Instant


class TaskRepositoryImplTest {

    private lateinit var taskDao: TaskDao
    private lateinit var taskApi: TaskApiService
    private lateinit var userPreferences: UserPreferences
    private lateinit var conflictResolver: ConflictResolver
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setup() {
        taskDao = mockk(relaxed = true)
        taskApi = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true)
        conflictResolver = ConflictResolver()
        
        repository = TaskRepositoryImpl(taskDao, taskApi, userPreferences, conflictResolver)
    }

    @Test
    fun `observeTasks returns tasks from local database`() = runTest {
        // Given
        val entity = createTaskEntity("1", "Test Task")
        every { taskDao.observeAllTaks() } returns flowOf(listOf(entity))

        // When
        val tasks = repository.observeTasks().first()

        // Then
        assertEquals(1, tasks.size)
        assertEquals("1", tasks[0].id)
        assertEquals("Test Task", tasks[0].title)
    }

    @Test
    fun `createTask saves to local database with PENDING status`() = runTest {
        // Given
        val now = Instant.now()
        val task = Task(
            id = "1",
            title = "New Task",
            description = "Description",
            completed = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        val entitySlot = slot<TaskEntity>()
        coEvery { taskDao.upsert(capture(entitySlot)) } returns Unit

        // When
        repository.createTask(task)

        // Then
        coVerify { taskDao.upsert(any()) }
        assertEquals(SyncStatus.PENDING.name, entitySlot.captured.syncStatus)
    }

    @Test
    fun `updateTask saves to local database with PENDING status`() = runTest {
        // Given
        val task = Task(
            id = "1",
            title = "Updated Task",
            description = "Updated Description",
            completed = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            syncStatus = SyncStatus.SYNCED
        )

        val entitySlot = slot<TaskEntity>()
        coEvery { taskDao.upsert(capture(entitySlot)) } returns Unit

        // When
        repository.updateTask(task)

        // Then
        coVerify { taskDao.upsert(any()) }
        assertEquals(SyncStatus.PENDING.name, entitySlot.captured.syncStatus)
        assertEquals(true, entitySlot.captured.completed)
    }

    @Test
    fun `deleteTask removes from local database`() = runTest {
        // Given
        val taskId = "1"

        // When
        repository.deleteTask(taskId)

        // Then
        coVerify { taskDao.deleteById(taskId) }
    }

    @Test
    fun `getTaskById returns task from local database`() = runTest {
        // Given
        val entity = createTaskEntity("1", "Test Task")
        coEvery { taskDao.getById("1") } returns entity

        // When
        val task = repository.getTaskById("1")

        // Then
        assertEquals("1", task?.id)
        assertEquals("Test Task", task?.title)
    }

    @Test
    fun `getPendingTasks returns pending tasks from local database`() = runTest {
        // Given
        val pendingEntity = createTaskEntity("1", "Pending Task", syncStatus = "PENDING")
        coEvery { taskDao.getPendingTasks() } returns listOf(pendingEntity)

        // When
        val tasks = repository.getPendingTasks()

        // Then
        assertEquals(1, tasks.size)
        assertEquals(SyncStatus.PENDING, tasks[0].syncStatus)
    }

    @Test
    fun `syncTasks calls repository methods`() = runTest {
        // Given
        coEvery { taskDao.getPendingTasks() } returns emptyList()
        coEvery { userPreferences.getLastSyncTime() } returns 0L
        coEvery { taskApi.getTasksSince(any()) } returns Response.success(emptyList())
        coEvery { userPreferences.updateLastSyncTime(any()) } returns Unit

        // When
        repository.syncTasks()

        // Then
        coVerify { taskDao.getPendingTasks() }
        coVerify { userPreferences.getLastSyncTime() }
    }


    @Test
    fun `observePendingTaskCount emits correct count`() = runTest {
        // Given
        val entities = listOf(
            createTaskEntity("1", "Pending 1", syncStatus = "PENDING"),
            createTaskEntity("2", "Synced", syncStatus = "SYNCED"),
            createTaskEntity("3", "Pending 2", syncStatus = "PENDING")
        )
        every { taskDao.observeAllTaks() } returns flowOf(entities)

        // When
        val count = repository.observePendingTaskCount().first()

        // Then
        assertEquals(2, count)
    }

    @Test
    fun `syncTasks fetches remote changes and updates local database`() = runTest {
        // Given
        val now = Instant.now()
        val remoteDto = TaskDto(
            id = "remote-1",
            title = "Remote Task",
            description = "From Server",
            completed = true,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )

        coEvery { taskDao.getPendingTasks() } returns emptyList()
        coEvery { userPreferences.getLastSyncTime() } returns now.minusSeconds(3600).toEpochMilli()
        coEvery { taskApi.getTasksSince(any()) } returns Response.success(listOf(remoteDto))
        coEvery { taskDao.getById("remote-1") } returns null

        // When
        val result = repository.syncTasks()

        // Then
        assertTrue(result)
        coVerify {
            taskDao.upsert(match {
                it.id == "remote-1" && 
                it.title == "Remote Task" &&
                it.syncStatus == "SYNCED"
            })
        }
        coVerify { userPreferences.updateLastSyncTime(any()) }
    }

    private fun createTaskEntity(
        id: String,
        title: String,
        description: String = "Description",
        completed: Boolean = false,
        syncStatus: String = "SYNCED"
    ): TaskEntity {
        val now = Instant.now().toEpochMilli()
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            completed = completed,
            createdAt = now,
            updatedAt = now,
            syncStatus = syncStatus
        )
    }
}
