package com.vocabulary.myvocabulary.ui.quizzes

import androidx.lifecycle.MutableLiveData
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.disposables.CompositeDisposable

class QuizRepositoryImpl(
        val dictionaryId: Long,
        private val failedOnly: Boolean,
        quizType: Int,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : QuizRepository {
    override val disposables = CompositeDisposable()
    override var focusableWordList: MutableList<QuizViewModel.FocusableWord> = mutableListOf()
    override val liveWordList: MutableLiveData<List<QuizViewModel.FocusableWord>> = MutableLiveData()
    override val updateIcon: MutableLiveData<Boolean> = MutableLiveData()
    override var listIsFinished = false

    init {
        observeList(quizType.toQuizType())
    }

    private fun observeList(quizType: QuizTypes) {
        when(quizType) {
            QuizTypes.FullQuiz -> observeFullList(dictionaryId, failedOnly)
            QuizTypes.QuickQuiz -> observeQuickList(dictionaryId, failedOnly)
            QuizTypes.WeakestTenQuiz -> observeWeakestList(dictionaryId, failedOnly)
        }
    }

    override fun observeFullList(dictionaryId: Long, failedOnly: Boolean) {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    it.forEachIndexed { index: Int, word: Word ->
                        if(!focusableWordList.contains(QuizViewModel.FocusableWord(word, index == 0))) {
                            if(!failedOnly || !word.lastResult) focusableWordList.add(QuizViewModel.FocusableWord(word, index == 0))

                        }

                    }
                    liveWordList.postValue(focusableWordList.subList(0, 1))
                    updateIcon.postValue(focusableWordList.size == 1)
                    listIsFinished = focusableWordList.size == 1

                }
    }

    override fun observeQuickList(dictionaryId: Long, failedOnly: Boolean) {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    it.forEachIndexed { index: Int, word: Word ->
                        if(!failedOnly || !word.lastResult) focusableWordList.add(QuizViewModel.FocusableWord(word, index == 0))
                    }
                    focusableWordList.random()
                    liveWordList.postValue(focusableWordList.subList(0, 1))
                    updateIcon.postValue(focusableWordList.size == 1)
                    listIsFinished = focusableWordList.size == 1

                }
    }

    override fun observeWeakestList(dictionaryId: Long, failedOnly: Boolean) {

    }

    override fun resetList() {

    }

}