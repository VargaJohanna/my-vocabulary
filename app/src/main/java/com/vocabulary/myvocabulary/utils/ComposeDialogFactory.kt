package com.vocabulary.myvocabulary.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.theme.dimens

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
    fun BuildDeleteDialog(
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

    @Composable
    fun BuildCreateWordDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (newExpression: String, newTranslation: String) -> Unit,
        onAddMore: (newExpression: String, newTranslation: String) -> Unit,
        dialogTitle: String
    ) {
        val newWordState = rememberTextFieldState()
        val newTranslationState = rememberTextFieldState()
        var showError by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        ThirdButtonAlertDialog(
            title = { Text(text = dialogTitle) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        state = newWordState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimens.PaddingMedium),
                        label = { Text(stringResource(R.string.create_expression_hint)) },
                        isError = showError,
                        supportingText = {
                            if (showError) {
                                Text(text = stringResource(R.string.please_enter_expression))
                            }
                        },
                        inputTransformation = { showError = false },
                    )

                    OutlinedTextField(
                        state = newTranslationState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimens.PaddingMedium),
                        label = { Text(stringResource(R.string.create_translation_hint)) },
                    )
                }
            },
            onDismissRequest = { onDismissRequest() },
            neutralButton = {
                TextButton(
                    onClick = {
                        if (newWordState.text.isEmpty()) {
                            showError = true
                        } else {
                            onAddMore(
                                newWordState.text.toString(),
                                newTranslationState.text.toString()
                            )
                            showError = false
                            newWordState.edit { this.delete(0, this.length) }
                            newTranslationState.edit { this.delete(0, this.length) }
                            focusRequester.requestFocus()
                        }
                    }
                ) {
                    Text(stringResource(R.string.add_more_button_label))
                }
            },
            negativeButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel_button_label))
                }
            },
            positiveButton = {
                TextButton(
                    onClick = {
                        if (newWordState.text.isEmpty()) {
                            showError = true
                        } else {
                            onConfirmation(
                                newWordState.text.toString(),
                                newTranslationState.text.toString()
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.save_button_label))
                }
            }
        )
    }

    @Composable
    fun ThirdButtonAlertDialog(
        onDismissRequest: () -> Unit,
        positiveButton: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        negativeButton: @Composable (() -> Unit)? = null,
        neutralButton: @Composable (() -> Unit)? = null,
        icon: @Composable (() -> Unit)? = null,
        title: @Composable (() -> Unit)? = null,
        text: @Composable (() -> Unit)? = null,
        shape: Shape = AlertDialogDefaults.shape,
        containerColor: Color = AlertDialogDefaults.containerColor,
        iconContentColor: Color = AlertDialogDefaults.iconContentColor,
        titleContentColor: Color = AlertDialogDefaults.titleContentColor,
        textContentColor: Color = AlertDialogDefaults.textContentColor,
        tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
        properties: DialogProperties = DialogProperties()
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            confirmButton = { positiveButton() },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    negativeButton?.let {
                        it()
                    }

                    neutralButton?.let {
                        it()
                    }
                }
            },
            icon = icon,
            title = title,
            text = text,
            shape = shape,
            containerColor = containerColor,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            tonalElevation = tonalElevation,
            properties = properties
        )
    }

    @Composable
    fun BuildEditWordDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (newExpression: String, newTranslation: String) -> Unit,
        expression: String,
        translation: String
    ) {
        val editExpressionState = rememberTextFieldState(expression)

        val editTranslationState = rememberTextFieldState(translation)
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            title = {
                Text(text = stringResource(R.string.edit_word_dialog_title))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        state = editExpressionState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimens.PaddingMedium),
                        label = { Text(stringResource(R.string.word_list_expression)) },
                        isError = showError,
                        supportingText = {
                            if (showError) {
                                Text(text = stringResource(R.string.please_enter_expression))
                            }
                        },
                        inputTransformation = { showError = false },
                    )

                    OutlinedTextField(
                        state = editTranslationState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimens.PaddingMedium),
                        label = {
                            if (editTranslationState.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.word_list_meaning),
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.word_list_meaning),
                                    color = Color.Black
                                )
                            }
                        }
                    )
                }
            },
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editExpressionState.text.isEmpty()) {
                            showError = true
                        } else {
                            onConfirmation(
                                editExpressionState.text.toString(),
                                editTranslationState.text.toString()
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.save_button_label))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(stringResource(R.string.cancel_button_label))
                }
            }
        )
    }

    @Composable
    fun BuildInfoDialog(
        onDismissRequest: () -> Unit,
        dialogTitle: String,
        dialogText: String
    ) {
        AlertDialog(
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(stringResource(R.string.info_dialog))
                }
            }
        )

    }

    @Composable
    fun BuildChooseDirectionDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (selectedOption: Int) -> Unit
    ) {
        var option by remember { mutableStateOf(0) }
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.dialog_pick_direction))
            },
            text = {
                val radioOptions = listOf(
                    stringResource(R.string.word_list_meaning),
                    stringResource(R.string.word_list_expression)
                )
                val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    radioOptions.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = {
                                        onOptionSelected(text)
                                        option = if (text == radioOptions[0]) 0 else 1
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(MaterialTheme.dimens.PaddingLarge),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null
                            )

                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = MaterialTheme.dimens.PaddingMedium)
                            )
                        }
                    }
                }
            },
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                TextButton(
                    onClick = { onConfirmation(option) }
                ) {
                    Text(stringResource(R.string.dialog_lets_do_it))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(stringResource(R.string.cancel_button_label))
                }
            }
        )
    }

    @Composable
    fun BuildCustomQuizSizeDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (size: Int) -> Unit
    ) {
        val editedSize = rememberTextFieldState()
        var showError by remember { mutableStateOf(false) }
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.custom_dialog_title))
            },
            text = {
                OutlinedTextField(
                    state = editedSize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text(stringResource(R.string.add_a_number)) },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text(text = stringResource(R.string.custom_dialog_error))
                        }
                    },
                    inputTransformation = { showError = false },
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedSize.text.isEmpty()) {
                            showError = true
                        } else {
                            try {
                                if(editedSize.text.toString().toInt() > 0) {
                                    onConfirmation(editedSize.text.toString().toInt())
                                } else {
                                        showError = true
                                }
                            } catch (e: NumberFormatException) {
                                showError = true
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.save_button_label))
                }
            }
        )
    }
}

@Preview
@Composable
fun BuildChooseDirectionDialogPreview() {
    val factory = ComposeDialogFactory()

    factory.BuildChooseDirectionDialog(
        onDismissRequest = {},
        onConfirmation = {}
    )
}

@Preview
@Composable
fun ThirdButtonAlertDialogWithNeutralPreview() {
    val factory = ComposeDialogFactory()
    val newWordState = rememberTextFieldState()
    val newTranslationState = rememberTextFieldState()
    var showError by remember { mutableStateOf(false) }
    factory.ThirdButtonAlertDialog(
        title = { Text("Create") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    state = newWordState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.dimens.PaddingMedium),
                    label = { Text("new expression") },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("new translation")
                        }
                    },
                    inputTransformation = { showError = false },
                )

                OutlinedTextField(
                    state = newTranslationState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.dimens.PaddingMedium),
                    label = { Text(stringResource(R.string.create_translation_hint)) },
                )
            }
        },
        onDismissRequest = { },
        neutralButton = {
            TextButton(
                onClick = {}
            ) {
                Text(stringResource(R.string.add_more_button_label))
            }
        },
        negativeButton = {
            TextButton(
                onClick = {}
            ) {
                Text(stringResource(R.string.cancel_button_label))
            }
        },
        positiveButton = {
            TextButton(
                onClick = {}
            ) {
                Text(stringResource(R.string.save_button_label))
            }
        }
    )
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