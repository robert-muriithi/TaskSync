package com.dlight.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlight.database.TaskDatabase
import com.dlight.database.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant


@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: TaskDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            TaskDatabase::class.java
        ).build()
        taskDao = database.taskDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertTask_andGetById_returnsCorrectTask() = runTest {
        // Given
        val task = createTaskEntity(id = "1", title = "Test Task")

        // When
        taskDao.upsert(task)
        val retrieved = taskDao.getById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals(task.id, retrieved?.id)
        assertEquals(task.title, retrieved?.title)
    }

    @Test
    fun upsert_existingTask_updatesTask() = runTest {
        // Given
        val task = createTaskEntity(id = "1", title = "Original Title")
        taskDao.upsert(task)

        // When
        val updatedTask = task.copy(title = "Updated Title")
        taskDao.upsert(updatedTask)
        val retrieved = taskDao.getById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved?.title)
    }

    @Test
    fun observeAll_emitsAllTaksTasks() = runTest {
        // Given
        val task1 = createTaskEntity(id = "1", title = "Task 1")
        val task2 = createTaskEntity(id = "2", title = "Task 2")
        val task3 = createTaskEntity(id = "3", title = "Task 3")

        // When
        taskDao.upsert(task1)
        taskDao.upsert(task2)
        taskDao.upsert(task3)
        val tasks = taskDao.observeAllTaks().first()

        // Then
        assertEquals(3, tasks.size)
        assertTrue(tasks.any { it.id == "1" })
        assertTrue(tasks.any { it.id == "2" })
        assertTrue(tasks.any { it.id == "3" })
    }

    @Test
    fun getPendingTasks_returnsPendingTasksOnly() = runTest {
        // Given
        val syncedTask = createTaskEntity(id = "1", title = "Synced", syncStatus = "SYNCED")
        val pendingTask1 = createTaskEntity(id = "2", title = "Pending 1", syncStatus = "PENDING")
        val pendingTask2 = createTaskEntity(id = "3", title = "Pending 2", syncStatus = "PENDING")
        val syncingTask = createTaskEntity(id = "4", title = "Syncing", syncStatus = "SYNCING")

        // When
        taskDao.upsert(syncedTask)
        taskDao.upsert(pendingTask1)
        taskDao.upsert(pendingTask2)
        taskDao.upsert(syncingTask)
        
        val pendingTasks = taskDao.getPendingTasks()

        // Then
        assertEquals(2, pendingTasks.size)
        assertTrue(pendingTasks.all { it.syncStatus == "PENDING" })
        assertTrue(pendingTasks.any { it.id == "2" })
        assertTrue(pendingTasks.any { it.id == "3" })
    }

    @Test
    fun updateSyncStatus_updatesStatusCorrectly() = runTest {
        // Given
        val task = createTaskEntity(id = "1", title = "Test Task", syncStatus = "PENDING")
        taskDao.upsert(task)

        // When
        taskDao.updateSyncStatus("1", "SYNCED")
        val updated = taskDao.getById("1")

        // Then
        assertNotNull(updated)
        assertEquals("SYNCED", updated?.syncStatus)
    }

    @Test
    fun deleteById_removesTask() = runTest {
        // Given
        val task = createTaskEntity(id = "1", title = "Test Task")
        taskDao.upsert(task)
        
        // Verify it exists
        assertNotNull(taskDao.getById("1"))

        // When
        taskDao.deleteById("1")
        val retrieved = taskDao.getById("1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun deleteAll_removesAllTasks() = runTest {
        // Given
        val task1 = createTaskEntity(id = "1", title = "Task 1")
        val task2 = createTaskEntity(id = "2", title = "Task 2")
        val task3 = createTaskEntity(id = "3", title = "Task 3")
        
        taskDao.upsert(task1)
        taskDao.upsert(task2)
        taskDao.upsert(task3)
        
        // Verify they exist
        assertEquals(3, taskDao.getAll().size)

        // When
        taskDao.deleteAll()
        val remaining = taskDao.getAll()

        // Then
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun upsertAll_insertsMultipleTasks() = runTest {
        // Given
        val tasks = listOf(
            createTaskEntity(id = "1", title = "Task 1"),
            createTaskEntity(id = "2", title = "Task 2"),
            createTaskEntity(id = "3", title = "Task 3")
        )

        // When
        taskDao.upsertAll(tasks)
        val allTasks = taskDao.getAll()

        // Then
        assertEquals(3, allTasks.size)
    }

    @Test
    fun upsertAll_updatesExistingTasks() = runTest {
        // Given
        val task1 = createTaskEntity(id = "1", title = "Original 1")
        val task2 = createTaskEntity(id = "2", title = "Original 2")
        taskDao.upsert(task1)
        taskDao.upsert(task2)

        // When
        val updatedTasks = listOf(
            task1.copy(title = "Updated 1"),
            task2.copy(title = "Updated 2")
        )
        taskDao.upsertAll(updatedTasks)
        
        val retrieved1 = taskDao.getById("1")
        val retrieved2 = taskDao.getById("2")

        // Then
        assertEquals("Updated 1", retrieved1?.title)
        assertEquals("Updated 2", retrieved2?.title)
    }

    @Test
    fun delete_removesSpecificTask() = runTest {
        // Given
        val task1 = createTaskEntity(id = "1", title = "Task 1")
        val task2 = createTaskEntity(id = "2", title = "Task 2")
        taskDao.upsert(task1)
        taskDao.upsert(task2)

        // When
        taskDao.delete(task1)
        val allTasks = taskDao.getAll()

        // Then
        assertEquals(1, allTasks.size)
        assertEquals("2", allTasks[0].id)
    }

    private fun createTaskEntity(
        id: String,
        title: String,
        description: String = "Description for $title",
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
