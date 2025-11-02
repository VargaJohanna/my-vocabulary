package com.vocabulary.myvocabulary.ui.words

import android.view.View
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.databinding.RowNumberOfWordsBinding
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class NumberOfWordsItem(
    private val numberOfWords: String
) : BindableItem<RowNumberOfWordsBinding>() {
    override fun initializeViewBinding(view: View): RowNumberOfWordsBinding {
        return RowNumberOfWordsBinding.bind(view)
    }

    override fun bind(binding: RowNumberOfWordsBinding, position: Int) {
        binding.numberOfWords.text = numberOfWords
    }

    override fun getLayout() = R.layout.row_number_of_words

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return numberOfWords == (other as? NumberOfWordsItem)?.numberOfWords
    }

    override fun equals(other: Any?) = numberOfWords == (other as? NumberOfWordsItem)?.numberOfWords

}