package com.vocabulary.myvocabulary.ui.quizzes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.ui.theme.dimens
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

@Composable
fun DictionaryPickerBottomSheet(
    onDismissRequestBottomSheet: (isSheetOpen: Boolean) -> Unit,
    selectedDictionaryId: (id: Long) -> Unit,
    showDialog: (isDialogOpen: Boolean) -> Unit,
) {
    val viewModel: DictionaryListViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.fetchDictionaries()
    }
    val dictionaryList by viewModel.dictionaries.collectAsState()
    DictionaryPickerContent(
        dictionaryList = dictionaryList,
        onDismissRequestBottomSheet = onDismissRequestBottomSheet,
        showQuizDirectionDialog = { showDialog(it) },
        onSelectedDictionary = { id ->
            selectedDictionaryId(id)
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryPickerContent(
    dictionaryList: List<Dictionary>,
    onDismissRequestBottomSheet: (isSheetOpen: Boolean) -> Unit,
    showQuizDirectionDialog: (isDialogOpen: Boolean) -> Unit,
    onSelectedDictionary: (id: Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()


    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismissRequestBottomSheet(false) },
    ) {
        Text(
            text = stringResource(R.string.quiz_dictionary_picker_title),
            modifier = Modifier
                .padding(MaterialTheme.dimens.PaddingSmall)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(MaterialTheme.dimens.PaddingMedium)
        ) {
            items(dictionaryList) { item ->
                DictionaryCard(item, onSelectedDictionary, showQuizDirectionDialog)
            }
        }
    }
}

@Composable
fun DictionaryCard(
    item: Dictionary,
    onSelect: (id: Long) -> Unit,
    showQuizDirectionDialog: (isDialogOpen: Boolean) -> Unit
) {
    Card(
        onClick = {
            onSelect(item.dictionaryId)
            showQuizDirectionDialog(true)
        },
        modifier = Modifier
            .padding(
                MaterialTheme.dimens.PaddingMedium
            )
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
    ) {
        Text(
            text = item.dictionaryName,
            modifier = Modifier
                .padding(MaterialTheme.dimens.PaddingLarge)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
fun DictionaryPickerPreview() {
    val previewList = listOf(
        Dictionary(
            dictionaryId = 1L,
            dictionaryName = "Test",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 2L,
            dictionaryName = "Test2",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 3L,
            dictionaryName = "Test3",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 4L,
            dictionaryName = "Test4",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test5",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test6",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test7",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test8",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test9",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test10",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null
        )
    )

    MaterialTheme {
        DictionaryPickerContent(
            dictionaryList = previewList,
            onDismissRequestBottomSheet = {},
            showQuizDirectionDialog = { },
            onSelectedDictionary = {}
        )
    }
}