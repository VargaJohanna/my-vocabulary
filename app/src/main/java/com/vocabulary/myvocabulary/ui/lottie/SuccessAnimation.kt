package com.vocabulary.myvocabulary.ui.lottie

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.theme.dimens

@Composable
fun SuccessAnimation() {
    var isPlaying by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(1f) }

    val composition by rememberLottieComposition(

        LottieCompositionSpec
            .RawRes(R.raw.confetti_falling)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        isPlaying = isPlaying,
        speed = speed,
        restartOnPlay = false

    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter

    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxWidth()
                .padding(MaterialTheme.dimens.PaddingMedium)
                .align(Alignment.TopCenter)
        )
    }
}

@Preview
@Composable
fun SuccessLottiePreview() {
    SuccessAnimation()
}