package com.vocabulary.myvocabulary.ui.quizzes

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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
            if (position == 0) {
                setMarginParameters(itemView)
            }
            if (position == wordList.size - 1) {
                itemView.solution.isEnabled = true
                itemView.solution.requestFocus()
                wordList[position] = wordList[position].copy(isFocused = false)
            } else {
                itemView.solution.isEnabled = false
                wordList[position] = wordList[position].copy(isFocused = false)
                itemView.solution.addTextChangedListener(object :TextWatcher{
                    override fun afterTextChanged(p0: Editable) {
                        setGuessedWord(QuizViewModel.GuessedWord(wordObject.word.wordId, p0.toString(), wordObject.word.word))
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                })
            }
            itemView.elevation = (position * 2).toFloat()
            if (askDirection == QuizDirectionType.AskMeaning) {
                itemView.question.text = wordObject.word.word
            } else {
                itemView.question.text = wordObject.word.translation
            }
        }
    }

    fun updateList(newList: List<QuizViewModel.FocusableWord>) {
        val diffResult = DiffUtil.calculateDiff(QuizDiffUtilCallBack(wordList, newList))
        this.wordList = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun setMarginParameters(itemView: View) {
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
    }

    fun setGuessedWord(guessedWord: QuizViewModel.GuessedWord) {
        _guessedWord.onNext(guessedWord)
    }
}