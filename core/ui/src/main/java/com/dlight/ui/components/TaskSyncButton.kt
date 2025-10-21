package com.dlight.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

enum class ButtonType {
    PRIMARY,
    SECONDARY,
    TEXT
}

@Composable
fun TaskSyncButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonType: ButtonType = ButtonType.PRIMARY,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge
) {
    when (buttonType) {
        ButtonType.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.animateContentSize(),
                enabled = enabled && !isLoading,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    textStyle = textStyle,
                    progressColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        ButtonType.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.animateContentSize(),
                enabled = enabled && !isLoading,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    textStyle = textStyle,
                    progressColor = MaterialTheme.colorScheme.primary
                )
            }
        }
        ButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.animateContentSize(),
                enabled = enabled && !isLoading,
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    textStyle = textStyle,
                    progressColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    textStyle: TextStyle,
    progressColor: Color
) {
    if (isLoading) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = progressColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = textStyle
            )
        }
    } else {
        Text(
            text = text,
            style = textStyle
        )
    }
}
