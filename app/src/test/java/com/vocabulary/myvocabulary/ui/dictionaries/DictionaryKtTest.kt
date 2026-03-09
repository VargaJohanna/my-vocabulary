package com.vocabulary.myvocabulary.ui.dictionaries

import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryEntry
import org.junit.Assert
import org.junit.Test
import java.util.*

class DictionaryKtTest {
    val dictionaryTest = Dictionary(
        dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = null,
        dictionaryLastResult = null,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 0
    )

    @Test
    fun `should convert dictionary to dictionaryEntry when id is 0`() {
        val dictionary = dictionaryTest

        val result = dictionary.toDictionaryEntry()

        Assert.assertEquals(
            DictionaryEntry(dictionaryId = 0L, dictionaryName = "Test", dictionaryCreated = Date(12)),
            result
        )
    }

    @Test
    fun `should convert dictionary to dictionaryEntry when id is 2`() {
        val dictionary = dictionaryTest.copy(dictionaryId = 2L)

        val result = dictionary.toDictionaryEntry()

        Assert.assertEquals(DictionaryEntry(dictionaryId = 2L, dictionaryName = "Test", dictionaryCreated = Date(12)), result)
    }
}