package com.dlight.database.dao

import androidx.room.*
import com.dlight.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun observeAllTaks(): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    suspend fun getAll(): List<TaskEntity>
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?
    @Query("SELECT * FROM tasks WHERE syncStatus = 'PENDING'")
    suspend fun getPendingTasks(): List<TaskEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)
    @Delete
    suspend fun delete(task: TaskEntity)
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
    @Query("UPDATE tasks SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}
