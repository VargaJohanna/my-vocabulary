package com.vocabulary.myvocabulary

import android.app.Application
import com.vocabulary.myvocabulary.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@MyApplication)
            modules(repositoryModule)
        }
    }
}