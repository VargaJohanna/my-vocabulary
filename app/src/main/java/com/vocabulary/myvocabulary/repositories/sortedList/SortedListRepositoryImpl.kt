package com.vocabulary.myvocabulary.repositories.sortedList

import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class SortedListRepositoryImpl(
        private val wordRepository: WordRepository,
        private val sortByRepository: SortByRepository,
        private val dictionaryRepository: DictionaryRepository
) : SortedListRepository {

    override fun getSortedWordList(dictionaryId: Long): Observable<List<Word>> {
        return Observable.combineLatest(
                wordRepository.getObservableWordList(dictionaryId),
                sortByRepository.sortByData(),
                BiFunction { list, sortData ->
                    when (sortData.sortByOption) {
                        SortByOptions.SortByTranslation ->
                            if (sortData.translationDescending) {
                                list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.translation })
                            } else {
                                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.translation })
                            }

                        SortByOptions.SortByWord ->
                            if (sortData.wordDescending) {
                                list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.word })
                            } else {
                                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.word })
                            }

                        SortByOptions.SortByDate ->
                            if (sortData.dateDescending) {
                                list.sortedWith(compareBy { it.created }).reversed()
                            } else {
                                list.sortedWith(compareBy { it.created })
                            }

                        else -> list
                    }
                }
        )
    }

    override fun getSortedDictionaryList(): Observable<List<Dictionary>> {
        return Observable.combineLatest(
                dictionaryRepository.allDictionaries,
                sortByRepository.sortByData(),
                BiFunction { list, sortData ->
                    when (sortData.sortByOption) {
                        SortByOptions.SortByDate ->
                            if (sortData.dateDescending) {
                                list.sortedWith(compareBy { it.dictionaryCreated }).reversed()
                            } else {
                                list.sortedWith(compareBy { it.dictionaryCreated })
                            }
                        else -> list
                    }
                }
        )
    }
}