package com.vocabulary.myvocabulary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.quotes.QuoteData
import com.vocabulary.myvocabulary.utils.DialogFactory
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.vocabulary.myvocabulary.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val dialogFactory: DialogFactory by inject()
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.dictionaryButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_dictionaryListFragment)
        }
        binding.quizButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_quizListFragment)
        }
        binding.aboutButton.setOnClickListener {
            it.findNavController().navigate(R.id.to_aboutFragment)
        }
        showQuote(binding.quoteButton)

        return view
    }

    private fun showQuote(quoteButton: ImageButton) {
        homeViewModel.liveQuote.observe(viewLifecycleOwner, Observer { quoteData ->
            if (quoteData != QuoteData.EMPTY) {
                quoteButton.show(true)
                quoteButton.setOnClickListener {
                    quoteData as QuoteData.Quote
                    dialogFactory.buildQuotesDialog(
                        requireActivity(),
                        quoteData.title,
                        quoteData.quote,
                        quoteData.author
                    ).show()
                }
            } else {
                quoteButton.show(false)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}