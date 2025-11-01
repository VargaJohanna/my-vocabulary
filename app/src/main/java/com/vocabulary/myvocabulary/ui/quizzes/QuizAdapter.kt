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
import com.vocabulary.myvocabulary.databinding.RowQuizBinding

class QuizAdapter(
        private var wordList: MutableList<QuizViewModel.FocusableWord>,
        private var askDirection: QuizDirectionType
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {
    private var lastGuess: String? = null
    private var watcher = GuessTextWatcher()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = RowQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuizViewHolder(binding)
    }

    override fun getItemCount(): Int = wordList.size

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(wordList[position], position)
    }

    inner class QuizViewHolder(private val binding: RowQuizBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wordObject: QuizViewModel.FocusableWord, position: Int) {
            makeLastElementEditable(position, binding.solution)
            addElevationToEachItem(itemView, position)

            setTextWatcher(itemView, position)

            setContent(wordObject, itemView, position)
            setAnimationForLastItem(itemView, position)
        }

        private fun setTextWatcher(itemView: View, position: Int) {
            if (position == wordList.size - 1) {
                binding.solution.addTextChangedListener(watcher)
            } else {
                binding.solution.removeTextChangedListener(watcher)
            }
        }

        private fun setContent(wordObject: QuizViewModel.FocusableWord, itemView: View, position: Int) {
            itemView.apply {
                if (position != wordList.size - 1) {
                    binding.question.setTextColor(ContextCompat.getColor(context, R.color.transparent))
                    binding.view.show(false)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                    binding.solution.apply {
                        setTextColor(ContextCompat.getColor(context, R.color.transparent))
                        setHintTextColor(ContextCompat.getColor(context, R.color.transparent))
                        background.setTint(ContextCompat.getColor(context, R.color.transparent))
                    }
                    binding.solution.setTextColor(ContextCompat.getColor(context, R.color.transparent))
                    binding.solution.setHintTextColor(ContextCompat.getColor(context, R.color.transparent))
                    binding.solution.background.setTint(ContextCompat.getColor(context, R.color.transparent))

                } else {
                    binding.question.setTextColor(ContextCompat.getColor(context, R.color.primary_text))
                    binding.view.show(true)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.icons))
                    binding.solution.setText("")
                    binding.question.text = ""
                    if (askDirection == QuizDirectionType.AskWord) {
                        binding.question.text = wordObject.word.word
                    } else {
                        binding.question.text = wordObject.word.translation
                    }
                    binding.solution.apply {
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

    fun lastGuess(): GuessedWord {
        return GuessedWord(wordList.last().word.wordId, lastGuess ?: "")
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