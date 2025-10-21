package com.dlight.domain.model

import java.time.Instant


data class Task(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus {
    SYNCED,
    PENDING,
    SYNCING,
    CONFLICT
}
