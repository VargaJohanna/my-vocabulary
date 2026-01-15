package com.vocabulary.myvocabulary.ui.quizzes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.ProvideAppBarTitle
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.ui.words.Word
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Calendar

@Composable
fun QuizScreen(
    quizType: Int,
    dictionaryId: Long,
    direction: Int,
    failedOnly: Boolean
) {

    val quizViewModel: QuizViewModel = koinViewModel {
        parametersOf(dictionaryId, direction, failedOnly)
    }

    val resultViewModel: ResultViewModel = koinViewModel {
        parametersOf(dictionaryId)
    }

    LaunchedEffect(dictionaryId, quizType) {
        quizViewModel.fetchQuizList()
        quizViewModel.startQuiz(quizType.toQuizType(), dictionaryId)
    }
    val quizList by quizViewModel.quizList.collectAsState()

    QuizScreenContent(
        dictionaryId, direction, failedOnly, quizList,
        onGuessSaved = { id, guess ->
            resultViewModel.latestGuess(lastGuess = GuessedWord(id, guess))
        },
        onListFinished = {})
}

@Composable
fun QuizScreenContent(
    dictionaryId: Long,
    direction: Int,
    failedOnly: Boolean,
    quizList: List<Word>,
    onGuessSaved: (Long, String) -> Unit,
    onListFinished: () -> Unit
) {
    ProvideAppBarTitle { Text(stringResource(R.string.quiz_toolbar)) }
    //rollingIndex: looping through the quizList so the cards can be shown one by one
    var rollingIndex by rememberSaveable { mutableStateOf(1) }
    //guessContent: the given answer by the user
    var guessContent by rememberSaveable { mutableStateOf("") }
    //isFabIconNext: a boolean to know if the FAB should be a next icon or a tick icon.
    // When the list is finished, then the FAB should be a tick icon
    var isFabIconNext by rememberSaveable { mutableStateOf(true) }
    //nextClicked: a boolean to know if the next button was clicked
    var nextClicked by rememberSaveable { mutableStateOf(false) }
    //focusedWordId: the id of the word that is actually in focus
    var focusedWordId by rememberSaveable { mutableStateOf(0L) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FabMenu(
                onNextClicked = { nextClicked = true },
                iconToDisplay = {
                    if (isFabIconNext) {
                        Icons.AutoMirrored.Filled.ArrowForward
                    } else {
                        Icons.Default.Check
                    }
                }
            )
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy((-90).dp)
        ) {
            if (quizList.isNotEmpty()) {
                val currentFocusedId = quizList.getOrNull(rollingIndex - 1)?.wordId ?: 0L
                quizList.subList(0, rollingIndex).toList().forEach { word ->
                    val isThisCardHidden = word.wordId != currentFocusedId
                    FocusCard(
                        word = word,
                        hideMe = isThisCardHidden,
                        editTextContent = {
                            if (isThisCardHidden.not()) {
                                guessContent = it
                                focusedWordId = word.wordId
                            }
                        },
                        askTranslation = direction.toDirectionType() == QuizDirectionType.AskTranslation
                    )
                }
            } else {
//                CircularProgressIndicator()
            }
        }
    }

    //When the last word is in focus then the FAB must be a tick icon.
    if (rollingIndex == quizList.size) {
        isFabIconNext = false
    }

    if (nextClicked) {
        nextClicked = false

        if (rollingIndex < quizList.size) {
            rollingIndex++
            isFabIconNext = true
            onGuessSaved(focusedWordId, guessContent)
            guessContent = ""
        } else {
            onGuessSaved(focusedWordId, guessContent)
            onListFinished()
        }
    }
}

@Composable
fun FocusCard(
    word: Word,
    editTextContent: (String) -> Unit,
    //hideMe: hide the content of the previous cards when a new card is called
    hideMe: Boolean,
    askTranslation: Boolean
) {
    val editState = rememberTextFieldState("")
    val focusRequester = remember { FocusRequester() }
    val question = if (askTranslation) { word.translation } else { word.word }

    LaunchedEffect(hideMe) {
        if (!hideMe) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(editState.text) {
        editTextContent(editState.text.toString())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.PaddingLarge),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.dimens.CardElevationSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .alpha((if (hideMe) 0f else 1f)),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingLarge)
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                text = question,
            )

            VerticalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium),
            )

            TextField(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingLarge)
                    .weight(1f)
                    .focusRequester(focusRequester),
                state = editState,
                placeholder = { Text(stringResource(R.string.quiz_hint_enter_solution)) },
            )
        }
    }
}

@Composable
fun FabMenu(
    onNextClicked: () -> Unit,
    iconToDisplay: () -> ImageVector,
) {
    FloatingActionButton(
        modifier = Modifier.imePadding(),
        onClick = { onNextClicked() }
    ) {
        Icon(
            imageVector = iconToDisplay(),
            contentDescription = stringResource(R.string.quiz_fab_next),
        )
    }
}

@Preview
@Composable
fun QuizScreenPreview() {
    val previewWords = listOf(
        Word(1, 1, "new", "novus", 0, 0, 0, Calendar.getInstance().time),
        Word(2, 1, "body", "corpus", 0, 0, 0, Calendar.getInstance().time),
        Word(3, 1, "day", "diem", 0, 0, 0, Calendar.getInstance().time)
    )

    QuizScreenContent(
        dictionaryId = 1,
        direction = 0,
        failedOnly = false,
        quizList = previewWords,
        onGuessSaved = { _, _ -> },
        onListFinished = {}
    )
}