package com.dlight.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

enum class TextFieldType {
    FILLED,
    OUTLINED
}

@Composable
fun TaskSyncTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textFieldType: TextFieldType = TextFieldType.OUTLINED
) {
    when (textFieldType) {
        TextFieldType.OUTLINED -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                label = label?.let { { Text(it) } },
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = leadingIcon?.let {
                    {
                        Icon(
                            imageVector = it,
                            contentDescription = null
                        )
                    }
                },
                trailingIcon = when {
                    trailingIcon != null -> {
                        {
                            if (onTrailingIconClick != null) {
                                IconButton(onClick = onTrailingIconClick) {
                                    Icon(
                                        imageVector = trailingIcon,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = trailingIcon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    else -> null
                },
                isError = isError,
                supportingText = errorMessage?.let {
                    {
                        if (isError) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = keyboardActions,
                colors = OutlinedTextFieldDefaults.colors()
            )
        }
        TextFieldType.FILLED -> {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                label = label?.let { { Text(it) } },
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = leadingIcon?.let {
                    {
                        Icon(
                            imageVector = it,
                            contentDescription = null
                        )
                    }
                },
                trailingIcon = when {
                    trailingIcon != null -> {
                        {
                            if (onTrailingIconClick != null) {
                                IconButton(onClick = onTrailingIconClick) {
                                    Icon(
                                        imageVector = trailingIcon,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = trailingIcon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    else -> null
                },
                isError = isError,
                supportingText = errorMessage?.let {
                    {
                        if (isError) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.colors()
            )
        }
    }
}
