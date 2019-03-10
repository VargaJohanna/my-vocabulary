package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class WordRepositoryImpl(
        private val wordDao: WordDao,
        private val rxSchedulers: RxSchedulers
) : WordRepository {
    val _allWords = BehaviorSubject.create<List<Word>>()
    override val allWords: Observable<List<Word>> = _allWords
    val _isWordInDictionary = BehaviorSubject.create<Boolean>()
    override val isWordInDictionary: Observable<Boolean> = _isWordInDictionary

    override fun observeAllList(dictionaryId: Long) {
        wordDao.getAllWordsInDictionary(dictionaryId)
                .map { list ->
                    list.map { it.toWord() }
                }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { _allWords.onNext(it) }
    }

    override fun observeIfWordIsInDictionary(wordId: Long) {
        wordDao.getNumberOfWordById(wordId)
                .map { it != 0 }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { _isWordInDictionary.onNext(it) }
    }

    override fun createWord(word: Word) = wordDao.insertWord(word.toWordEntry())

    override fun deleteWord(word: Word) = wordDao.deleteWord(word.toWordEntry())

    override fun updateWord(word: Word) = wordDao.updateWord(word.toWordEntry())

    override fun getWordById(wordId: Long): Single<Word> {
        return wordDao.getWordById(wordId).map {
            it.toWord()
        }
    }
}