package com.vocabulary.myvocabulary.repositories.word

import androidx.work.WorkManager
import com.nhaarman.mockitokotlin2.*
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*
import java.util.Arrays.asList

class WordRepositoryImplTest {

    private val wordDao = mock<WordDao>()
    private val requestedDictionary = 1L
    private val wordIdToTest = 1L
    private val wordToTest = Word(wordId = 1L, containerDictionaryId = requestedDictionary, word = "a", translation = "b", created = Date(5))
    private val wordList: List<Word> = listOf(
        Word(
            wordId = 1L,
            containerDictionaryId = requestedDictionary,
            word = "a",
            translation = "b",
            created = Date(5)
        ),
        Word(
            wordId = 2L,
            containerDictionaryId = requestedDictionary,
            word = "b",
            translation = "c",
            created = Date(5)
        ),
        Word(
            wordId = 3L,
            containerDictionaryId = requestedDictionary,
            word = "c",
            translation = "d",
            created = Date(5)
        )
    )

    @Test
    fun `should return a list of words of the given dictionary when getObservableWordList() is called`() {
        runBlocking {
            val wordRepository = givenWordRepositoryWithDao()

            val result = wordRepository.getObservableWordList(requestedDictionary).first()

            assertThat(result).isEqualTo(wordList)
        }
    }

    @Test
    fun `should return true when the requested word is in the dictionary`() {
        runBlocking {
            val wordRepository = givenWordRepositoryWithDao()

            val result = wordRepository.getIsWordInDictionary(wordIdToTest).first()

            assertThat(result).isTrue()
        }
    }

    @Test
    fun `should return false when the requested word is in the dictionary`() {
        runBlocking {
            val wordRepository = givenWordRepositoryWithNoWords()

            val result = wordRepository.getIsWordInDictionary(wordIdToTest).first()

            assertThat(result).isFalse()
        }
    }

    @Test
    fun `should create word when createWord() is called`() {
        val wordRepository = givenWordRepository()

        wordRepository.createWord(wordToTest)

        verify(wordDao).insertWord(wordToTest.toWordEntry())
    }

    @Test
    fun `should delete word when deleteWord() is called`() {
        val wordRepository = givenWordRepository()

        wordRepository.deleteWord(wordToTest)

        verify(wordDao).deleteWord(wordToTest.toWordEntry())
    }

    @Test
    fun `should update word when updateWord() is called`() {
        val wordRepository = givenWordRepository()

        wordRepository.updateWord(wordToTest)

        verify(wordDao).updateWord(wordToTest.toWordEntry())
    }

    @Test
    fun `should return word by requested id`() {
        runBlocking {
            val wordRepository = givenWordRepositoryWithWordById()

            wordRepository.getWordById(wordIdToTest)

            verify(wordDao).getWordById(wordIdToTest)
        }
    }

    private val workManager = mock<WorkManager>()

    private fun givenWordRepository(): WordRepositoryImpl {
        return WordRepositoryImpl(wordDao, workManager)
    }

    private fun givenWordRepositoryWithDao(): WordRepositoryImpl {
        whenever(wordDao.getNumberOfWordById(wordIdToTest)).thenReturn(flowOf(1))
        whenever(wordDao.getAllWordsInDictionary(requestedDictionary)).thenReturn(flowOf(wordList.map { it.toWordEntry() }))
        return WordRepositoryImpl(wordDao, workManager)
    }

    private fun givenWordRepositoryWithNoWords(): WordRepositoryImpl {
        whenever(wordDao.getNumberOfWordById(wordIdToTest)).thenReturn(flowOf(0))
        whenever(wordDao.getAllWordsInDictionary(requestedDictionary)).thenReturn(flowOf(wordList.map { it.toWordEntry() }))
        return WordRepositoryImpl(wordDao, workManager)
    }

    private fun givenWordRepositoryWithWordById(): WordRepositoryImpl {
        runBlocking {
            whenever(wordDao.getWordById(wordIdToTest)).thenReturn(wordToTest.toWordEntry())
        }
        return WordRepositoryImpl(wordDao, workManager)
    }
}
