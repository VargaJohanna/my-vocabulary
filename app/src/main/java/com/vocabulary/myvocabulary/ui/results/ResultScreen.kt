package com.vocabulary.myvocabulary.ui.results

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.navigation.FabConfiguration
import com.vocabulary.myvocabulary.ui.lottie.FailedAnimation
import com.vocabulary.myvocabulary.ui.lottie.SuccessAnimation
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toQuizType
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.ui.words.Word
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Calendar

@Composable
fun ResultScreen(
    dictionaryId: Long,
    direction: Int,
    quizType: Int,
    onRestartQuiz: (quizType: Int, dictionaryId: Long, direction: Int, failedOnly: Boolean) -> Unit,
    onExit: () -> Unit,
    onUpdateFab: (FabConfiguration) -> Unit,
    onBackClick: (() -> Unit) -> Unit
) {

    val resultViewModel: ResultViewModel = koinViewModel {
        parametersOf(dictionaryId, direction)
    }

    val resultList by resultViewModel.getGuessedList().collectAsState()
    val numOfPassed by resultViewModel.getNumOfPassed().collectAsState()
    val numOfWords = resultList.size
    val resultPercentage by resultViewModel.getResultPercentage().collectAsState()

    val handleExit = {
        resultViewModel.resetGuessedWordCollections()
        resultViewModel.dispose()
        onExit()
    }

    BackHandler(enabled = true) {
        handleExit()
    }

    LaunchedEffect(Unit) {
        resultViewModel.fetchGuessedList()
        onBackClick {
            handleExit()
        }
    }

    LaunchedEffect(Unit) {
        resultViewModel.fetchGuessedList()
    }

    ResultScreenContent(
        resultList = resultList,
        directionType = direction.toDirectionType(),
        onExit = handleExit,
        onRestartNew = {
            resultViewModel.resetGuessedWordCollections()
            resultViewModel.startNew(dictionaryId, quizType.toQuizType())
            resultViewModel.dispose()
            onRestartQuiz(quizType, dictionaryId, direction, false)
        },
        onRestartFailedOnly = {
            resultViewModel.resetGuessedWordCollections()
            resultViewModel.dispose()
            onRestartQuiz(quizType, dictionaryId, direction, true)
        },
        passedQuiz = resultViewModel.isAllPassed,
        onUpdateFab = { onUpdateFab(it) },
        numOfPassed = numOfPassed,
        numOfWords = numOfWords,
        resultPercentage = resultPercentage
    )
}

@Composable
fun ResultScreenContent(
    modifier: Modifier = Modifier,
    resultList: List<Word>,
    directionType: QuizDirectionType,
    onExit: () -> Unit,
    onRestartNew: () -> Unit,
    onRestartFailedOnly: () -> Unit,
    passedQuiz: Boolean,
    onUpdateFab: (FabConfiguration) -> Unit,
    numOfPassed: Int,
    numOfWords: Int,
    resultPercentage: Int = 0
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.secondaryContainer

    LaunchedEffect(passedQuiz, isFabExpanded) {
        onUpdateFab(
            if (passedQuiz) {
                FabConfiguration.FabMenu(
                    isVisible = true,
                    expanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    icon = Icons.Default.Replay,
                    labelId = R.string.result_start_over_label,
                    items =
                        listOf(
                            FabConfiguration.FabButton(
                                icon = Icons.Default.ChangeCircle,
                                iconLabelId = R.string.result_start_over_label,
                                onClick = {
                                    isFabExpanded = false
                                    onRestartNew()
                                },
                                extendedLabelId = R.string.result_start_over_label,
                                containerColor = containerColor
                            ),
                            FabConfiguration.FabButton(
                                icon = Icons.Default.Close,
                                iconLabelId = R.string.exit_fab_label,
                                onClick = {
                                    isFabExpanded = false
                                    onExit()
                                },
                                extendedLabelId = R.string.exit_fab_label,
                                containerColor = containerColor
                            )
                        )

                )
            } else {
                FabConfiguration.FabMenu(
                    isVisible = true,
                    expanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    icon = Icons.Default.Replay,
                    labelId = R.string.result_start_over_label,
                    items =
                        listOf(
                            FabConfiguration.FabButton(
                                icon = Icons.Default.ChangeCircle,
                                iconLabelId = R.string.result_start_over_label,
                                onClick = {
                                    isFabExpanded = false
                                    onRestartNew()
                                },
                                extendedLabelId = R.string.result_start_over_label,
                                containerColor = containerColor
                            ),
                            FabConfiguration.FabButton(
                                icon = Icons.Default.Error,
                                iconLabelId = R.string.result_failed_ones_only_label,
                                onClick = {
                                    isFabExpanded = false
                                    onRestartFailedOnly()
                                },
                                extendedLabelId = R.string.result_failed_ones_only_label,
                                containerColor = containerColor
                            ),
                            FabConfiguration.FabButton(
                                icon = Icons.Default.Close,
                                iconLabelId = R.string.exit_fab_label,
                                onClick = {
                                    isFabExpanded = false
                                    onExit()
                                },
                                extendedLabelId = R.string.exit_fab_label,
                                containerColor = containerColor
                            )
                        )

                )
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = String.format(
                    stringResource(R.string.result_stats),
                    numOfPassed,
                    numOfWords,
                    resultPercentage
                )
            )
            ResultLazyList(
                list = resultList,
                paddingValues = PaddingValues(0.dp),
                directionType = directionType
            )
        }
    }

    if (passedQuiz) {
        SuccessAnimation()
    } else {
        FailedAnimation()
    }
}

@Composable
fun ResultLazyList(
    modifier: Modifier = Modifier,
    list: List<Word>,
    paddingValues: PaddingValues,
    directionType: QuizDirectionType,
) {
    val state = rememberLazyListState()

    LazyColumn(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = paddingValues
    ) {
        items(list) { item ->
            if (item.lastResult) {
                ResultListItemPassed(
                    question = if (directionType == QuizDirectionType.AskWord) item.word else item.translation,
                    answer = item.lastGuess
                )
            } else {
                ResultListItemFailed(
                    question = if (directionType == QuizDirectionType.AskTranslation) item.translation else item.word,
                    answer = item.lastGuess,
                    solution = if (directionType == QuizDirectionType.AskTranslation) item.word else item.translation
                )
            }
        }
    }
}

@Composable
fun ResultListItemPassed(
    modifier: Modifier = Modifier,
    question: String,
    answer: String,
) {
    Row(
        modifier = modifier
            .padding(MaterialTheme.dimens.PaddingSmall)
            .fillMaxWidth()

    ) {
        Card(
            modifier = Modifier.weight(0.8f)
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .background(color = MaterialTheme.colorScheme.primaryFixedDim),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.PaddingLarge)
                        .weight(0.5f)
                        .align(Alignment.CenterVertically),
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryFixed
                )

                VerticalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(
                        top = MaterialTheme.dimens.PaddingMedium,
                        bottom = MaterialTheme.dimens.PaddingMedium
                    )
                )

                Text(
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.PaddingLarge)
                        .weight(0.5f)
                        .align(Alignment.CenterVertically),
                    text = answer,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryFixed
                )
                Icon(
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.PaddingMedium)
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Default.Done,
                    contentDescription = stringResource(R.string.result_start_over_label),
                    tint = MaterialTheme.colorScheme.onPrimaryFixed
                )
            }
        }

    }
}

@Composable
fun ResultListItemFailed(
    modifier: Modifier = Modifier,
    question: String,
    answer: String,
    solution: String
) {
    Row(
        modifier = modifier
            .padding(MaterialTheme.dimens.PaddingSmall)
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.weight(0.8f)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(color = (MaterialTheme.colorScheme.errorContainer)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = modifier
                        .padding(MaterialTheme.dimens.PaddingLarge)
                        .weight(0.5f)
                        .align(Alignment.CenterVertically),
                    text = question,
                    style = MaterialTheme.typography.bodyLarge
                )

                VerticalDivider(
                    thickness = 1.dp,
                    modifier = modifier.padding(MaterialTheme.dimens.PaddingMedium)
                )
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(MaterialTheme.dimens.PaddingMedium)

                ) {
                    Text(
                        text = answer.ifBlank { "-" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = solution,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Icon(
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.PaddingMedium)
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.result_start_over_label),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Preview
@Composable
fun ResultScreenPreview() {
    val list = listOf(
        Word(
            wordId = 1,
            containerDictionaryId = 1,
            word = "new",
            translation = "novus",
            beenAsked = 0,
            failed = 0,
            passed = 0,
            created = Calendar.getInstance().time,
            lastResult = false,
            lastGuess = "bad"
        ),
        Word(
            wordId = 2,
            containerDictionaryId = 1,
            word = "body",
            translation = "corpus",
            beenAsked = 0,
            failed = 0,
            passed = 0,
            created = Calendar.getInstance().time,
            lastResult = true,
            lastGuess = "corpus"
        ),
        Word(3, 1, "day", "diem", 0, 0, 0, Calendar.getInstance().time),
    )

    ResultScreenContent(
        resultList = list,
        directionType = QuizDirectionType.AskTranslation,
        onExit = {},
        onRestartNew = {},
        onRestartFailedOnly = {},
        passedQuiz = true,
        onUpdateFab = {},
        numOfPassed = 2,
        numOfWords = 3,
        resultPercentage = 66
    )
}