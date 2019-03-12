package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import java.util.*

class DefaultDictionaryData {
    fun getDefaultDictionary() =
            Dictionary(dictionaryName = "Hungarian Animals", dictionaryCreated =  Calendar.getInstance().time)
}