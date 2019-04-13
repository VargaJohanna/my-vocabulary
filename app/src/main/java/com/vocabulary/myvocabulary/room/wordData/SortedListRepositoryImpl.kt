package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.SortByOptions
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class SortedListRepositoryImpl(
        private val wordRepository: WordRepository,
        private val sortByRepository: SortByRepository
) : SortedListRepository {

    override fun getSortedWordList(dictionaryId: Long): Observable<List<Word>> {
        return Observable.combineLatest(
                wordRepository.getObservableWordList(dictionaryId),
                sortByRepository.sortBy,
                BiFunction { list, sort ->
                    when (sort) {
                        SortByOptions.SortByTranslation -> {
                            list
                                    .sortedWith(compareBy { it.translation })
                                    .sortedWith(compareBy { it.translation }).reversed()
                        }
                        SortByOptions.SortByWord -> {
                            list
                                    .sortedWith(compareBy { it.word })
                                    .sortedWith(compareBy { it.translation }).reversed()
                        }
                        SortByOptions.SortByDate -> {
                            list
                                    .sortedWith(compareBy { it.created }).reversed()
                                    .sortedWith(compareBy { it.translation }).sortedWith(compareBy { it.translation })
                        }
                    }
                }
        )
    }
}