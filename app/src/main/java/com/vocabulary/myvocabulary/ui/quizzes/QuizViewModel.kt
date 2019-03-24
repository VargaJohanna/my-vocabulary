package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable

class QuizViewModel(
        val dictionaryId: Long,
        val optionType: Int,
        failedOnly: Boolean,
        private val quizType: Int,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val updateIcon = MutableLiveData<Boolean>()
    private var lastIndexOfSubList = 1
    private var listIsFinished = false
    val directionType = optionType.toDirectionType()
    private val liveWordList = MutableLiveData<List<FocusableWord>>()
    private var focusableWordList = mutableListOf<FocusableWord>()

    init {
        observeQuizList(failedOnly)
    }

    private fun observeQuizList(failedOnly: Boolean) {
        disposables += quizRepository.quizList
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    focusableWordList.clear()
                    it.forEachIndexed { index: Int, word: Word ->
                        val newFocusableWord = QuizViewModel.FocusableWord(word, index == 0)
                        if (!focusableWordList.contains(newFocusableWord) && word.containerDictionaryId == dictionaryId) {
                            if (!failedOnly || !word.lastResult) focusableWordList.add(newFocusableWord)
                        }
                    }
                    if (focusableWordList.isNotEmpty()) {
                        focusableWordList.shuffle()
                        liveWordList.postValue(focusableWordList.subList(0, 1))
                        updateIcon.postValue(focusableWordList.size == 1)
                        listIsFinished = focusableWordList.size == 1
                    }
                }
    }

    fun getLiveWordList(): LiveData<List<FocusableWord>> = liveWordList

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun nextClicked() {
        if (lastIndexOfSubList < focusableWordList.size) {
            lastIndexOfSubList += 1
            setFocusableValue(lastIndexOfSubList)
            listIsFinished = lastIndexOfSubList == focusableWordList.size

            liveWordList.postValue(focusableWordList.subList(0, lastIndexOfSubList))
            updateIcon.postValue(lastIndexOfSubList == focusableWordList.size)
        }
    }

    fun listIsNotFinished() = !listIsFinished

    fun getUpdateIcon(): LiveData<Boolean> = updateIcon

    private fun setFocusableValue(position: Int) {
        focusableWordList.subList(0, lastIndexOfSubList).forEachIndexed { index, focusableWord ->
            val focused = index == position - 1 || index == position - 2
            focusableWordList.subList(0, lastIndexOfSubList)[index] = focusableWord.copy(isFocused = focused)
        }
    }

    fun startNew() {
        focusableWordList.clear()
        when (quizType.toQuizType()) {
            QuizTypes.FullQuiz -> quizRepository.resetFullQuizList(dictionaryId)
            QuizTypes.QuickQuiz -> quizRepository.resetQuickQuizList(dictionaryId)
            QuizTypes.WeakestQuiz -> quizRepository.resetWeakestFive(dictionaryId)
        }
    }

    data class FocusableWord(
            val word: Word,
            val isFocused: Boolean
    )

    data class GuessedWord(
            val wordId: Long,
            val guess: String,
            val wordValue: String
    )
}
