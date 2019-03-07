package com.vocabulary.myvocabulary.room.dictionaryData

import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import java.util.*

class DefaultDictionary {
    fun getDefaultDictionary() =
            Dictionary(1, "Hungarian Animals", Calendar.getInstance().time)
}