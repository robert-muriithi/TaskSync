package com.dlight.tasksync.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val SPLASH = "splash"
    const val TASKS = "tasks"
    const val ADD_TASK = "tasks/add"
    
    fun taskDetails(taskId: String) = "tasks/details/$taskId"
    const val TASK_DETAILS_PATTERN = "tasks/details/{taskId}"
}