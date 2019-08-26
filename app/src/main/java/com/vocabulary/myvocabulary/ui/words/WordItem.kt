package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.row_word.view.*

class WordItem(
        private val wordData: Word
) : Item(wordData.wordId) {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.containerView.apply {
            word.text = wordData.word
            translation.text = wordData.translation
        }
    }

    override fun getLayout() = R.layout.row_word

    override fun isSameAs(other: com.xwray.groupie.Item<*>?) =  wordData == (other as? WordItem)?.wordData

    override fun equals(other: Any?) = wordData == (other as? WordItem)?.wordData
}