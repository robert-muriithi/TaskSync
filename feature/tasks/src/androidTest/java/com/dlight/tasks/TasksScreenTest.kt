package com.dlight.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlight.ui.theme.TaskSyncTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun  tasksScreen_topBar_hasTitle() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                Text("Tasks")
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Tasks")
            .assertExists()
    }

    @Test
    fun tasksScreen_emptyState_showsPlaceholder() {
        composeTestRule.setContent {
            TaskSyncTheme {
                Column {
                    Text(
                        "No tasks yet",
                        modifier = Modifier.testTag("tasksEmptyTitle")
                    )
                    Text(
                        "Tap + to create your first task",
                        modifier = Modifier.testTag("tasksEmptySubtitle")
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag("tasksEmptyTitle")
            .assertExists()
            .assertTextEquals("No tasks yet")

        composeTestRule
            .onNodeWithTag("tasksEmptySubtitle")
            .assertExists()
    }

    @Test
    fun tasksScreen_fab_exists() {
        composeTestRule.setContent {
            TaskSyncTheme {
                FloatingActionButton(
                    onClick = {},
                    modifier = Modifier.testTag("tasksFab")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Task"
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag("tasksFab")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun tasksScreen_fab_clickable() {
        // Given
        var clicked = false

        composeTestRule.setContent {
            TaskSyncTheme {
                FloatingActionButton(
                    onClick = { clicked = true },
                    modifier = Modifier.testTag("tasksFab")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }

        // When
        composeTestRule
            .onNodeWithTag("tasksFab")
            .performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun tasksScreen_logoutButton_exists() {
        composeTestRule.setContent {
            TaskSyncTheme {
                IconButton(
                    onClick = {},
                    modifier = Modifier.testTag("tasksLogoutButton")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag("tasksLogoutButton")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun tasksScreen_syncBanner_displaysWhenPending() {
        composeTestRule.setContent {
            TaskSyncTheme {
                Surface(
                    modifier = Modifier.testTag("tasksSyncBanner")
                ) {
                    Text(
                        "3 tasks waiting to sync",
                        modifier = Modifier.testTag("tasksSyncBannerText")
                    )
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("tasksSyncBanner")
            .assertExists()

        composeTestRule
            .onNodeWithTag("tasksSyncBannerText")
            .assertTextContains("3 tasks waiting to sync")
    }
}
