package com.vocabulary.myvocabulary.ui.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.theme.MyVocabularyTheme
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.Calendar

@Composable
fun WordDetailsBottomSheet(
    clickedWord: Word,
    onDismissRequest: (isSheetOpen: Boolean) -> Unit,
    showEditDialog: (Boolean) -> Unit,
    showDelete: (Boolean) -> Unit
) {
    WordDetailsBottomSheetContent(
        clickedWord = clickedWord,
        onDismissRequest = onDismissRequest,
        showEditDialog = { showEditDialog(it) },
        showDeleteDialog = { showDelete(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailsBottomSheetContent(
    clickedWord: Word,
    onDismissRequest: (isSheetOpen: Boolean) -> Unit,
    showEditDialog: (Boolean) -> Unit,
    showDeleteDialog: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismissRequest( false) }
    ) {
        Column(
            modifier = Modifier.height(IntrinsicSize.Min)
                .padding(MaterialTheme.dimens.PaddingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(2f)
                        .align(Alignment.CenterVertically),
                    text = stringResource(R.string.expression_colon)
                )

                IconButton(
                    onClick = { showEditDialog(true) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Word edit icon"
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog(true) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete word icon"
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
            ) {
                Text(text = clickedWord.word,
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge)
                )
            }

            Spacer(modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium))

            Text(text = stringResource(R.string.translation_colon))

            Spacer(modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
            ) {
                if (clickedWord.translation.isEmpty()) {
                    Text(text = stringResource(R.string.word_list_meaning), color = Color.Gray, modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge))
                } else {
                    Text(text = clickedWord.translation,
                        modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(MaterialTheme.dimens.PaddingLarge)
                ) {
                    WordInformationRow(
                        title = stringResource(R.string.word_detail_attempts_so_far),
                        value = clickedWord.beenAsked.toString()
                    )

                    WordInformationRow(
                        title = stringResource(R.string.passed_colon),
                        value = clickedWord.passed.toString()
                    )

                    WordInformationRow(
                        title = stringResource(R.string.failed_colon),
                        value = clickedWord.failed.toString()
                    )

                    WordInformationRow(
                        title = stringResource(R.string.last_result_colon),
                        value = clickedWord.lastResult.toString()
                    )
                    
                    WordInformationRow(
                        title = stringResource(R.string.details_created),
                        value = DateTypeConverter().formatDate(clickedWord.created)
                    )
                }
            }
        }
    }
}

@Composable
fun WordInformationRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Text(value)
    }
}

@Preview
@Composable
fun WordDetailsBottomSheetPreview() {
    val previewWord = Word(1, 1, "new", "", 0, 0, 0, Calendar.getInstance().time)

    MyVocabularyTheme() {
        WordDetailsBottomSheetContent(
            clickedWord = previewWord,
            onDismissRequest = { },
            showEditDialog = { _ -> },
            showDeleteDialog = {_ -> }
        )
    }

}