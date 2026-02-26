package com.vocabulary.myvocabulary.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocabulary.myvocabulary.R
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
    val lastPracticedDictionary = homeViewModel.lastPracticedDictionary.collectAsState()
    val mostPracticedDictionary = homeViewModel.mostPracticedDictionary.collectAsState()

    val lastPracticedDateLast = lastPracticedDictionary.value?.dictionaryLastPracticed?.let { date ->
        DateTypeConverter().formatDate(date)
    }

    val lastPracticedDateMost = mostPracticedDictionary.value?.dictionaryLastPracticed?.let { date ->
        DateTypeConverter().formatDate(date)
    }

    val avgResultLast = lastPracticedDictionary.value?.averageResult?.let {
        round(it)
    }
    
    val avgResultMost = mostPracticedDictionary.value?.averageResult?.let {
        round(it)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        HomeScreenContent(
            lastPracticedDictionary = lastPracticedDictionary.value?.dictionaryName,
            lastPracticedDateLast = lastPracticedDateLast,
            avgResultLast = avgResultLast ?: 0f,
            mostPracticedDictionary = mostPracticedDictionary.value?.dictionaryName,
            avgResultMost = avgResultMost ?: 0f,
            lastPracticedDateMost = lastPracticedDateMost
        )
    }
}

@Composable
fun HomeScreenContent(
    lastPracticedDictionary: String?,
    lastPracticedDateLast: String?,
    avgResultLast: Float,
    mostPracticedDictionary: String?,
    avgResultMost: Float,
    lastPracticedDateMost: String?,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        FlowColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (lastPracticedDictionary != null) {
                DictionaryStatsCard(
                    labelFirst = stringResource(R.string.last_practiced_label),
                    valueFirst = lastPracticedDictionary ?: "",
                    labelSecond = stringResource(R.string.average_rate_label),
                    valueSecond = "$avgResultLast %",
                    labelThird = stringResource(R.string.last_time_practiced_label),
                    valueThird = lastPracticedDateLast ?: ""
                )
            } else {
                PlaceholderCard(
                    title = stringResource(R.string.last_practiced_label),
                    body = stringResource(R.string.last_practiced_placeholder)
                )
            }

            if (mostPracticedDictionary != null) {
                DictionaryStatsCard(
                    labelFirst = stringResource(R.string.most_practiced_label),
                    valueFirst = mostPracticedDictionary ?: "",
                    labelSecond = stringResource(R.string.average_rate_label),
                    valueSecond = "$avgResultMost %",
                    labelThird = stringResource(R.string.last_time_practiced_label),
                    valueThird = lastPracticedDateMost ?: ""
                )
            } else {
                PlaceholderCard(
                    title = stringResource(R.string.most_practiced_label),
                    body = stringResource(R.string.most_practiced_placeholder)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.PaddingMedium)
            ) {
                Text(
                    text = "Memorise: ",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.dimens.PaddingMedium),
                    style = MaterialTheme.typography.titleMedium
                )
                WordCard(
                    modifier = Modifier,
                    wordItem = Word(1, 1, "new", "novus", 0, 0, 0, Calendar.getInstance().time)

                )
                WordCard(
                    modifier = Modifier,
                    wordItem = Word(2, 1, "peace", "pax", 0, 0, 0, Calendar.getInstance().time)

                )
                WordCard(
                    modifier = Modifier,
                    wordItem = Word(3, 1, "body", "corpus", 0, 0, 0, Calendar.getInstance().time)

                )
                WordCard(
                    modifier = Modifier,
                    wordItem = Word(4, 1, "house", "domus", 0, 0, 0, Calendar.getInstance().time)

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
            .padding(MaterialTheme.dimens.PaddingMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.PaddingMedium)
                .align(Alignment.CenterHorizontally)
        ) {
            Text( text = labelFirst,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.Top),
                style = MaterialTheme.typography.titleMedium
            )

            Text( text = valueFirst,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.Top),
                style = MaterialTheme.typography.bodyMedium
            )

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.PaddingMedium)
                .align(Alignment.CenterHorizontally)
        ) {
            Text( text = labelSecond,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.Top),
                style = MaterialTheme.typography.titleMedium
            )
            Text( text = valueSecond,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.Top),
                style = MaterialTheme.typography.bodyMedium
            )

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.PaddingMedium)
                .align(Alignment.CenterHorizontally)
        ) {
            Text( text = labelThird,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.Top),
                style = MaterialTheme.typography.titleMedium
            )
            Text( text = valueThird,
                modifier = Modifier
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.Top),
                style = MaterialTheme.typography.bodyMedium
            )

        }
    }
}

@Composable
fun PlaceholderCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.PaddingMedium),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_vecteezy_desert_dunes_at_sunset_with_cloud_vector_egyptian_landscape_25738879),
                contentDescription = "Placeholder card background",
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.2f),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomEnd
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.PaddingMedium)
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.PaddingLarge)
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = body,
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.PaddingLarge)
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
            .padding(MaterialTheme.dimens.PaddingMedium)
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
                modifier = Modifier.padding(MaterialTheme.dimens.PaddingMedium)
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
    HomeScreenContent(
        lastPracticedDictionary = null,
        lastPracticedDateLast = DateTypeConverter().formatDate(Calendar.getInstance().time),
        avgResultLast = 78f,
        mostPracticedDictionary = "Spanish",
        avgResultMost = 33f,
        lastPracticedDateMost = DateTypeConverter().formatDate(Calendar.getInstance().time)
    )
}