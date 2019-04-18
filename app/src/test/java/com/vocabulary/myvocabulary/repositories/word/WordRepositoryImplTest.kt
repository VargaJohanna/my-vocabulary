package com.vocabulary.myvocabulary.repositories.word

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.Arrays.asList

class WordRepositoryImplTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()
    private val wordDao = mock<WordDao>()
    private val requestedDictionary = 1L
    private val wordIdToTest = 1L
    private val wordToTest = Word(wordId = 1L, containerDictionaryId = requestedDictionary, word = "a", translation = "b", created = Date(2019, 4, 4, 4, 4))
    private val wordList: List<Word> = asList(
            Word(wordId = 1L, containerDictionaryId = requestedDictionary, word = "a", translation = "b", created = Date(2019, 4, 4, 4, 4)),
            Word(wordId = 2L, containerDictionaryId = requestedDictionary, word = "b", translation = "c", created = Date(2019, 4, 4, 4, 4)),
            Word(wordId = 3L, containerDictionaryId = requestedDictionary, word = "c", translation = "d", created = Date(2019, 4, 4, 4, 4))
    )

    @Test
    fun `should return a list of words of the given dictionary when getObservableWordList() is called`() {
        val wordRepository = givenWordRepositoryWithDao()

        val testObserver = wordRepository.getObservableWordList(requestedDictionary).test()

        testObserver.assertValues(wordList)
                .assertNoErrors()
                .dispose()
    }

    @Test
    fun `should return true when the requested word is in the dictionary`() {
        val wordRepository = givenWordRepositoryWithDao()

        val testObserver = wordRepository.getIsWordInDictionary(wordIdToTest).test()

        testObserver.assertValue(true)
                .assertNoErrors()
                .dispose()
    }

    @Test
    fun `should return false when the requested word is in the dictionary`() {
        val wordRepository = givenWordRepositoryWithNoWords()

        val testObserver = wordRepository.getIsWordInDictionary(wordIdToTest).test()

        testObserver.assertValue(false)
                .assertNoErrors()
                .dispose()
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
        val wordRepository = givenWordRepositoryWithWordById()

        wordRepository.getWordById(wordIdToTest)

        verify(wordDao).getWordById(wordIdToTest)
    }

    private fun givenWordRepository(): WordRepositoryImpl {
        return WordRepositoryImpl(wordDao)
    }

    private fun givenWordRepositoryWithDao(): WordRepositoryImpl {
        whenever(wordDao.getNumberOfWordById(wordIdToTest)).thenReturn(Observable.just(1))
        whenever(wordDao.getAllWordsInDictionary(requestedDictionary)).thenReturn(Observable.just(wordList.map { it.toWordEntry() }))
        return WordRepositoryImpl(wordDao)
    }

    private fun givenWordRepositoryWithNoWords(): WordRepositoryImpl {
        whenever(wordDao.getNumberOfWordById(wordIdToTest)).thenReturn(Observable.just(0))
        whenever(wordDao.getAllWordsInDictionary(requestedDictionary)).thenReturn(Observable.just(wordList.map { it.toWordEntry() }))
        return WordRepositoryImpl(wordDao)
    }

    private fun givenWordRepositoryWithWordById(): WordRepositoryImpl {
        whenever(wordDao.getWordById(wordIdToTest)).thenReturn(Single.just(wordToTest.toWordEntry()))
        return WordRepositoryImpl(wordDao)
    }
}