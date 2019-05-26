package com.vocabulary.myvocabulary.repositories.share

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.Single

interface ShareDictionaryRepository {
    fun storeCsvData(data: Uri)
    fun getCsvData(): Uri?
    fun setIsImport(import: Boolean)
    fun getImport(): Observable<Boolean>
}