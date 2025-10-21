package com.dlight.tasksync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dlight.auth.ui.LoginScreen
import com.dlight.tasks.TasksScreen
import com.dlight.tasks.add.AddTaskScreen
import com.dlight.tasks.details.TaskDetailsScreen
import com.dlight.tasksync.navigation.NavRoutes
import com.dlight.tasksync.ui.splash.SplashScreen
import com.dlight.ui.theme.TaskSyncTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskSyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TaskSyncApp()
                }
            }
        }
    }
}

@Composable
fun TaskSyncApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                isLoggedIn = isLoggedIn,
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToTasks = {
                    navController.navigate(NavRoutes.TASKS) {
                        popUpTo(NavRoutes.SPLASH) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.TASKS) {
                        popUpTo(NavRoutes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(NavRoutes.TASKS) {
            TasksScreen(
                onNavigateToAddTask = {
                    navController.navigate(NavRoutes.ADD_TASK)
                },
                onNavigateToTaskDetails = { taskId ->
                    navController.navigate(NavRoutes.taskDetails(taskId))
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.TASKS) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(NavRoutes.ADD_TASK) {
            AddTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = NavRoutes.TASK_DETAILS_PATTERN,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailsScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

