package com.vocabulary.myvocabulary.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vocabulary.myvocabulary.R
import org.koin.androidx.viewmodel.ext.sharedViewModel
import org.koin.core.parameter.parametersOf

class ResultFragment : Fragment() {
    private val resultViewModel: ResultViewModel by sharedViewModel {
        parametersOf(
                ResultFragmentArgs.fromBundle(arguments!!).dictionaryIdForResult,
                ResultFragmentArgs.fromBundle(arguments!!).directionType
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }
}