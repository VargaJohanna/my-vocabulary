package com.vocabulary.myvocabulary.room.wordData

import com.vocabulary.myvocabulary.ui.words.Word
import java.util.*

class DefaultWordList {
    fun getDefaultWordList(): List<Word> =
            listOf(Word(1, 1, "cat", "cica", 0, 0, 0, Calendar.getInstance().time),
                    Word(2, 1, "dog", "kutya", 0, 0, 0, Calendar.getInstance().time),
                    Word(3, 1, "horse", "ló", 0, 0, 0, Calendar.getInstance().time))
}