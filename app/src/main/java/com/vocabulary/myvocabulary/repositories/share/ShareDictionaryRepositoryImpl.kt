package com.vocabulary.myvocabulary.repositories.share

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class ShareDictionaryRepositoryImpl : ShareDictionaryRepository {
    private val _import = BehaviorSubject.create<Boolean>()
    private val import: Observable<Boolean> = _import

    override fun setIsImport(import: Boolean) {
        _import.onNext(import)
    }

    override fun getImport(): Observable<Boolean> {
        return import
    }

    private var csvData: Uri? = null

    override fun getCsvData(): Uri? {
        return csvData
    }

    override fun storeCsvData(data: Uri) {
        csvData = data
    }
}