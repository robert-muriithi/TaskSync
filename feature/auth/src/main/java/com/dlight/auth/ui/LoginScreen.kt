package com.dlight.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlight.auth.R
import com.dlight.ui.components.ButtonType
import com.dlight.ui.components.TaskSyncButton
import com.dlight.ui.components.TaskSyncTextField
import com.dlight.ui.components.TextFieldType

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess : () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.testTag("loginErrorSnackbar")
            ) 
        },
        modifier = modifier.testTag("loginScreen")
    ) { paddingValues ->
        LoginContent(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onLoginClick = viewModel::onLoginClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_welcome),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .testTag("loginWelcomeText")
        )

        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .testTag("loginSubtitle")
        )

        TaskSyncTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("loginEmailField"),
            label = stringResource(R.string.login_email_label),
            placeholder = stringResource(R.string.login_email_placeholder),
            leadingIcon = Icons.Default.Email,
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError,
            enabled = !uiState.isLoading,
            singleLine = true,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            textFieldType = TextFieldType.OUTLINED
        )

        TaskSyncButton(
            text = stringResource(R.string.login_button),
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("loginButton"),
            enabled = uiState.email.isNotBlank() && !uiState.isLoading,
            isLoading = uiState.isLoading,
            buttonType = ButtonType.PRIMARY
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    Scaffold(
        snackbarHost = {},
        modifier = Modifier
    ) { paddingValues ->
        LoginContent(
            uiState = LoginUiState(
                email = ""
            ),
            onEmailChange = {},
            onLoginClick = {},
            modifier = Modifier.padding(paddingValues)
        )
    }
}
