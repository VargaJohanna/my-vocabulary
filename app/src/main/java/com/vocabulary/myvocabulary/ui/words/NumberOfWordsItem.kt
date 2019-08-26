package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder


class NumberOfWordsItem(numberOfWords: String) : Item(-1) {
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout() = R.layout.row_word

}