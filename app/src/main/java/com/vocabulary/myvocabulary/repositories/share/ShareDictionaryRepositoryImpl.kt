package com.vocabulary.myvocabulary.repositories.share

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShareDictionaryRepositoryImpl : ShareDictionaryRepository {
    private val _import = MutableStateFlow(false)

    override fun setIsImport(import: Boolean) {
        _import.value = import
    }

    override fun getImport(): Flow<Boolean> = _import.asStateFlow()

    private var csvData: Uri? = null

    override fun getCsvUri() = csvData

    override fun storeCsvData(data: Uri) {
        csvData = data
    }
}
