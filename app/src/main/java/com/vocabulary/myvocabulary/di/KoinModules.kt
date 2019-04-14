package com.vocabulary.myvocabulary.di

import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.vocabulary.myvocabulary.repositories.AppDatabase
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepositoryImpl
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepositoryImpl
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepositoryImpl
import com.vocabulary.myvocabulary.repositories.word.*
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.rx.SchedulersImpl
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepositoryImpl
import com.vocabulary.myvocabulary.ui.quizzes.QuizViewModel
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.ui.words.WordDetailsViewModel
import com.vocabulary.myvocabulary.ui.words.WordListViewModel
import com.vocabulary.myvocabulary.utils.DialogFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().dictionaryDao() }
    single { get<AppDatabase>().wordDao() }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get(), get()) }
    single<WordRepository> { WordRepositoryImpl(get()) }
    single<QuizRepository> { QuizRepositoryImpl(get()) }
    single<SortByRepository> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(get())
        SortByRepositoryImpl(preferences,
        RxSharedPreferences.create(preferences))}
    single<SortedListRepository> { SortedListRepositoryImpl(get(), get()) }
}

val viewModelModule = module {
    viewModel { DictionaryListViewModel(get(), get(), get()) }
    viewModel { (dictionaryId: Long) -> WordListViewModel(dictionaryId, get(), get(), get(), get(), get()) }
    viewModel { (dictionaryId: Long, optionType: Int, failedOnly: Boolean) ->
        QuizViewModel(
                dictionaryId,
                optionType,
                failedOnly,
                get(),
                get())
    }
    viewModel { (dictionaryId: Long) -> ResultViewModel(dictionaryId, get(), get(), get()) }
    viewModel { WordDetailsViewModel(get(), get()) }
}

val schedulerModule = module {
    factory<RxSchedulers> { SchedulersImpl() }
}

val factoryModule = module {
    single { DialogFactory() }
}

