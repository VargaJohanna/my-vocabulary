package com.vocabulary.myvocabulary.repositories.share

import android.net.Uri
import io.reactivex.Observable

interface ShareDictionaryRepository {
    fun storeCsvData(data: Uri)
    fun getCsvUri(): Uri?
    fun setIsImport(import: Boolean)
    fun getImport(): Observable<Boolean>
}