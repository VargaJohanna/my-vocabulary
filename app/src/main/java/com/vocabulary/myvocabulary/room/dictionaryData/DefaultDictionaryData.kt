package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import java.util.*

class DefaultDictionaryData {
    fun getDefaultDictionary() =
            Dictionary(dictionaryId = 1L, dictionaryName = "Hungarian Animals", dictionaryCreated = Calendar.getInstance().time)
}