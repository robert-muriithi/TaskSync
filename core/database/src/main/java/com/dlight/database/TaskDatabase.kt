package com.dlight.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dlight.database.dao.TaskDao
import com.dlight.database.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
