package com.dlight.data.mapper

import com.dlight.database.entity.TaskEntity
import com.dlight.domain.model.SyncStatus
import com.dlight.domain.model.Task
import com.dlight.network.model.TaskDto
import java.time.Instant
import java.time.format.DateTimeFormatter

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        completed = completed,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
        syncStatus = syncStatus.name
    )
}

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        completed = completed,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
        syncStatus = try {
            SyncStatus.valueOf(syncStatus)
        } catch (e: IllegalArgumentException) {
            SyncStatus.SYNCED
        }
    )
}

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        completed = completed,
        createdAt = try {
            Instant.parse(createdAt)
        } catch (_: Exception) {
            Instant.now()
        },
        updatedAt = try {
            Instant.parse(updatedAt)
        } catch (_: Exception) {
            Instant.now()
        },
        syncStatus = SyncStatus.SYNCED
    )
}

fun Task.toDto(): TaskDto {
    val formatter = DateTimeFormatter.ISO_INSTANT
    return TaskDto(
        id = id,
        title = title,
        description = description,
        completed = completed,
        createdAt = formatter.format(createdAt),
        updatedAt = formatter.format(updatedAt)
    )
}

fun List<TaskEntity>.toDomain(): List<Task> = map { it.toDomain() }
fun List<TaskDto>.toDomainFromDto(): List<Task> = map { it.toDomain() }
