package com.vocabulary.myvocabulary.ui.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.vocabulary.myvocabulary.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocabulary.myvocabulary.navigation.ProvideAppBarTitle
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WordListScreen(
    dictionaryId: Long,
    dictionaryName: String
) {
    val viewModel: WordListViewModel = koinViewModel(
        parameters = { parametersOf(dictionaryId) }
    )
    val dialogFactory: ComposeDialogFactory = koinInject()

    LaunchedEffect(Unit) {
        viewModel.fetchWordList()
    }

    val wordList by viewModel.wordList.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    ProvideAppBarTitle { Text(dictionaryName) }

    Scaffold(
        floatingActionButton = {
            FabMenu(onShowCreateDialog = { showCreateDialog = true })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.PaddingMedium)
            ) {
                Text(
                    text = stringResource(R.string.word_list_expression),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.word_list_meaning),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

            }
            WordLazyList(wordList.first)
        }
    }

    if(showCreateDialog) {
        dialogFactory.BuildCreateWordDialog(
            dialogTitle = stringResource(R.string.create_new_word_dialog_title),
            onDismissRequest = {
                showCreateDialog = false
            },
            onConfirmation = {newWord, newTranslation ->
                viewModel.insertWord(viewModel.createWordObject(newWord, newTranslation))
                showCreateDialog = false
            },
            onAddMore = { newWord, newTranslation ->
                viewModel.insertWord(viewModel.createWordObject(newWord, newTranslation))
            }
        )
    }
}

@Composable
fun FabMenu(onShowCreateDialog: () -> Unit) {
    FloatingActionButton(
        onClick = { onShowCreateDialog() }
    ){
        Icon(
            imageVector =  Icons.Default.Add,
            contentDescription = stringResource(R.string.dictionary_fab_description),
        )
    }
}

@Composable
fun WordLazyList(
    list: List<Word>
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.dimens.PaddingMedium)
    ) {
        items(list) {items ->
            WordCard(modifier = Modifier, expression = items.word, translation = items.translation)
        }
    }

}
@Composable
fun WordCard(
    modifier: Modifier,
    expression: String,
    translation: String
) {
    Card(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.PaddingMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge),
                    text = expression,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            VerticalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium)
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingLarge),
                    text = translation,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun WordListScreenPreview() {
    WordListScreen(1L, "Test")
}