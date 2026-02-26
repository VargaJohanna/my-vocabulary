package com.vocabulary.myvocabulary.repositories.dictionary

import android.util.Log
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class DictionaryRepositoryImpl(
        private val dictionaryDao: DictionaryDao,
        private val rxSchedulers: RxSchedulers

) : DictionaryRepository {
    private val _allDictionaries = BehaviorSubject.create<List<Dictionary>>()
    override val allDictionaries: Observable<List<Dictionary>> = _allDictionaries
    private val _allDictionariesFlow : MutableStateFlow<List<Dictionary>> = MutableStateFlow(emptyList())
    override val allDictionariesFlow : StateFlow<List<Dictionary>> = _allDictionariesFlow
    override val numberOfDictionaries: Observable<Int> = allDictionaries.map { it.size }

    init {
        // Because DictionaryRepository is a singleton we don't need to dispose. It will stop when the application stops.
        // (see in com/vocabulary/myvocabulary/di/KoinModules.kt)
        dictionaryDao.getAllDictionaries()
                .map { list ->
                    list.map { it.toDictionary() }
                }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe (
                    {
                        _allDictionaries.onNext(it)
                        _allDictionariesFlow.value = it},
                    { error -> Log.e("Repo", "Database error", error) })
    }

    override fun createDictionary(dictionary: Dictionary) = dictionaryDao.insertDictionary(dictionary.toDictionaryEntry())

    override fun deleteDictionary(dictionary: Dictionary) = dictionaryDao.deleteDictionary(dictionary.toDictionaryEntry())

    override fun updateDictionary(dictionary: Dictionary) = dictionaryDao.updateDictionary(dictionary.toDictionaryEntry())

    override fun getDictionaryById(dictionaryId: Long): Single<Dictionary> {
        return allDictionaries.map {
            it.first {
                it.dictionaryId == dictionaryId
            }
        }
                .firstOrError()
    }

    override fun onQuizFinished(dictionaryId: Long?) {
        val currentDate = Calendar.getInstance().time
        dictionaryId?.let { id ->
            Completable.fromAction {
                dictionaryDao.updateLastPracticed(id, currentDate)
            }
                .subscribeOn(rxSchedulers.io())
                .subscribe(
                    {}, // Success: Do nothing
                    { error -> Log.e("Repo", "Error updating last practiced", error) }
                )
        }
    }

    override fun saveQuizStats(id: Long, scorePercentage: Int) {
        val now = Calendar.getInstance().time
        Completable.fromAction {
            dictionaryDao.updateDictionaryStats(id, now, scorePercentage)
        }
            .subscribeOn(rxSchedulers.io())
            .subscribe(
                {}, // Success: Do nothing
                { error -> Log.e("Repo", "Error saving quiz stats", error) }
            )
    }
}