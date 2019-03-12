package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class DictionaryRepositoryImpl(
        private val dictionaryDao: DictionaryDao,
        private val rxSchedulers: RxSchedulers

) : DictionaryRepository {
    private val _allDictionaries = BehaviorSubject.create<List<Dictionary>>()
    override val allDictionaries: Observable<List<Dictionary>> = _allDictionaries
    override val numberOfDictionaries: Observable<Int> = allDictionaries.map { it.size }

    init {
        dictionaryDao.getAllDictionaries()
                .map { list ->
                    list.map { it.toDictionary() }
                }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { _allDictionaries.onNext(it) }
    }

    override fun createDictionary(dictionary: Dictionary) = dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())

    override fun deleteDictionary(dictionary: Dictionary) = dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())

    override fun updateDictionary(dictionary: Dictionary) = dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())

    override fun getDictionaryById(dictionaryId: Long): Observable<Dictionary> {
        return allDictionaries.map {
            it.first {
                it.dictionaryId == dictionaryId
            }
        }
    }
}