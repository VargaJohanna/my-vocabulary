package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import org.junit.Assert
import org.junit.Test
import java.util.*

class DictionaryKtTest {

    @Test
    fun `should convert dictionary to dictionaryEntry when id is 0`() {
        val dictionary = Dictionary(dictionaryId = 0L, dictionaryName = "Test", dictionaryCreated = Date(12))

        val result = dictionary.toDictionaryEntry()

        Assert.assertEquals(DictionaryEntry(dictionaryId = 0L, dictionaryName = "Test", dictionaryCreated = Date(12)), result)
    }

    @Test
    fun `should convert dictionary to dictionaryEntry when id is 2`() {
        val dictionary = Dictionary(dictionaryId = 2L, dictionaryName = "Test", dictionaryCreated = Date(12))

        val result = dictionary.toDictionaryEntry()

        Assert.assertEquals(DictionaryEntry(dictionaryId = 2L, dictionaryName = "Test", dictionaryCreated = Date(12)), result)
    }
}