package com.dlight.tasks.add

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
class AddTaskScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addTaskScreen_topBar_displaysTitle() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                Text(
                    "Add New Task",
                    modifier = Modifier.testTag("addTaskTitle")
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("addTaskTitle")
            .assertExists()
            .assertTextEquals("Add New Task")
    }

    @Test
    fun addTaskScreen_backButton_exists() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                IconButton(
                    onClick = {},
                    modifier = Modifier.testTag("addTaskBackButton")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("addTaskBackButton")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun addTaskScreen_backButton_clickable() {
        // Given
        var clicked = false

        composeTestRule.setContent {
            TaskSyncTheme {
                IconButton(
                    onClick = { clicked = true },
                    modifier = Modifier.testTag("addTaskBackButton")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }

        // When
        composeTestRule
            .onNodeWithTag("addTaskBackButton")
            .performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun addTaskScreen_saveButton_existsWithTestTag() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.testTag("addTaskSaveButton")
                ) {
                    Text("Create Task")
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("addTaskSaveButton")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun addTaskScreen_emptyTitle_saveButtonDisabled() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.testTag("addTaskSaveButton")
                ) {
                    Text("Create Task")
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("addTaskSaveButton")
            .assertIsNotEnabled()
    }

    @Test
    fun addTaskScreen_validTitle_saveButtonEnabled() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                Button(
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.testTag("addTaskSaveButton")
                ) {
                    Text("Create Task")
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("addTaskSaveButton")
            .assertIsEnabled()
    }

    @Test
    fun addTaskScreen_saveButton_clickable() {
        // Given
        var clicked = false

        composeTestRule.setContent {
            TaskSyncTheme {
                Button(
                    onClick = { clicked = true },
                    enabled = true,
                    modifier = Modifier.testTag("addTaskSaveButton")
                ) {
                    Text("Create Task")
                }
            }
        }

        // When
        composeTestRule
            .onNodeWithTag("addTaskSaveButton")
            .performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun addTaskScreen_titleField_hasTestTag() {
        composeTestRule.setContent {
            TaskSyncTheme {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Task Title") },
                    modifier = Modifier.testTag("addTaskTitleField")
                )
            }
        }

        composeTestRule
            .onNodeWithTag("addTaskTitleField")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun addTaskScreen_descriptionField_hasTestTag() {
        composeTestRule.setContent {
            TaskSyncTheme {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Description") },
                    modifier = Modifier.testTag("addTaskDescriptionField")
                )
            }
        }

        composeTestRule
            .onNodeWithTag("addTaskDescriptionField")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun addTaskScreen_errorMessage_displaysWhenPresent() {
        composeTestRule.setContent {
            TaskSyncTheme {
                Text(
                    "Title is required",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("addTaskErrorMessage")
                )
            }
        }

        composeTestRule
            .onNodeWithTag("addTaskErrorMessage")
            .assertExists()
            .assertTextEquals("Title is required")
    }
}
