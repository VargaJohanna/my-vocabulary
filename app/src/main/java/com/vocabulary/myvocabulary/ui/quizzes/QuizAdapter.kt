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
    private var watcher = GuessTextWatcher()
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

            setTextWatcher(itemView, position)

            setContent(wordObject, itemView, position)
            setAnimationForLastItem(itemView, position)
        }

        private fun setTextWatcher(itemView: View, position: Int) {
            if (position == wordList.size - 1) {
                itemView.solution.addTextChangedListener(watcher)
            } else {
                itemView.solution.removeTextChangedListener(watcher)
            }
        }

        private fun setContent(wordObject: QuizViewModel.FocusableWord, itemView: View, position: Int) {
            itemView.apply {
                if (position != wordList.size - 1) {
                    question.setTextColor(ContextCompat.getColor(context, R.color.transparent))
                    view.show(false)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                    solution.apply {
                        setTextColor(ContextCompat.getColor(context, R.color.transparent))
                        setHintTextColor(ContextCompat.getColor(context, R.color.transparent))
                        background.setTint(ContextCompat.getColor(context, R.color.transparent))
                    }
                } else {
                    question.setTextColor(ContextCompat.getColor(context, R.color.primary_text))
                    view.show(true)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.icons))
                    solution.setText("")
                    question.text = ""
                    if (askDirection == QuizDirectionType.AskWord) {
                        question.text = wordObject.word.word
                    } else {
                        question.text = wordObject.word.translation
                    }
                    solution.apply {
                        setTextColor(ContextCompat.getColor(context, R.color.primary_text))
                        setHintTextColor(ContextCompat.getColor(context, R.color.grey))
                        background.setTint(ContextCompat.getColor(context, R.color.divider))
                    }
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

    fun lastGuess(): GuessedWord? {
        return if(wordList.isNotEmpty()) {
            GuessedWord(wordList.last().word.wordId, lastGuess
                    ?: "")
        } else null
    }

    private inner class GuessTextWatcher : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            lastGuess = p0.toString()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    }
}