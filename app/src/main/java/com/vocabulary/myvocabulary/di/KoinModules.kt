package com.vocabulary.myvocabulary.di

import com.vocabulary.myvocabulary.room.AppDatabase
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepositoryImpl
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.room.wordData.WordRepositoryImpl
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.rx.SchedulersImpl
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepositoryImpl
import com.vocabulary.myvocabulary.ui.quizzes.QuizViewModel
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.ui.words.WordDetailsViewModel
import com.vocabulary.myvocabulary.ui.words.WordListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().dictionaryDao() }
    single { get<AppDatabase>().wordDao() }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get(), get()) }
    single<WordRepository> { WordRepositoryImpl(get()) }
    single<QuizRepository> { QuizRepositoryImpl(get(), get()) }
}

val viewModelModule = module {
    viewModel { DictionaryListViewModel(get(), get(), get()) }
    viewModel { (dictionaryId: Long) -> WordListViewModel(dictionaryId, get(), get(), get()) }
    viewModel { (dictionaryId: Long, optionType: Int, failedOnly: Boolean, quizType: Int) ->
        QuizViewModel(
                dictionaryId,
                optionType,
                failedOnly,
                quizType,
                get(),
                get())
    }
    viewModel { (dictionaryId: Long) -> ResultViewModel(dictionaryId, get(), get(), get()) }
    viewModel { WordDetailsViewModel() }
}

val schedulerModule = module {
    factory<RxSchedulers> { SchedulersImpl() }
}
