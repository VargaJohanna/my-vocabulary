package com.vocabulary.myvocabulary.ui.lottie

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.theme.dimens

@Composable
fun NewDictionaryAnimation() {
    var speed by remember { mutableStateOf(3f) }
    var isVisible by remember { mutableStateOf(true) }

    val composition by rememberLottieComposition(

        LottieCompositionSpec
            .RawRes(R.raw.dancing_book)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        isPlaying = isVisible,
        speed = speed,
        restartOnPlay = false

    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            isVisible = false
        }
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,

            ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.PaddingMedium)
                    .align(Alignment.TopCenter)
            )
        }
    }

}

@Preview
@Composable
fun NewDictionaryAnimationPreview() {
    NewDictionaryAnimation()
}
