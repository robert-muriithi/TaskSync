package com.dlight.network.model

import com.google.gson.annotations.SerializedName

data class TaskDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("completed")
    val completed: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class TasksResponse(
    @SerializedName("tasks")
    val tasks: List<TaskDto>
)
