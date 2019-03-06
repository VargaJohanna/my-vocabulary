package com.vocabulary.myvocabulary.di

import com.vocabulary.myvocabulary.room.AppDatabase
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().dictionaryDao() }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get()) }
}