package com.vocabulary.myvocabulary.utils

import androidx.recyclerview.widget.DiffUtil
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary

class DictionaryDiffUtilCallBack(
        private val oldList: List<Dictionary>,
        private val newList: List<Dictionary>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].dictionaryId == newList[newItemPosition].dictionaryId
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