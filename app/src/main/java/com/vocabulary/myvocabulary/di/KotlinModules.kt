package com.vocabulary.myvocabulary.di

import com.vocabulary.myvocabulary.data.AppDatabase
import com.vocabulary.myvocabulary.data.dictionaryDatabase.DictionaryRepository
import com.vocabulary.myvocabulary.data.dictionaryDatabase.DictionaryRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().dictionaryDao() }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get()) }
}