package com.vocabulary.myvocabulary.ui.results

import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(word: Word) {
            setQuestionText(word, itemView.result_question)
            setGuessText(word, itemView.guess)

            setItemViewAppearance(itemView, word)

        }

        private fun setItemViewAppearance(itemView: View, word: Word) {
            itemView.materialCardView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                    if(word.lastResult) R.color.light_green
                    else R.color.light_error))

            itemView.passed_icon.show(word.lastResult)
            itemView.failed_icon.show(!word.lastResult)
            itemView.correct_solution.display(!word.lastResult)
            itemView.guess.setTextColor(ContextCompat.getColor(itemView.context, if (!word.lastResult) R.color.error else R.color.secondary_text))
            itemView.correct_solution.text = if (directionType == QuizDirectionType.AskWord) word.translation else word.word

        }

        private fun setGuessText(word: Word, guess: TextView) {
            guess.text = if(word.lastGuess.isEmpty()) "--" else word.lastGuess
        }

        private fun setQuestionText(word: Word, resultQuestion: TextView) {
            resultQuestion.text = if (directionType == QuizDirectionType.AskWord) word.word else word.translation
        }
    }

    fun updateList(newList: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallBack(resultList, newList))
        this.resultList = newList
        diffResult.dispatchUpdatesTo(this)
    }

}