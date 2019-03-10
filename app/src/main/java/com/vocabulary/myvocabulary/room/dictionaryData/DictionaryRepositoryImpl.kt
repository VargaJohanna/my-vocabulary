package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class DictionaryRepositoryImpl(
        private val dictionaryDao: DictionaryDao,
        private val rxSchedulers: RxSchedulers

) : DictionaryRepository {
    private val _allDictionaries = BehaviorSubject.create<List<Dictionary>>()
    override val allDictionaries: Observable<List<Dictionary>> = _allDictionaries
    private val _numberOfDictionaries = BehaviorSubject.create<Int>()
    override val numberOfDictionaries: Observable<Int> = _numberOfDictionaries

    init {
        dictionaryDao.getAllDictionaries()
                .map { list ->
                    list.map { it.toDictionary() }
                }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { _allDictionaries.onNext(it) }

        dictionaryDao.getNumberOfDictionaries()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { _numberOfDictionaries.onNext(it) }
    }

    override fun createDictionary(dictionary: Dictionary): Long {
        return dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())
    }

    override fun deleteDictionary(dictionary: Dictionary) = dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())

    override fun updateDictionary(dictionary: Dictionary) = dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())

    override fun getDictionaryById(dictionaryId: Long): Single<Dictionary> {
        return dictionaryDao.getDictionaryById(dictionaryId).map {
            it.toDictionary()
        }
    }
}