package com.vocabulary.myvocabulary.di

import com.vocabulary.myvocabulary.room.AppDatabase
import com.vocabulary.myvocabulary.room.dictionaryData.DefaultDictionaryData
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepositoryImpl
import com.vocabulary.myvocabulary.room.wordData.DefaultWordList
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.room.wordData.WordRepositoryImpl
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.rx.SchedulersImpl
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.ui.quizzes.QuizViewModel
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.ui.words.WordListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().dictionaryDao() }
    single { get<AppDatabase>().wordDao() }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get(), get()) }
    single<WordRepository> { WordRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { DictionaryListViewModel(get(), get()) }
    viewModel { (dictionaryId: Long) -> WordListViewModel(dictionaryId, get(), get()) }
    viewModel { (dictionaryId: Long, optionType: Int) -> QuizViewModel(dictionaryId, optionType, get(), get()) }
    viewModel { (directionType: Int, dictionaryId: Long) -> ResultViewModel(directionType, dictionaryId, get(), get()) }
}

val schedulerModule = module {
    factory<RxSchedulers> { SchedulersImpl() }
}

val defaultDataModule = module {
    single { DefaultDictionaryData() }
    single { DefaultWordList() }
}
