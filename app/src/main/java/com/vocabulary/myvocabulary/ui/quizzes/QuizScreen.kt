package com.vocabulary.myvocabulary.ui.quizzes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vocabulary.myvocabulary.R
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
    failedOnly: Boolean,
    onQuizFinished: (Long, Int, Int) -> Unit,
    onUpdateFab: (@Composable () -> Unit) -> Unit,
    onExit: () -> Unit,
    onRegisterExitLogic: (() -> Unit) -> Unit,
) {
    val quizViewModel: QuizViewModel = koinViewModel {
        parametersOf(dictionaryId, direction, failedOnly)
    }

    val resultViewModel: ResultViewModel = koinViewModel {
        parametersOf(dictionaryId, direction)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(dictionaryId, quizType) {
        quizViewModel.fetchQuizList()
        quizViewModel.startQuiz(quizType.toQuizType(), dictionaryId)
    }

    val quizState by quizViewModel.quizUiState.collectAsStateWithLifecycle()
    val snackBarMessage = stringResource(R.string.empty_list_snackbar)
    val snackBarErrorMessage = stringResource(R.string.snack_bar_error)

    val handleExit = {
        resultViewModel.resetGuessedWordCollections()
        resultViewModel.dispose()
        quizViewModel.clearList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            when (val state = quizState) {
                is QuizUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is QuizUiState.SuccessList -> {
                    QuizScreenContent(
                        direction = direction,
                        onGuessSaved = { id, guess ->
                            resultViewModel.latestGuess(lastGuess = GuessedWord(id, guess))
                        },
                        onListFinished = {
                            onQuizFinished(dictionaryId, direction, quizType)
                        },
                        onUpdateFab = onUpdateFab,
                        state = state,
                        onNextClicked = {
                            quizViewModel.onNextClicked()
                        },
                        onGuessChanged = { guess -> quizViewModel.onGuessChanged(guess) }
                    )
                }

                is QuizUiState.EmptyList -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            message = snackBarMessage,
                            duration = SnackbarDuration.Short
                        )
                        handleExit()
                        onExit()
                    }
                }

                is QuizUiState.Error -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            message = snackBarErrorMessage,
                            duration = SnackbarDuration.Short
                        )
                        handleExit()
                        onExit()
                    }
                }
            }
        }
    }

// Pass handleExit logic up to the NavHost
    LaunchedEffect(Unit) {
        onRegisterExitLogic {
            handleExit()
        }
    }

    BackHandler(enabled = true) {
        handleExit()
        onExit()
    }
}

@Composable
fun QuizScreenContent(
    direction: Int,
    onGuessSaved: (Long, String) -> Unit,
    onListFinished: () -> Unit,
    onUpdateFab: (@Composable () -> Unit) -> Unit,
    state: QuizUiState.SuccessList,
    onNextClicked: () -> Unit,
    onGuessChanged: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.rollingIndex) {
        if (state.rollingIndex > 0) {
            listState.animateScrollToItem(state.rollingIndex - 1)
        }

    }

    LaunchedEffect(state) {
        onUpdateFab {
            FabMenu(
                onNextClicked = {
                    val guessToSave = state.currentGuess.trim()
                    val idToSave = state.currentFocusedWordId
                    onGuessSaved(idToSave, guessToSave)

                    if(state.isFabIconNext) {
                        onNextClicked()
                    } else {
                        onListFinished()
                    }
                },
                iconToDisplay = {
                    if (state.isFabIconNext) Icons.AutoMirrored.Filled.ArrowForward
                    else Icons.Default.Check
                }
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy((-80).dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(
                count = state.rollingIndex,
                key = { index -> state.quizList.getOrNull(index)?.wordId ?: "fallback_$index" }
            ) { index ->
                val word = state.quizList.getOrNull(index)
                if (word != null) {
                    val isThisCardActive = remember(word.wordId, state.currentFocusedWordId) {
                        word.wordId == state.currentFocusedWordId
                    }
                    FocusCard(
                        modifier = Modifier
                            .fillMaxWidth(),
                        word = word,
                        isActive = isThisCardActive,
                        editTextContent = { newText ->
                            if (isThisCardActive) {
                                onGuessChanged(newText)
                            }
                        },
                        askTranslation = direction.toDirectionType() == QuizDirectionType.AskTranslation,
                        initialText = state.currentGuess
                    )
                }
            }
            item {
                Spacer(
                    modifier = Modifier.height(300.dp)
                )
            }
        }
    }

}

@Composable
fun FocusCard(
    modifier: Modifier = Modifier,
    word: Word,
    editTextContent: (String) -> Unit,
    isActive: Boolean,
    askTranslation: Boolean,
    initialText: String
) {
    val editState = rememberTextFieldState(initialText)
    val focusRequester = remember { FocusRequester() }
    val question = if (askTranslation) {
        word.translation
    } else {
        word.word
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            focusRequester.requestFocus()
        }
    }
    LaunchedEffect(editState.text) {
        if (isActive) {
            editTextContent(editState.text.toString())
        }
    }

//    AnimatedVisibility(
//        visible = isVisible,
//        enter = slideInVertically(
//            initialOffsetY = { fullHeight -> fullHeight },
//            animationSpec = tween(durationMillis = 500)
//        ) + fadeIn(animationSpec = tween(durationMillis = 500)),
//        modifier = Modifier.fillMaxSize()
//    ) {
//    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.PaddingLarge),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .alpha((if (isActive) 1f else 0f)),
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
                    enabled = isActive,
                    placeholder = { Text(stringResource(R.string.quiz_hint_enter_solution)) },
                )
            }
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
        direction = 0,
        onGuessSaved = { _, _ -> },
        onListFinished = { },
        onUpdateFab = { },
        onNextClicked = { },
        state = QuizUiState.SuccessList(previewWords),
        onGuessChanged = { _ -> }
    )
}