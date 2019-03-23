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
import org.koin.core.inject
import org.koin.core.parameter.parametersOf


class QuizViewModel(
        val dictionaryId: Long,
        val optionType: Int,
        private val failedOnly: Boolean,
        quizType: Int,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel(), KoinComponent {
    private val quizRepository : QuizRepository by inject{
        parametersOf(dictionaryId, failedOnly, quizType)
    }
    private val disposables = CompositeDisposable()
//    private val liveWordList: MutableLiveData<List<FocusableWord>> = MutableLiveData()
//    private var focusableWordList: MutableList<FocusableWord> = mutableListOf()
//    private val updateIcon: MutableLiveData<Boolean> = MutableLiveData()
    private var lastIndexOfSubList: Int = 1
//    private var listIsFinished = false
    val directionType = optionType.toDirectionType()
    
    fun getLiveWordList(): LiveData<List<FocusableWord>> = quizRepository.liveWordList

    override fun onCleared() {
        disposables.clear()
        quizRepository.disposables.clear()
        super.onCleared()
    }

    fun nextClicked() {
        if (lastIndexOfSubList < quizRepository.focusableWordList.size) {
            lastIndexOfSubList += 1
            setFocusableValue(lastIndexOfSubList)
            quizRepository.listIsFinished = lastIndexOfSubList == quizRepository.focusableWordList.size

            quizRepository.liveWordList.postValue(quizRepository.focusableWordList.subList(0, lastIndexOfSubList))
            quizRepository.updateIcon.postValue(lastIndexOfSubList == quizRepository.focusableWordList.size)
        }
    }

    fun listIsNotFinished() = !quizRepository.listIsFinished
    fun getUpdateIcon(): LiveData<Boolean> = quizRepository.updateIcon

    private fun setFocusableValue(position: Int) {
        quizRepository.focusableWordList.subList(0, lastIndexOfSubList).forEachIndexed { index, focusableWord ->
            val focused = index == position - 1 || index == position - 2
            quizRepository.focusableWordList.subList(0, lastIndexOfSubList)[index] = focusableWord.copy(isFocused = focused)
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
