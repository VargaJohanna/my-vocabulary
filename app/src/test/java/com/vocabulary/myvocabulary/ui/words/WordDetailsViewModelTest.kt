package com.vocabulary.myvocabulary.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*

class WordDetailsViewModelTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()
    private val wordRepository = mock<WordRepository>()
    private val requestedId = 1L
    private val singleWord = Word(wordId = requestedId, containerDictionaryId = 2L, word = "a", translation = "a", created = Date(2019, 4, 4, 4, 4))

    @Test
    fun `should delegate to repository when getWordById() is called`() {
        val wordDetailsViewModel = givenWordDetailsViewModel()
        val requestedId = requestedId

        wordDetailsViewModel.getWordById(requestedId)

        verify(wordRepository).getWordById(requestedId)
    }

    @Test
    fun `should update currentWordLive when getWordById() is called`() {
        val wordDetailsViewModel = givenWordDetailsViewModel()
        wordDetailsViewModel.getWordById(requestedId)

        wordDetailsViewModel.getCurrentWord().observeForever(mock())

        assertEquals(singleWord, wordDetailsViewModel.getCurrentWord().value)
    }

    private fun givenWordDetailsViewModel(): WordDetailsViewModel {
        whenever(wordRepository.getWordById(requestedId)).thenReturn(Single.just(singleWord))
        return WordDetailsViewModel(wordRepository, TestScheduler())
    }
}