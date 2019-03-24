package com.vocabulary.myvocabulary.ui.quizzes

import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class QuizRepositoryImpl(
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : QuizRepository {
    private val _fullQuizList = BehaviorSubject.create<List<Word>>()
    override val fullQuizList: Observable<List<Word>> = _fullQuizList
    private val disposables = CompositeDisposable()
    private var dictionaryId: Long = 0

    override fun getFullQuizList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe {
                    _fullQuizList.onNext(it)
                }
    }

    override fun startNewQuiz(dictionaryId: Long) {
        this.dictionaryId = dictionaryId
        disposables.clear()
    }

}