package com.vocabulary.myvocabulary.ui.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.vansuita.materialabout.builder.AboutBuilder
import com.vocabulary.myvocabulary.BuildConfig
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.R

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val version = "${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}"

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            AboutBuilder.with(ctx)
                .setPhoto(R.drawable.profile)
                .setCover(com.vansuita.materialabout.R.mipmap.profile_cover)
                .addEmailLink(Constants.EMAIL)
                .addLinkedInLink(Constants.LINKEDIN_ID)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(ctx.getString(R.string.app_name))
                .setAppTitle(version)
                .setActionsColumnsCount(2)
                .setLinksAnimated(true)
                .setLinksColumnsCount(2)
                .setWrapScrollView(true)
                .setShowAsCard(true)
                .addShareAction(R.string.app_name)
                .build()
        }
    )
}

@Preview
@Composable
fun AboutPreview() {
    AboutScreen()
}