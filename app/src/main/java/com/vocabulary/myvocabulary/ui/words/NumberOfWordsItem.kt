package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.row_number_of_words.view.*


class NumberOfWordsItem(
        private val numberOfWords: String
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.containerView.apply {
            number_of_words.text = numberOfWords
        }
    }

    override fun getLayout() = R.layout.row_number_of_words

    override fun isSameAs(other: com.xwray.groupie.Item<*>?) = numberOfWords == (other as? NumberOfWordsItem)?.numberOfWords

    override fun equals(other: Any?) = numberOfWords == (other as? NumberOfWordsItem)?.numberOfWords

}