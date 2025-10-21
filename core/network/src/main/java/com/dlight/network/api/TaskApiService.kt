package com.dlight.network.api

import com.dlight.network.model.TaskDto
import retrofit2.Response
import retrofit2.http.*

interface TaskApiService {
    @GET("tasks")
    suspend fun getTasks(): Response<List<TaskDto>>
    @GET("tasks")
    suspend fun getTasksSince(
        @Query("since") since: String
    ): Response<List<TaskDto>>
    @POST("tasks")
    suspend fun createTask(
        @Body task: TaskDto
    ): Response<TaskDto>
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body task: TaskDto
    ): Response<TaskDto>
    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: String
    ): Response<Unit>
}
