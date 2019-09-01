package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.row_word.view.*

class WordItem(
        val wordData: Word,
        private val onClick: (selectedItem: WordItem) -> Unit
) : Item(wordData.wordId) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.containerView.apply {
            word.text = wordData.word
            translation.text = wordData.translation
            setOnClickListener { onClick(this@WordItem) }
        }
    }

    override fun getLayout() = R.layout.row_word

    override fun isSameAs(other: com.xwray.groupie.Item<*>?) = wordData.wordId == (other as? WordItem)?.wordData?.wordId

    override fun equals(other: Any?) = wordData.wordId == (other as? WordItem)?.wordData?.wordId
}