package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizRepository
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.util.*

class DictionaryListViewModelTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    private val dictionaryRepository = mock<DictionaryRepository>()
    private val quizRepository = mock<QuizRepository>()
    private val newDictionaryId = 5L

    @Test
    fun `should create dictionary when insertDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = Dictionary(1, "Test", Date(12))

        dictionaryListViewModel.insertDictionary(dictionaryWithId)

        verify(dictionaryRepository).createDictionary(dictionaryWithId)
    }

    @Test
    fun `should update newlyCreatedItemDetails when insertDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionary = Dictionary(dictionaryName = "Test", dictionaryCreated = Date(12))

        dictionaryListViewModel.insertDictionary(dictionary)

        Assert.assertEquals(DictionaryDetails(newDictionaryId, dictionary.dictionaryName),
                dictionaryListViewModel.newlyCreatedItemDetails.value?.peekContent())
    }

    @Test
    fun `should create dictionary object with given name when createDictionaryObject() is called`(){
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryName = "Hungarian"

        val dictionary = dictionaryListViewModel.createDictionaryObject(dictionaryName)

        Assert.assertEquals(dictionaryName, dictionary.dictionaryName)
    }

    @Test
    fun `should update dictionary when renameDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryToUpdate = Dictionary(dictionaryName = "Updated", dictionaryCreated = Date(12))

        dictionaryListViewModel.renameDictionary(dictionaryToUpdate)

        verify(dictionaryRepository).updateDictionary(dictionaryToUpdate)
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryWithId = Dictionary(1, "Test", Date(12))

        dictionaryListViewModel.deleteDictionary(dictionaryWithId)

        verify(dictionaryRepository).deleteDictionary(dictionaryWithId)
    }

    private fun givenDictionaryListViewModel(): DictionaryListViewModel {
        whenever(dictionaryRepository.createDictionary(any())).thenReturn(newDictionaryId)
        whenever(dictionaryRepository.allDictionaries).thenReturn(Observable.never())
        return DictionaryListViewModel(dictionaryRepository, TestScheduler(), quizRepository)
    }
}