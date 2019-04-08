package com.vocabulary.myvocabulary.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.vansuita.materialabout.builder.AboutBuilder
import com.vocabulary.myvocabulary.BuildConfig
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.fragment_about.view.*

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val version = "${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}"
        val builder = AboutBuilder.with(requireContext())
                .setPhoto(R.drawable.profile)
                .setCover(R.mipmap.profile_cover)
                .addEmailLink(Constants.EMAIL)
                .addLinkedInLink(Constants.LINKEDIN_ID)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(getString(R.string.app_name))
                .setAppTitle(version)
                .addShareAction(R.string.app_name)
                .setActionsColumnsCount(2)
                .setLinksAnimated(true)
                .setLinksColumnsCount(2)
                .setWrapScrollView(true)
                .setShowAsCard(true)
        return inflater.inflate(R.layout.fragment_about, container, false).apply {
            about_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            about_layout.addView(builder.build())
        }
    }
}