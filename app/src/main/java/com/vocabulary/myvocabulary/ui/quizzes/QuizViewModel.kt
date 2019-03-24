package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent


class QuizViewModel(
        val dictionaryId: Long,
        val optionType: Int,
        private val failedOnly: Boolean,
        val quizType: Int,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers,
        private val quizRepository: QuizRepository
) : ViewModel(), KoinComponent {
    private val disposables = CompositeDisposable()
    private val updateIcon: MutableLiveData<Boolean> = MutableLiveData()
    private var lastIndexOfSubList: Int = 1
    private var listIsFinished = false
    val directionType = optionType.toDirectionType()
    private val liveWordList: MutableLiveData<List<FocusableWord>> = MutableLiveData()
    private var focusableWordList: MutableList<FocusableWord> = mutableListOf()

    init {
        observeList(quizType.toQuizType())
    }

    private fun observeList(quizType: QuizTypes) {
        when (quizType) {
            QuizTypes.FullQuiz -> observeFullList(failedOnly)
            QuizTypes.QuickQuiz -> observeQuickList(dictionaryId, failedOnly)
            QuizTypes.WeakestTenQuiz -> observeWeakestList(dictionaryId, failedOnly)
        }
    }

    private fun observeFullList(failedOnly: Boolean) {
        disposables += quizRepository.fullQuizList
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    it.forEachIndexed { index: Int, word: Word ->
                        val newFocusableWord = QuizViewModel.FocusableWord(word, index == 0)
                        if (!focusableWordList.contains(newFocusableWord) && word.containerDictionaryId == dictionaryId) {
                            if (!failedOnly || !word.lastResult) focusableWordList.add(newFocusableWord)
                        }
                    }
                    if (focusableWordList.isNotEmpty()) {
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

    fun observeQuickList(dictionaryId: Long, failedOnly: Boolean) {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    it.forEachIndexed { index: Int, word: Word ->
                        if (!failedOnly || !word.lastResult) focusableWordList.add(QuizViewModel.FocusableWord(word, index == 0))
                    }
                    focusableWordList.random()
                    if (focusableWordList.isNotEmpty()) {
                        liveWordList.postValue(focusableWordList.subList(0, 1))
                        updateIcon.postValue(focusableWordList.size == 1)
                        listIsFinished = focusableWordList.size == 1
                    }
                }
    }

    fun observeWeakestList(dictionaryId: Long, failedOnly: Boolean) {

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

    fun startNew() {
        quizRepository.startNewQuiz(dictionaryId)
        focusableWordList.clear()
        quizRepository.getFullQuizList()
    }
}
