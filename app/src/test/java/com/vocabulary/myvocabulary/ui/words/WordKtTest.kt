package com.vocabulary.myvocabulary.ui.words

import com.vocabulary.myvocabulary.repositories.word.WordEntry
import org.junit.Assert
import org.junit.Test
import java.util.*

class WordKtTest {

    @Test
    fun `should convert to WordEntry when word id is 0`() {
        val word = Word(wordId = 0L,
                containerDictionaryId = 2L,
                word = "wheedle",
                translation = "hizeleg",
                created = Date(2019, 4, 18, 10, 10),
                beenAsked = 5,
                failed = 3,
                passed = 2,
                lastResult = true,
                lastGuess = "wheedle")

        val result = word.toWordEntry()

        Assert.assertEquals(WordEntry(wordId = 0L,
                containerDictionaryId = 2L,
                word = "wheedle",
                translation = "hizeleg",
                created = Date(2019, 4, 18, 10, 10),
                beenAsked = 5,
                failed = 3,
                passed = 2,
                lastResult = true,
                lastGuess = "wheedle"), result)
    }

    @Test
    fun `should convert to WordEntry when word id is 1`() {
        val word = Word(wordId = 1L,
                containerDictionaryId = 2L,
                word = "wheedle",
                translation = "hizeleg",
                created = Date(2019, 4, 18, 10, 10),
                beenAsked = 5,
                failed = 3,
                passed = 2,
                lastResult = true,
                lastGuess = "wheedle")

        val result = word.toWordEntry()

        Assert.assertEquals(WordEntry(wordId = 1L,
                containerDictionaryId = 2L,
                word = "wheedle",
                translation = "hizeleg",
                created = Date(2019, 4, 18, 10, 10),
                beenAsked = 5,
                failed = 3,
                passed = 2,
                lastResult = true,
                lastGuess = "wheedle"), result)
    }
}