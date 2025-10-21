package com.dlight.auth.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlight.auth.R
import com.dlight.ui.theme.TaskSyncTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_initialState_displaysCorrectly() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("loginWelcomeText")
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("Welcome")

        composeTestRule
            .onNodeWithTag("loginSubtitle")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("loginEmailField")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("loginButton")
            .assertExists()
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_emailInput_updatesField() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(email = "test@example.com"),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }
        composeTestRule
            .onNodeWithTag("loginEmailField")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyEmail_buttonDisabled() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(email = ""),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loginButton")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_validEmail_buttonEnabled() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(email = "test@example.com"),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loginButton")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_loadingState_displaysCorrectly() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(
                        email = "test@example.com",
                        isLoading = true
                    ),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loginButton")
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag("loginEmailField")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_emailError_displaysErrorMessage() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(
                        email = "invalid-email",
                        emailError = "Please enter a valid email address"
                    ),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Please enter a valid email address")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_clickLoginButton_triggersCallback() {
        // Given
        var loginClicked = false
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(email = "test@example.com"),
                    onEmailChange = {},
                    onLoginClick = { loginClicked = true }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithTag("loginButton")
            .performClick()

        // Then
        assert(loginClicked)
    }

    @Test
    fun loginScreen_typeEmailAndLogin_workflow() {
        // Given
        var loginClicked = false
        
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(email = "user@test.com"),
                    onEmailChange = {},
                    onLoginClick = { loginClicked = true }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithTag("loginButton")
            .assertIsEnabled()

        // When
        composeTestRule
            .onNodeWithTag("loginButton")
            .performClick()

        // Then
        assert(loginClicked)
    }

    @Test
    fun loginScreen_longEmail_scrollsAndDisplays() {
        // Given
        val longEmail = "very.long.email.address.that.might.need.scrolling@example.com"
        
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(email = longEmail),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loginEmailField")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_loadingWithEmail_buttonDisabled() {
        // Given
        composeTestRule.setContent {
            TaskSyncTheme {
                LoginContent(
                    uiState = LoginUiState(
                        email = "test@example.com",
                        isLoading = true
                    ),
                    onEmailChange = {},
                    onLoginClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loginButton")
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag("loginEmailField")
            .assertIsNotEnabled()
    }
}
