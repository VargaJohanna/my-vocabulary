package com.vocabulary.myvocabulary.repositories.sortedList

import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Observable

interface SortedListRepository {
    fun getSortedWordList(dictionaryId: Long): Observable<List<Word>>

}