package com.vocabulary.myvocabulary.ui.quizzes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import kotlinx.coroutines.MainScope
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

@Composable
fun DictionaryPickerBottomSheet(
//    selectedQuiz: QuizTypes,
    onDismissRequest: (isSheetOpen: Boolean) -> Unit,
) {
    val viewModel: DictionaryListViewModel = koinViewModel()
    val dialogFactory: ComposeDialogFactory = koinInject()

    LaunchedEffect(Unit) {
        viewModel.fetchDictionaries()
    }
    val dictionaryList by viewModel.dictionaries.collectAsState()
    DictionaryPickerContent(
        dictionaryList = dictionaryList,
        onDismissRequest = onDismissRequest,
        showQuizDirectionDialog = false
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryPickerContent(
    dictionaryList: List<Dictionary>,
    onDismissRequest: (isSheetOpen: Boolean) -> Unit,
    showQuizDirectionDialog: Boolean
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismissRequest(false) },
    ) {
        Text(text = stringResource(R.string.quiz_dictionary_picker_title),
            modifier = Modifier.padding(MaterialTheme.dimens.PaddingSmall)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge)
        
        LazyColumn(
            modifier = Modifier
                .padding(MaterialTheme.dimens.PaddingMedium)
        ) {
            items(dictionaryList) { item ->
                DictionaryCard(item)
            }
        }
    }
}

@Composable
fun DictionaryCard(
    item: Dictionary
) {
    Card(
        onClick = {},
        modifier = Modifier
            .padding(MaterialTheme.dimens.PaddingMedium
            )
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
    ) {
        Text(
            text = item.dictionaryName,
            modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge)
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
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 2L,
            dictionaryName = "Test2",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 3L,
            dictionaryName = "Test3",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 4L,
            dictionaryName = "Test4",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test5",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test6",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test7",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test8",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test9",
            dictionaryCreated = Calendar.getInstance().time
        ),
        Dictionary(
            dictionaryId = 5L,
            dictionaryName = "Test10",
            dictionaryCreated = Calendar.getInstance().time
        )
    )

    MaterialTheme {
        DictionaryPickerContent(
            dictionaryList = previewList,
            onDismissRequest = {},
            showQuizDirectionDialog = false
        )
    }
}