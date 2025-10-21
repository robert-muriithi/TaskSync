package com.dlight.domain.sync

import com.dlight.domain.model.Task
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Strategy: "Last Updated Wins" based on timestamp
 */
@Singleton
class ConflictResolver @Inject constructor() {

    /**
     * Resolve conflict between local and remote task
     * 
     * @param local Local version of the task
     * @param remote Remote version of the task
     * @return The task that should be kept (winner)
     *
     * If timestamps are equal, prefer local version
     * If remote is newer, keep remote
     */
    fun resolveConflict(local: Task, remote: Task): ConflictResolution {
        return when {
            local.updatedAt == remote.updatedAt -> {
                ConflictResolution.KeepLocal(local)
            }
            remote.updatedAt.isAfter(local.updatedAt) -> {
                ConflictResolution.KeepRemote(remote)
            }
            else -> {
                ConflictResolution.KeepLocal(local)
            }
        }
    }
    /**
     * Check if there is a conflict between local and remote task
     * @param local Local version of the task
     * @param remote Remote version of the task
     * @return True if there is a conflict, false otherwise
     */
    fun hasConflict(local: Task, remote: Task): Boolean {
        return local.updatedAt != remote.updatedAt &&
               (local.title != remote.title ||
                local.description != remote.description ||
                local.completed != remote.completed)
    }
}
sealed class ConflictResolution {
    data class KeepLocal(val task: Task) : ConflictResolution()
    data class KeepRemote(val task: Task) : ConflictResolution()
}
