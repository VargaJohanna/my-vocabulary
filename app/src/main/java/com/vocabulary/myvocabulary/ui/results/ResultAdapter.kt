package com.vocabulary.myvocabulary.ui.results

import android.view.LayoutInflater
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
import com.vocabulary.myvocabulary.databinding.RowResultBinding

class ResultAdapter(
    private var resultList: List<Word>,
    private val directionType: QuizDirectionType
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = RowResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun getItemCount(): Int = resultList.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(resultList[position])
    }

    inner class ResultViewHolder(private val binding: RowResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(word: Word) {
            setQuestionText(word, binding.resultQuestion)
            setGuessText(word, binding.guess)
            setItemViewAppearance(binding, word)
        }

        private fun setItemViewAppearance(binding: RowResultBinding, word: Word) {

            binding.materialCardView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (word.lastResult) R.color.light_green
                    else R.color.light_error
                )
            )

            binding.passedIcon.show(word.lastResult)
            binding.failedIcon.show(!word.lastResult)
            binding.correctSolution.display(!word.lastResult)
            binding.guess.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (!word.lastResult) R.color.error else R.color.secondary_text
                )
            )
            binding.correctSolution.text =
                if (directionType == QuizDirectionType.AskWord) word.translation else word.word

        }

        private fun setGuessText(word: Word, guess: TextView) {
            guess.text = if (word.lastGuess.isEmpty()) "-" else word.lastGuess
        }

        private fun setQuestionText(word: Word, resultQuestion: TextView) {
            resultQuestion.text =
                if (directionType == QuizDirectionType.AskWord) word.word else word.translation
        }
    }

    fun updateList(newList: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallBack(resultList, newList))
        this.resultList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}