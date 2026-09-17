package com.vocabulary.myvocabulary.repositories.sortedList

import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortByDictionaryOptions
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SortedListRepositoryImpl(
        private val wordRepository: WordRepository,
        private val sortByRepository: SortByRepository,
        private val sortByDictRepository: SortDictionaryRepository,
        private val dictionaryRepository: DictionaryRepository
) : SortedListRepository {

    override fun getSortedWordList(dictionaryId: Long): Flow<List<Word>> {
        return combine(
            wordRepository.getObservableWordList(dictionaryId),
            sortByRepository.sortByData()
        ) { wordList, sortData ->
            when (sortData.sortByOption) {
                SortByOptions.SortByTranslation ->
                    if (sortData.translationDescending) {
                        wordList.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.translation })
                    } else {
                        wordList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.translation })
                    }

                SortByOptions.SortByWord ->
                    if (sortData.wordDescending) {
                        wordList.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.word })
                    } else {
                        wordList.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.word })
                    }

                SortByOptions.SortByDate ->
                    if (sortData.dateDescending) {
                        wordList.sortedWith(compareBy { it.created }).reversed()
                    } else {
                        wordList.sortedWith(compareBy { it.created })
                    }
            }
        }
    }

    override fun getSortedDictionaryList(): Flow<List<Dictionary>> {
        return combine(
            dictionaryRepository.allDictionaries,
            sortByDictRepository.sortByData(),
        ) { list, sortData ->
            when (sortData.sortByOption) {
                SortByDictionaryOptions.SortByDate ->
                    if (sortData.dateDescending) {
                        list.sortedWith(compareBy { it.dictionaryCreated }).reversed()
                    } else {
                        list.sortedWith(compareBy { it.dictionaryCreated })
                    }
                SortByDictionaryOptions.SortByTitle ->
                    if (sortData.titleDescending) {
                        list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.dictionaryName })
                    } else {
                        list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.dictionaryName })
                    }
            }
        }
    }
}