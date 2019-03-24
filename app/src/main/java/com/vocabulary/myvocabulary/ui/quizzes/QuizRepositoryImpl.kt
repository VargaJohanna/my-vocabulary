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
    override val _quizList: BehaviorSubject<List<Word>> = BehaviorSubject.create<List<Word>>()
    override val quizList: Observable<List<Word>> = _quizList
    private val disposables = CompositeDisposable()

    override fun resetFullQuizList(dictionaryId: Long) {
        disposables.clear()
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .firstOrError()
                .observeOn(rxSchedulers.main())
                .subscribe { t ->
                    _quizList.onNext(t)
                }
    }

    override fun resetQuickQuizList(dictionaryId: Long) {
        disposables.clear()
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .map { it.shuffled() }
                .map { it.take(5) }
                .firstOrError()
                .observeOn(rxSchedulers.main())
                .subscribe { t -> _quizList.onNext(t) }
    }

    override fun resetWeakestFive(dictionaryId: Long) {
        disposables.clear()
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .map { list -> list.sortedWith(compareBy { it.failed }).reversed() }
                .map { it.take(5) }
                .firstOrError()
                .observeOn(rxSchedulers.main())
                .subscribe { t -> _quizList.onNext(t) }
    }
}