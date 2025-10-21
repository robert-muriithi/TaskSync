package com.dlight.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlight.data.local.UserPreferences
import com.dlight.data.repository.TaskRepositoryImpl
import com.dlight.database.TaskDatabase
import com.dlight.database.dao.TaskDao
import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import com.dlight.domain.sync.ConflictResolver
import com.dlight.network.api.TaskApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SyncIntegrationTest {

    private lateinit var database: TaskDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var mockWebServer: MockWebServer
    private lateinit var taskApi: TaskApiService
    private lateinit var userPreferences: UserPreferences
    private lateinit var conflictResolver: ConflictResolver
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            TaskDatabase::class.java
        ).build()
        taskDao = database.taskDao()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        taskApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApiService::class.java)

        userPreferences = UserPreferences(context)
        conflictResolver = ConflictResolver()

        repository = TaskRepositoryImpl(taskDao, taskApi, userPreferences, conflictResolver)
    }

    @After
    fun tearDown() {
        database.close()
        mockWebServer.shutdown()
    }

    @Test
    fun end_to_end_sync_flow_offline_create_reconnect_sync_to_server() = runTest {

        // STEP 1: OFFLINE - Create task locally
        val now = Instant.now()
        val localTask = Task(
            id = "temp-123",
            title = "Offline Task",
            description = "Created while offline",
            completed = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        repository.createTask(localTask)


        val localTasks = repository.observeTasks().first()
        assertEquals(1, localTasks.size)
        assertEquals("Offline Task", localTasks[0].title)
        assertEquals(SyncStatus.PENDING, localTasks[0].syncStatus)

        val pendingCount = repository.observePendingTaskCount().first()
        assertEquals(1, pendingCount)


        // STEP 2: RECONNECT - Mock server responses
        val serverTaskId = "server-456"
        val serverResponseBody = """
            {
                "id": "$serverTaskId",
                "title": "Offline Task",
                "description": "Created while offline",
                "completed": false,
                "createdAt": "$now",
                "updatedAt": "$now"
            }
        """.trimIndent()

        // Mock successful CREATE response (POST /tasks)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(serverResponseBody)
                .setHeader("Content-Type", "application/json")
        )


        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .setHeader("Content-Type", "application/json")
        )

        // STEP 3: SYNC - Trigger synchronization
        val syncSuccess = repository.syncTasks()

        assertTrue(syncSuccess)

        // STEP 4: VERIFY - Check final state
        val syncedTasks = repository.observeTasks().first()
        assertEquals(1, syncedTasks.size)
        assertEquals(serverTaskId, syncedTasks[0].id)
        assertEquals("Offline Task", syncedTasks[0].title)
        assertEquals(SyncStatus.SYNCED, syncedTasks[0].syncStatus)

        val finalPendingCount = repository.observePendingTaskCount().first()
        assertEquals(0, finalPendingCount)

        assertEquals(2, mockWebServer.requestCount)
        
        val request1 = mockWebServer.takeRequest()
        assertEquals("POST", request1.method)
        assertTrue(request1.path?.startsWith("/tasks") == true)

        val request2 = mockWebServer.takeRequest()
        assertEquals("GET", request2.method)
        assertTrue(request2.path?.startsWith("/tasks") == true)
    }

    @Test
    fun end_to_end_sync_flow_offline_edit_reconnect_sync_updates_server() = runTest {
        // STEP 1: SETUP - Create synced task
        val now = Instant.now()
        val syncedTask = Task(
            id = "server-123",
            title = "Original Title",
            description = "Original Description",
            completed = false,
            createdAt = now.minusSeconds(3600),
            updatedAt = now.minusSeconds(3600),
            syncStatus = SyncStatus.SYNCED
        )

        repository.createTask(syncedTask.copy(syncStatus = SyncStatus.SYNCED))
        
        taskDao.updateSyncStatus("server-123", "SYNCED")

        // STEP 2: OFFLINE - Edit task
        val editedTask = syncedTask.copy(
            title = "Edited Title",
            description = "Edited while offline",
            completed = true,
            updatedAt = now
        )

        repository.updateTask(editedTask)

        val editedLocal = repository.getTaskById("server-123")
        assertEquals(SyncStatus.PENDING, editedLocal?.syncStatus)

        // STEP 3: RECONNECT - Mock server responses
        val serverResponseBody = """
            {
                "id": "server-123",
                "title": "Edited Title",
                "description": "Edited while offline",
                "completed": true,
                "createdAt": "${now.minusSeconds(3600)}",
                "updatedAt": "$now"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(serverResponseBody)
                .setHeader("Content-Type", "application/json")
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .setHeader("Content-Type", "application/json")
        )

        // STEP 4: SYNC - Trigger synchronization
        val syncSuccess = repository.syncTasks()

        assertTrue(syncSuccess)

        // STEP 5: VERIFY - Check final state
        val finalTask = repository.getTaskById("server-123")
        assertEquals("Edited Title", finalTask?.title)
        assertEquals("Edited while offline", finalTask?.description)
        assertEquals(true, finalTask?.completed)
        assertEquals(SyncStatus.SYNCED, finalTask?.syncStatus)

        // Verify no pending tasks
        val finalPendingCount = repository.observePendingTaskCount().first()
        assertEquals(0, finalPendingCount)
    }

    @Test
    fun end_to_end_sync_flow_conflict_resolution_picks_latest() = runTest {
        // STEP 1: SETUP - Create synced task
        val baseTime = Instant.now().minusSeconds(7200)  // 2hrss  ago
        val syncedTask = Task(
            id = "server-123",
            title = "Original Title",
            description = "Original Description",
            completed = false,
            createdAt = baseTime,
            updatedAt = baseTime,
            syncStatus = SyncStatus.SYNCED
        )

        repository.createTask(syncedTask.copy(syncStatus = SyncStatus.SYNCED))
        taskDao.updateSyncStatus("server-123", "SYNCED")

        // STEP 2: OFFLINE - Edit locally (1 hour ago)
        val localEditTime = Instant.now().minusSeconds(3600)
        val localEdit = syncedTask.copy(
            title = "Local Edit",
            updatedAt = localEditTime
        )
        repository.updateTask(localEdit)

        val remoteEditTime = Instant.now()
        val remoteTaskJson = """
            {
                "id": "server-123",
                "title": "Remote Edit",
                "description": "Edited on another device",
                "completed": true,
                "createdAt": "$baseTime",
                "updatedAt": "$remoteEditTime"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "id": "server-123",
                        "title": "Local Edit",
                        "description": "Original Description",
                        "completed": false,
                        "createdAt": "$baseTime",
                        "updatedAt": "$localEditTime"
                    }
                """.trimIndent())
                .setHeader("Content-Type", "application/json")
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[$remoteTaskJson]")
                .setHeader("Content-Type", "application/json")
        )

        // STEP 4: SYNC - Trigger synchronization
        val syncSuccess = repository.syncTasks()
        assertTrue(syncSuccess)

        // STEP 5: VERIFY - Remote (newer) wins
        val finalTask = repository.getTaskById("server-123")
        assertEquals("Remote Edit", finalTask?.title)
        assertEquals("Edited on another device", finalTask?.description)
        assertEquals(true, finalTask?.completed)
        assertEquals(SyncStatus.SYNCED, finalTask?.syncStatus)
    }
}
