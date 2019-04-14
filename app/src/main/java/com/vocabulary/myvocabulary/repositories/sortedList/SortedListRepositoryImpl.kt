package com.vocabulary.myvocabulary.repositories.sortedList

import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.repositories.sortBy.SortByOptions
import com.vocabulary.myvocabulary.repositories.sortBy.toSortByOption
import io.reactivex.Observable
import io.reactivex.functions.Function3

class SortedListRepositoryImpl(
        private val wordRepository: WordRepository,
        private val sortByRepository: SortByRepository
) : SortedListRepository {

    override fun getSortedWordList(dictionaryId: Long): Observable<List<Word>> {
        return Observable.combineLatest(
                wordRepository.getObservableWordList(dictionaryId),
                sortByRepository.sortBy,
                sortByRepository.sortDirection,
                Function3 { list, sort, descending ->
                    when (sort.toSortByOption()) {
                        SortByOptions.SortByTranslation ->
                            if (descending) list.sortedWith(compareByDescending { it.translation })
                            else list.sortedWith(compareBy { it.translation })

                        SortByOptions.SortByWord ->
                            if (descending) list.sortedWith(compareByDescending { it.word })
                            else list.sortedWith(compareBy { it.word })

                        SortByOptions.SortByDate ->
                            if (descending) list.sortedWith(compareByDescending { it.created })
                            else list.sortedWith(compareBy { it.created })
                    }
                }
        )
    }
}