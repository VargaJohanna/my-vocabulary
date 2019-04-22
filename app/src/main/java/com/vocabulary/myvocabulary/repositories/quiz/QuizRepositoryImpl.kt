package com.vocabulary.myvocabulary.repositories.quiz

import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class QuizRepositoryImpl(
        private val wordRepository: WordRepository
) : QuizRepository {
    private val _quizList: BehaviorSubject<List<Word>> = BehaviorSubject.create<List<Word>>()
    override val quizList: Observable<List<Word>> = _quizList

    override fun resetQuizList(dictionaryId: Long, quizType: QuizTypes): Completable {
        return when (quizType) {
            QuizTypes.FullQuiz -> resetFullQuizList(dictionaryId)
            QuizTypes.QuickQuiz -> resetQuickQuizList(dictionaryId)
            QuizTypes.WeakestQuiz -> resetWeakestFive(dictionaryId)
        }.toCompletable()
    }

    private fun resetFullQuizList(dictionaryId: Long): Single<List<Word>> {
        return wordRepository.getObservableWordList(dictionaryId)
                .firstOrError()
                .doOnSuccess {
                    _quizList.onNext(it)
                }
    }

    private fun resetQuickQuizList(dictionaryId: Long): Single<List<Word>> {
        return wordRepository.getObservableWordList(dictionaryId)
                .firstOrError()
                .map { it.shuffled() }
                .map { it.take(5) }
                .doOnSuccess {
                    _quizList.onNext(it)
                }
    }

    private fun resetWeakestFive(dictionaryId: Long): Single<List<Word>> {
        return wordRepository.getObservableWordList(dictionaryId)
                .firstOrError()
                .map { sortWeaknessesList(it)}
                .map { it.take(5) }
                .doOnSuccess {
                    _quizList.onNext(it)
                }
    }

    override fun updateQuizList(list: List<Word>) {
        _quizList.onNext(list)
    }

    private fun sortWeaknessesList(list: List<Word>) :List<Word> {
        // Sort by failed / beenAsked ratio. If it's the same then take the one where beenAsked is more
        return list.map {
            when {
                it.beenAsked != 0 -> it to (it.failed.toFloat() / it.beenAsked.toFloat())
                else -> it to 0f
            }
        }.sortedWith(compareBy({ (_, value) -> value }, { (key, _) -> key.beenAsked })).toMap().keys.reversed()
    }
}