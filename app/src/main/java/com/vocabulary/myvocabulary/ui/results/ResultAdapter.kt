package com.vocabulary.myvocabulary.ui.results

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.WordDiffUtilCallBack
import kotlinx.android.synthetic.main.row_result.view.*

class ResultAdapter(
        private var resultList: List<Word>,
        private val directionType: QuizDirectionType
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ResultViewHolder(inflater.inflate(R.layout.row_result, parent, false))
    }

    override fun getItemCount(): Int = resultList.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(resultList[position])
        Log.d(javaClass.name, "START BINDING")
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(word: Word) {
            when (directionType) {
                QuizDirectionType.AskWord -> itemView.result_question.text = word.word
                else -> itemView.result_question.text = word.translation
            }
            when {
                word.lastGuess.isEmpty() -> itemView.guess.text = "--"
                else -> itemView.guess.text = word.lastGuess
            }

            when {
                word.lastResult -> {
                    itemView.materialCardView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.light_green))
                    itemView.passed_icon.show(true)
                    itemView.failed_icon.show(false)
                    itemView.correct_solution.display(false)

                }
                else -> {
                    itemView.materialCardView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.light_error))
                    itemView.passed_icon.show(false)
                    itemView.failed_icon.show(true)
                    itemView.guess.show(true)

                    itemView.guess.setTextColor(ContextCompat.getColor(itemView.context, R.color.error))
                    itemView.correct_solution.display(true)
                    when (directionType) {
                        QuizDirectionType.AskWord -> itemView.correct_solution.text = word.translation
                        else -> itemView.correct_solution.text = word.word
                    }
                }
            }
        }
    }

    fun updateList(newList: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallBack(resultList, newList))
        this.resultList = newList
        diffResult.dispatchUpdatesTo(this)
    }

}