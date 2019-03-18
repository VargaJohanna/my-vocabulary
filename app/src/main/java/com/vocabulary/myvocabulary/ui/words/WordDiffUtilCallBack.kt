package com.vocabulary.myvocabulary.ui.words

import androidx.recyclerview.widget.DiffUtil
import com.vocabulary.myvocabulary.ui.words.Word

class WordDiffUtilCallBack(
        private val oldList: List<Word>,
        private val newList: List<Word>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].wordId == newList[newItemPosition].wordId
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}