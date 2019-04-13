package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.SortByDirection
import com.vocabulary.myvocabulary.utils.SortByOptions
import com.vocabulary.myvocabulary.utils.toSortByOption
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3

class SortedListRepositoryImpl(
        private val wordRepository: WordRepository,
        private val sortByRepository: SortByRepository
) : SortedListRepository {

    override fun getSortedWordList(dictionaryId: Long): Observable<List<Word>> {
        return Observable.combineLatest(
                wordRepository.getObservableWordList(dictionaryId),
                sortByRepository.sortBy,
                BiFunction { list, sort ->
                    when (sort.toSortByOption()) {
                        SortByOptions.SortByTranslation -> {
                            if (sortByRepository.sortDirection == SortByDirection.SortDecrease) {
                                list.sortedWith(compareBy { it.translation })
                            } else {
                                list.sortedWith(compareBy { it.translation }).reversed()
                            }
                        }
                        SortByOptions.SortByWord -> {
                            if (sortByRepository.sortDirection == SortByDirection.SortDecrease) {
                                list.sortedWith(compareBy { it.word })
                            } else {
                                list.sortedWith(compareBy { it.word }).reversed()
                            }
                        }
                        SortByOptions.SortByDate -> {
                            if (sortByRepository.sortDirection == SortByDirection.SortDecrease) {
                                list.sortedWith(compareBy { it.created }).reversed()
                            } else {
                                list.sortedWith(compareBy { it.word })
                            }

                        }
                    }
                }
        )
    }
}