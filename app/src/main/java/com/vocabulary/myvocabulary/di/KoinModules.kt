package com.vocabulary.myvocabulary.di

import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.vocabulary.myvocabulary.BuildConfig
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.network.QuoteService
import com.vocabulary.myvocabulary.repositories.AppDatabase
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepositoryImpl
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepository
import com.vocabulary.myvocabulary.repositories.guessedWord.GuessedWordRepositoryImpl
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepository
import com.vocabulary.myvocabulary.repositories.quiz.CustomQuizRepositoryImpl
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
import com.vocabulary.myvocabulary.repositories.quiz.QuizRepositoryImpl
import com.vocabulary.myvocabulary.repositories.quotes.*
import com.vocabulary.myvocabulary.repositories.search.SearchRepository
import com.vocabulary.myvocabulary.repositories.search.SearchRepositoryImpl
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepository
import com.vocabulary.myvocabulary.repositories.sortBy.SortByRepositoryImpl
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepository
import com.vocabulary.myvocabulary.repositories.sortedList.SortedListRepositoryImpl
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepositoryImpl
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.rx.SchedulersImpl
import com.vocabulary.myvocabulary.ui.dictionaries.DictionaryListViewModel
import com.vocabulary.myvocabulary.ui.home.HomeViewModel
import com.vocabulary.myvocabulary.ui.dictionaries.ShareDictionaryViewModel
import com.vocabulary.myvocabulary.ui.quizzes.QuizViewModel
import com.vocabulary.myvocabulary.ui.results.ResultViewModel
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepositoryImpl
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepository
import com.vocabulary.myvocabulary.repositories.sortBy.dictionary.SortDictionaryRepositoryImpl
import com.vocabulary.myvocabulary.ui.quizzes.QuizListViewModel
import com.vocabulary.myvocabulary.ui.words.WordDetailsViewModel
import com.vocabulary.myvocabulary.ui.words.WordListViewModel
import com.vocabulary.myvocabulary.utils.ComposeDialogFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

val repositoryModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().dictionaryDao() }
    single { get<AppDatabase>().wordDao() }
    single { get<AppDatabase>().quoteDao() }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get(), get()) }
    single<WordRepository> { WordRepositoryImpl(get()) }
    single<QuizRepository> { QuizRepositoryImpl(get(), get()) }
    single<SortByRepository> {
        SortByRepositoryImpl(get(), get())
    }
    single<SortDictionaryRepository> {
        SortDictionaryRepositoryImpl(get(), get())
    }
    single<SearchRepository> {
        SearchRepositoryImpl()
    }
    single<SortedListRepository> { SortedListRepositoryImpl(get(), get(), get(), get()) }
    single<GuessedWordRepository> { GuessedWordRepositoryImpl() }
    single<LocalQuoteRepository> { LocalQuoteRepositoryImpl(get()) }
    single<NetworkQuoteRepository> { NetworkQuoteRepositoryImpl(get()) }
    single<QuoteRepository> { QuoteRepositoryImpl(get(), get()) }
    single { PreferenceManager.getDefaultSharedPreferences(get()) }
    single { RxSharedPreferences.create(get()) }
    single<ShareDictionaryRepository> { ShareDictionaryRepositoryImpl() }
    single<CustomQuizRepository> { CustomQuizRepositoryImpl() }
}

val networkModule = module {
    single {
        Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-Api-Key", BuildConfig.API_KEY)
                .build()
            chain.proceed(request)
        }
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<Interceptor>())
            .build()
    }
    single{GsonConverterFactory.create()}
    single { Retrofit.Builder()
            .baseUrl(Constants.QOD_BASE_URL)
            .client(get())
            .addConverterFactory(get<GsonConverterFactory>())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build() }
    single{get<Retrofit>().create(QuoteService::class.java)}
}

val viewModelModule = module {
    viewModel { DictionaryListViewModel(get(), get(), get()) }
    viewModel { (dictionaryId: Long) -> WordListViewModel(dictionaryId, get(), get(), get(), get(), get(), get()) }
    viewModel { (dictionaryId: Long, optionType: Int, failedOnly: Boolean) ->
        QuizViewModel(
                dictionaryId,
                failedOnly,
                get(),
                get(),)
    }
    viewModel { (dictionaryId: Long, quizDirection: Int) -> ResultViewModel(dictionaryId, quizDirection, get(), get(), get(), get(), get()) }
    viewModel { (wordId: Long) -> WordDetailsViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { ShareDictionaryViewModel(get(), get(), get()) }
    viewModel { QuizListViewModel(get()) }
}

val schedulerModule = module {
    factory<RxSchedulers> { SchedulersImpl() }
}

val factoryModule = module {
    single { ComposeDialogFactory() }
}

