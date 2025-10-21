package com.dlight.domain.sync

import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ConflictResolverTest {

    private lateinit var conflictResolver: ConflictResolver

    @Before
    fun setup() {
        conflictResolver = ConflictResolver()
    }

    @Test
    fun `resolveConflict - remote is newer - returns KeepRemote`() {
        // Given
        val now = Instant.now()
        val oneHourAgo = now.minusSeconds(3600)
        
        val localTask = Task(
            id = "1",
            title = "Local Title",
            description = "Local Description",
            completed = false,
            createdAt = oneHourAgo,
            updatedAt = oneHourAgo,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Remote Title",
            description = "Remote Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val result = conflictResolver.resolveConflict(localTask, remoteTask)

        // Then
        assertTrue(result is ConflictResolution.KeepRemote)
        assertEquals(remoteTask, (result as ConflictResolution.KeepRemote).task)
    }

    @Test
    fun `resolveConflict - local is newer - returns KeepLocal`() {
        // Given
        val now = Instant.now()
        val oneHourAgo = now.minusSeconds(3600)
        
        val localTask = Task(
            id = "1",
            title = "Local Title",
            description = "Local Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Remote Title",
            description = "Remote Description",
            completed = false,
            createdAt = oneHourAgo,
            updatedAt = oneHourAgo,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val result = conflictResolver.resolveConflict(localTask, remoteTask)

        // Then
        assertTrue(result is ConflictResolution.KeepLocal)
        assertEquals(localTask, (result as ConflictResolution.KeepLocal).task)
    }

    @Test
    fun `resolveConflict - same timestamp - keeps local`() {
        // Given
        val now = Instant.now()
        
        val localTask = Task(
            id = "1",
            title = "Local Title",
            description = "Local Description",
            completed = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Remote Title",
            description = "Remote Description",
            completed = true,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val result = conflictResolver.resolveConflict(localTask, remoteTask)

        assertTrue(result is ConflictResolution.KeepLocal)
        assertEquals(localTask, (result as ConflictResolution.KeepLocal).task)
    }

    @Test
    fun `hasConflict - same content - returns false`() {
        // Given
        val now = Instant.now()
        val task1 = Task(
            id = "1",
            title = "Same Title",
            description = "Same Description",
            completed = true,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )
        
        val task2 = task1.copy()

        // When
        val hasConflict = conflictResolver.hasConflict(task1, task2)

        // Then
        assertFalse(hasConflict)
    }

    @Test
    fun `hasConflict - different timestamps and title - returns true`() {
        // Given
        val now = Instant.now()
        val oneHourAgo = now.minusSeconds(3600)
        
        val localTask = Task(
            id = "1",
            title = "Local Title",
            description = "Same Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = oneHourAgo,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Remote Title",
            description = "Same Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val hasConflict = conflictResolver.hasConflict(localTask, remoteTask)

        // Then
        assertTrue(hasConflict)
    }

    @Test
    fun `hasConflict - different timestamps and description - returns true`() {
        // Given
        val now = Instant.now()
        val oneHourAgo = now.minusSeconds(3600)
        
        val localTask = Task(
            id = "1",
            title = "Same Title",
            description = "Local Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = oneHourAgo,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Same Title",
            description = "Remote Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val hasConflict = conflictResolver.hasConflict(localTask, remoteTask)

        // Then
        assertTrue(hasConflict)
    }

    @Test
    fun `hasConflict - different timestamps and completed status - returns true`() {
        // Given
        val now = Instant.now()
        val oneHourAgo = now.minusSeconds(3600)
        
        val localTask = Task(
            id = "1",
            title = "Same Title",
            description = "Same Description",
            completed = false,
            createdAt = oneHourAgo,
            updatedAt = oneHourAgo,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Same Title",
            description = "Same Description",
            completed = true,
            createdAt = oneHourAgo,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val hasConflict = conflictResolver.hasConflict(localTask, remoteTask)

        // Then
        assertTrue(hasConflict)
    }

    @Test
    fun `hasConflict - same timestamp but different content - returns false`() {
        // Given
        val now = Instant.now()
        
        val localTask = Task(
            id = "1",
            title = "Local Title",
            description = "Local Description",
            completed = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        
        val remoteTask = Task(
            id = "1",
            title = "Remote Title",
            description = "Remote Description",
            completed = true,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED
        )

        // When
        val hasConflict = conflictResolver.hasConflict(localTask, remoteTask)

        assertFalse(hasConflict)
    }
}
