package com.vocabulary.myvocabulary.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocabulary.myvocabulary.R

class ComposeDialogFactory {
    @Composable
    fun BuildCreateDictionaryDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (newTitle: String) -> Unit,
        dialogTitle: String
    ) {
        val newDictionaryTitleState = rememberTextFieldState()
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            title = {
                Text(text = dialogTitle)
            },
            text = {
                OutlinedTextField(
                    state = newDictionaryTitleState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text(stringResource(R.string.create_dictionary_hint)) },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text(text = stringResource(R.string.please_enter_a_title))
                        }
                    },
                    inputTransformation = { showError = false },
                    lineLimits = TextFieldLineLimits.SingleLine
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newDictionaryTitleState.text.isEmpty()) {
                            showError = true
                        } else {
                            onConfirmation(newDictionaryTitleState.text.toString())
                        }
                    }
                ) {
                    Text(stringResource(R.string.create_button_label))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel_button_label))
                }
            }
        )
    }

    @Composable
    fun BuildDeleteDictionaryDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
        dialogTitle: String,
        message: String
    ) {
        AlertDialog(
            title = {
                Text(text = dialogTitle)
            },
            text = {
                Text(message)
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(stringResource(R.string.dictionary_menu_delete))
                }
            }
        )
    }

    @Composable
    fun BuildRenameDictionaryDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (editedTitle: String) -> Unit,
        dialogTitle: String
    ) {
        val editedTitleState = rememberTextFieldState()
        var showError by remember { mutableStateOf(false) }
        AlertDialog(
            title = {
                Text(text = dialogTitle)
            },
            text = {
                OutlinedTextField(
                    state = editedTitleState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text(stringResource(R.string.rename_dictionary_hint)) },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text(text = stringResource(R.string.please_enter_a_title))
                        }
                    },
                    inputTransformation = { showError = false },
                    lineLimits = TextFieldLineLimits.SingleLine
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedTitleState.text.isEmpty()) {
                            showError = true
                        } else {
                            onConfirmation(editedTitleState.text.toString())
                        }
                    }
                ) {
                    Text(stringResource(R.string.create_button_label))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel_button_label))
                }
            }
        )
    }
}

@Preview
@Composable
fun DictionaryDialogPreview() {
    val newDictionaryTitleState = rememberTextFieldState()
    AlertDialog(
        title = { Text("Create Dictionary") },
        text = {
            OutlinedTextField(
                state = newDictionaryTitleState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text(stringResource(R.string.create_dictionary_hint)) }
            )
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {}
            ) { Text(stringResource(R.string.create_button_label)) }
        },
        dismissButton = {
            TextButton(
                onClick = {}
            ) { Text(stringResource(R.string.cancel_button_label)) }
        }
    )
}

@Preview
@Composable
fun DeleteDialogPreview() {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.dictionary_menu_delete))
        },
        text = {
            Text(stringResource(R.string.verify_deletion))
        },
        onDismissRequest = {

        },
        confirmButton = {
            TextButton(
                onClick = {

                }
            ) {
                Text(stringResource(R.string.dictionary_menu_delete))
            }
        }
    )
}