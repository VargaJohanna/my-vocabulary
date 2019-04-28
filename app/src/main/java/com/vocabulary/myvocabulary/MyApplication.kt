package com.vocabulary.myvocabulary

import android.app.Application
import com.vocabulary.myvocabulary.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(networkModule, repositoryModule, viewModelModule, schedulerModule, factoryModule)
        }
    }
}