package com.vocabulary.myvocabulary.ui.quizzes

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.show
import kotlinx.android.synthetic.main.row_quiz.view.*

class QuizAdapter(
        private var wordList: MutableList<QuizViewModel.FocusableWord>,
        private var askDirection: QuizDirectionType
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {
    private var lastGuess: String? = null
    private var watcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable) {
            lastGuess = p0.toString()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuizViewHolder(inflater.inflate(com.vocabulary.myvocabulary.R.layout.row_quiz, parent, false))
    }

    override fun getItemCount(): Int = wordList.size

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(wordList[position], position)
    }

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(wordObject: QuizViewModel.FocusableWord, position: Int) {
            makeLastElementEditable(position, itemView.solution)
            addElevationToEachItem(itemView, position)

            saveGuessedWord(itemView, position)

            setContent(wordObject, itemView, position)
            setAnimationForLastItem(itemView, position)
        }

        private fun saveGuessedWord(itemView: View, position: Int) {
            if (position == wordList.size - 1) {
                itemView.solution.addTextChangedListener(watcher)
            } else {
                itemView.solution.removeTextChangedListener(watcher)
            }
        }

        private fun setContent(wordObject: QuizViewModel.FocusableWord, itemView: View, position: Int) {
            if (position != wordList.size - 1) {
                itemView.question.setTextColor(ContextCompat.getColor(itemView.context, R.color.transparent))
                itemView.solution.setTextColor(ContextCompat.getColor(itemView.context, R.color.transparent))
                itemView.solution.setHintTextColor(ContextCompat.getColor(itemView.context, R.color.transparent))
                itemView.solution.background.setTint(ContextCompat.getColor(itemView.context, R.color.transparent))
                itemView.view.show(false)
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.light_grey))
            } else {
                itemView.question.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))
                itemView.solution.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))
                itemView.solution.setHintTextColor(ContextCompat.getColor(itemView.context, R.color.grey))
                itemView.solution.background.setTint(ContextCompat.getColor(itemView.context, R.color.divider))
                itemView.view.show(true)
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.icons))
                itemView.solution.setText("")
                itemView.question.text = ""
                if (askDirection == QuizDirectionType.AskWord) {
                    itemView.question.text = wordObject.word.word
                } else {
                    itemView.question.text = wordObject.word.translation
                }
            }
        }

        private fun addElevationToEachItem(itemView: View, position: Int) {
            itemView.elevation = (position * 2).toFloat()
        }
    }

    private fun makeLastElementEditable(position: Int, solution: EditText) {
        if (position == wordList.size - 1) {
            solution.isEnabled = true
            solution.requestFocus()
            wordList[position] = wordList[position].copy(isFocused = false)
        } else {
            solution.isEnabled = false
            wordList[position] = wordList[position].copy(isFocused = false)
        }
    }

    fun updateList(newList: List<QuizViewModel.FocusableWord>) {
        val diffResult = DiffUtil.calculateDiff(QuizDiffUtilCallBack(wordList, newList))
        this.wordList = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun setAnimationForLastItem(view: View, position: Int) {
        if (position == wordList.size - 1) {
            val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_up)
            view.startAnimation(animation)
        }
    }

    fun lastGuess(): QuizViewModel.GuessedWord {
        return QuizViewModel.GuessedWord(wordList.last().word.wordId, lastGuess
                ?: "", wordList.last().word.word)
    }
}