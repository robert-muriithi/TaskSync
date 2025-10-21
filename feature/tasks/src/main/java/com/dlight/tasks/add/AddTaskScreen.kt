package com.dlight.tasks.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dlight.tasks.R
import com.dlight.ui.components.ButtonType
import com.dlight.ui.components.TaskSyncButton
import com.dlight.ui.components.TaskSyncTextField
import com.dlight.ui.components.TextFieldType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isTaskCreated) {
        if (uiState.isTaskCreated) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.add_task_title),
                        modifier = Modifier.testTag("addTaskTitle")
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("addTaskBackButton")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                },
                modifier = Modifier.testTag("addTaskTopBar")
            )
        },
        snackbarHost = { 
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.testTag("addTaskSnackbar")
            ) 
        },
        modifier = modifier.testTag("addTaskScreen")
    ) { padding ->
        AddTaskContent(
            uiState = uiState,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onSaveClick = viewModel::onSaveClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun AddTaskContent(
    uiState: AddTaskUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("addTaskContent"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TaskSyncTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("addTaskTitleField"),
            label = stringResource(R.string.add_task_title_label),
            placeholder = stringResource(R.string.add_task_title_placeholder),
            isError = uiState.titleError != null,
            errorMessage = uiState.titleError,
            enabled = !uiState.isLoading,
            singleLine = true,
            imeAction = ImeAction.Next,
            textFieldType = TextFieldType.OUTLINED
        )

        TaskSyncTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("addTaskDescriptionField"),
            label = stringResource(R.string.add_task_description_label),
            placeholder = stringResource(R.string.add_task_description_placeholder),
            enabled = !uiState.isLoading,
            singleLine = false,
            imeAction = ImeAction.Done,
            textFieldType = TextFieldType.OUTLINED
        )

        Spacer(modifier = Modifier.weight(1f))

        TaskSyncButton(
            text = stringResource(R.string.add_task_save),
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("addTaskSaveButton"),
            enabled = uiState.title.isNotBlank() && !uiState.isLoading,
            isLoading = uiState.isLoading,
            buttonType = ButtonType.PRIMARY
        )
    }
}
