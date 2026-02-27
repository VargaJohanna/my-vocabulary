package com.vocabulary.myvocabulary.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.theme.dimens
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar
import kotlin.math.round
import kotlin.text.ifEmpty

@Composable
fun HomeScreen(
    onClickDictionaries: () -> Unit = {},
    onClickQuiz: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val lastPracticedDictionary by homeViewModel.lastPracticedDictionary.collectAsState()
    val mostPracticedDictionary by homeViewModel.mostPracticedDictionary.collectAsState()
    val leastPracticedDictionary by homeViewModel.leastPracticedDictionary.collectAsState()
    val memoriseList by homeViewModel.memoriseList.collectAsState()
    val numOfDictionary by homeViewModel.numOfDictionaries.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        HomeScreenContent(
            lastPracticed = lastPracticedDictionary,
            mostPracticed = mostPracticedDictionary,
            leastPracticed = leastPracticedDictionary,
            memoriseList = memoriseList,
            numOfDictionary = numOfDictionary
        )
    }
}

@Composable
fun HomeScreenContent(
    lastPracticed: Dictionary?,
    mostPracticed: Dictionary?,
    leastPracticed: Dictionary?,
    memoriseList: List<Word>,
    numOfDictionary: Int

) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (lastPracticed != null) {
                val lastPracticedDate = lastPracticed.dictionaryLastPracticed?.let { date ->
                    DateTypeConverter().formatDate(date)
                }
                DictionaryStatsCard(
                    labelFirst = stringResource(R.string.last_practiced_label),
                    valueFirst = lastPracticed.dictionaryName,
                    labelSecond = stringResource(R.string.average_rate_label),
                    valueSecond = "${round(lastPracticed.averageResult)} %",
                    labelThird = stringResource(R.string.last_time_practiced_label),
                    valueThird = lastPracticedDate ?: ""
                )
            } else {
                PlaceholderCard(
                    title = stringResource(R.string.last_practiced_label),
                    body = stringResource(R.string.last_practiced_placeholder),
                )
            }

            if (mostPracticed != null && numOfDictionary > 2) {
                val mostPracticedDate = mostPracticed.dictionaryLastPracticed?.let { date ->
                    DateTypeConverter().formatDate(date)
                }
                DictionaryStatsCard(
                    labelFirst = stringResource(R.string.most_practiced_label),
                    valueFirst = mostPracticed.dictionaryName,
                    labelSecond = stringResource(R.string.average_rate_label),
                    valueSecond = "${round(mostPracticed.averageResult)} %",
                    labelThird = stringResource(R.string.last_time_practiced_label),
                    valueThird = mostPracticedDate ?: ""
                )
            } else {
                PlaceholderCard(
                    title = stringResource(R.string.most_practiced_label),
                    body = stringResource(R.string.most_practiced_placeholder),
                )
            }

            if (leastPracticed != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.dimens.PaddingMedium,
                            vertical = MaterialTheme.dimens.PaddingSmall
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_vecteezy_abstract_gray_background),
                            contentDescription = "Placeholder card background",
                            modifier = Modifier
                                .matchParentSize()
                                .alpha(0.4f),
                            contentScale = ContentScale.FillHeight,
                            alignment = Alignment.BottomEnd
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.dimens.PaddingLarge)

                        ) {
                            Text(
                                text = stringResource(R.string.memorise_label),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = MaterialTheme.dimens.PaddingMedium,
                                        end = MaterialTheme.dimens.PaddingMedium,
                                        top = MaterialTheme.dimens.PaddingMedium,
                                        bottom = MaterialTheme.dimens.PaddingMedium
                                    ),
                                style = MaterialTheme.typography.titleMedium
                            )
                            for (word in memoriseList) {
                                WordCard(
                                    modifier = Modifier,
                                    wordItem = word
                                )
                            }
                        }
                    }
                }

            } else {
                PlaceholderCard(
                    title = stringResource(R.string.memorise_label),
                    body = stringResource(R.string.memorise_placeholder),
                )
            }
        }
    }
}

@Composable
fun DictionaryStatsCard(
    labelFirst: String,
    valueFirst: String,
    labelSecond: String,
    valueSecond: String,
    labelThird: String,
    valueThird: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.dimens.PaddingMedium,
                vertical = MaterialTheme.dimens.PaddingSmall
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_vecteezy_abstract_gray_background),
                contentDescription = "Placeholder card background",
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.4f),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomEnd
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = MaterialTheme.dimens.PaddingLarge,
                            bottom = MaterialTheme.dimens.PaddingMedium,
                            start = MaterialTheme.dimens.PaddingLarge,
                            end = MaterialTheme.dimens.PaddingLarge,
                        )
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = labelFirst,
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.PaddingMedium)
                            .align(Alignment.Top),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = valueFirst,
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.PaddingMedium)
                            .align(Alignment.Top),
                        style = MaterialTheme.typography.bodyMedium
                    )

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = MaterialTheme.dimens.PaddingMedium,
                            horizontal = MaterialTheme.dimens.PaddingLarge
                        )
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = labelSecond,
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.PaddingMedium)
                            .align(Alignment.Top),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = valueSecond,
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.PaddingMedium)
                            .align(Alignment.Top),
                        style = MaterialTheme.typography.bodyMedium
                    )

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = MaterialTheme.dimens.PaddingMedium,
                            bottom = MaterialTheme.dimens.PaddingLarge,
                            start = MaterialTheme.dimens.PaddingLarge,
                            end = MaterialTheme.dimens.PaddingLarge,
                        )
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = labelThird,
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.PaddingMedium)
                            .align(Alignment.Top),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = valueThird,
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.PaddingMedium)
                            .align(Alignment.Top),
                        style = MaterialTheme.typography.bodyMedium
                    )

                }

            }

        }
    }
}

@Composable
fun PlaceholderCard(
    title: String,
    body: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.dimens.PaddingMedium,
                vertical = MaterialTheme.dimens.PaddingSmall
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_vecteezy_abstract_pastel),
                contentDescription = "Placeholder card background",
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.3f),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomEnd
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = MaterialTheme.dimens.PaddingMedium,
                        start = MaterialTheme.dimens.PaddingMedium,
                        end = MaterialTheme.dimens.PaddingMedium,
                        bottom = MaterialTheme.dimens.PaddingExtraLarge
                    )
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(
                            top = MaterialTheme.dimens.PaddingLarge,
                            bottom = MaterialTheme.dimens.PaddingSmall,
                            start = MaterialTheme.dimens.PaddingLarge,
                            end = MaterialTheme.dimens.PaddingLarge
                        )
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = body,
                    modifier = Modifier
                        .padding(
                            top = MaterialTheme.dimens.PaddingSmall,
                            bottom = MaterialTheme.dimens.PaddingLarge,
                            start = MaterialTheme.dimens.PaddingLarge,
                            end = MaterialTheme.dimens.PaddingLarge
                        )
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun WordCard(
    modifier: Modifier,
    wordItem: Word,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.PaddingMedium,
                vertical = MaterialTheme.dimens.PaddingMedium)
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
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium),
                    text = wordItem.word,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            VerticalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(MaterialTheme.dimens.PaddingSmall)
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium),
                    text = wordItem.translation.ifEmpty { stringResource(R.string.word_hint) },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun HomePreview() {
    val wordList = listOf(
        Word(
            2,
            1,
            "two",
            "zwei",
            0,
            0,
            0,
            Calendar.getInstance().time
        ),
        Word(
            1,
            1,
            "three",
            "drei",
            0,
            0,
            0,
            Calendar.getInstance().time
        ),
        Word(
            0,
            1,
            "one",
            "eins",
            0,
            0,
            0,
            Calendar.getInstance().time
        ))
    HomeScreenContent(
        lastPracticed = null,
        mostPracticed = Dictionary(
            dictionaryId = 0,
            dictionaryName = "German",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        ),
        leastPracticed = Dictionary(
            dictionaryId = 0,
            dictionaryName = "German",
            dictionaryCreated = Calendar.getInstance().time,
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0,
        ),
        memoriseList = wordList,
        numOfDictionary = 1
    )
}