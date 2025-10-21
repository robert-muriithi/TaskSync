package com.dlight.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dlight.database. DatabaseConstants.TASKS_TABLE_NAME

@Entity(tableName = TASKS_TABLE_NAME)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String
)
