package com.vocabulary.myvocabulary.ui.quizzes

import androidx.recyclerview.widget.DiffUtil

class QuizDiffUtilCallBack(
        private val oldList: List<QuizViewModel.FocusableWord>,
        private val newList: List<QuizViewModel.FocusableWord>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldList[oldItemPosition].word.wordId == newList[newItemPosition].word.wordId
                && oldList[oldItemPosition].isFocused == newList[newItemPosition].isFocused)

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