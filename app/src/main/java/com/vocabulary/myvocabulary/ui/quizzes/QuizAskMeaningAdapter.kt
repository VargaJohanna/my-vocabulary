package com.vocabulary.myvocabulary.ui.quizzes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.WordDiffUtilCallBack
import kotlinx.android.synthetic.main.row_quiz.view.*

class QuizAskMeaningAdapter(private var wordList: List<Word>) : RecyclerView.Adapter<QuizAskMeaningAdapter.QuizViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuizViewHolder(inflater.inflate(R.layout.row_quiz, parent, false))
    }

    override fun getItemCount(): Int = wordList.size

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(wordList[position])
    }

    inner class QuizViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        fun bind(word: Word) {
            itemView.question.text = word.word
        }
    }

    fun updateList(newList: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallBack(wordList, newList))
        this.wordList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}