package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import io.reactivex.Observable

class DictionaryListViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
//    val dictionary: Observable<Dictionary> = dictionaryRepository.getDictionaryById()
    val dictionaryList: Observable<List<Dictionary>> = dictionaryRepository.getAllDictionaries()
}
