package com.vocabulary.myvocabulary.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vansuita.materialabout.builder.AboutBuilder
import com.vocabulary.myvocabulary.BuildConfig
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val view = binding.root

        val version = "${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}"
        val builder = AboutBuilder.with(requireContext())
                .setPhoto(R.drawable.profile)
                .setCover(com.vansuita.materialabout.R.mipmap.profile_cover)
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

        binding.aboutToolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.aboutLayout.addView(builder.build())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}