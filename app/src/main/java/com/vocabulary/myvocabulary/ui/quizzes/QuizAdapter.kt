package com.vocabulary.myvocabulary.ui.quizzes

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.convertDpToPx
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.row_quiz.view.*

class QuizAdapter(
        private var wordList: MutableList<QuizViewModel.FocusableWord>,
        private var askDirection: QuizDirectionType
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {
    private val _guessedWord = BehaviorSubject.create<QuizViewModel.GuessedWord>()
    val guessedWord: Observable<QuizViewModel.GuessedWord> = _guessedWord
    private var guessEntered = false

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
            guessEntered = false
            addExtraMarginForFirstElement(position, itemView)

            makeLastElementEditable(position, itemView.solution, itemView.question, itemView)

            itemView.solution.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable) {
                    setGuessedWord(QuizViewModel.GuessedWord(wordObject.word.wordId, p0.toString(), wordObject.word.word))
                    guessEntered = p0.isNotEmpty()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
            if (!guessEntered) setGuessedWord(QuizViewModel.GuessedWord(wordObject.word.wordId, "", wordObject.word.word))

            addElevationToEachItem(itemView, position)

            if (askDirection == QuizDirectionType.AskWord) itemView.question.text = wordObject.word.word
            else itemView.question.text = wordObject.word.translation
            setAnimation(itemView)
        }

        private fun addElevationToEachItem(itemView: View, position: Int) {
            itemView.elevation = (position * 2).toFloat()
        }

        fun setGuessedWord(guessedWord: QuizViewModel.GuessedWord) {
            _guessedWord.onNext(guessedWord)
        }
    }

    private fun makeLastElementEditable(position: Int, solution: EditText, question: TextView, itemView: View) {
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

    fun addExtraMarginForFirstElement(position: Int, itemView: View) {
        if (position == 0) {
            val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                    8f.convertDpToPx(itemView.context),
                    150,
                    8f.convertDpToPx(itemView.context)
                    , 0
            )
            itemView.layoutParams = params
        } else {
            val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                    8f.convertDpToPx(itemView.context),
                    0,
                    8f.convertDpToPx(itemView.context)
                    , 0
            )
            itemView.layoutParams = params
        }
    }

    fun setAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_up)
        view.startAnimation(animation)
    }
}