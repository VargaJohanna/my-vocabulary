package com.vocabulary.myvocabulary.repositories.share

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ShareDictionaryRepository {
    fun storeCsvData(data: Uri)
    fun getCsvUri(): Uri?
    fun setIsImport(import: Boolean)
    fun getImport(): Flow<Boolean>
}