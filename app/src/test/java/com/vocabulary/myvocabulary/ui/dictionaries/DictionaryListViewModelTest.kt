package com.vocabulary.myvocabulary.ui.dictionaries

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
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
    private val dictionary = Dictionary(dictionaryName = "Test", dictionaryCreated = Date(12))

    @Test
    fun `should create dictionary when insertDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionary = Dictionary(1, "Test", Date(12))

        dictionaryListViewModel.insertDictionary(dictionary)

        verify(dictionaryRepository).createDictionary(dictionary)
    }

    @Test
    fun `should update newlyCreatedItemDetails when insertDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()

        dictionaryListViewModel.insertDictionary(dictionary)

        Assert.assertEquals(DictionaryDetails(newDictionaryId, dictionary.dictionaryName),
                dictionaryListViewModel.newlyCreatedItemDetails.value?.peekContent())
    }

    @Test
    fun `should create dictionary object with given name when createDictionaryObject() is called`(){
        val dictionaryListViewModel = givenDictionaryListViewModel()
        val dictionaryName = "Hungarian"

        Assert.assertEquals(dictionaryName, dictionaryListViewModel.createDictionaryObject(dictionaryName).dictionaryName)
    }

    @Test
    fun `should rename dictionary when renameDictionary() is called`() {
        val dictionaryListViewModel = givenDictionaryListViewModel()

        dictionaryListViewModel.renameDictionary(dictionary)

    }

    private fun givenDictionaryListViewModel(): DictionaryListViewModel {
        whenever(dictionaryRepository.createDictionary(any())).thenReturn(newDictionaryId)
        whenever(dictionaryRepository.allDictionaries).thenReturn(Observable.never())
//        whenever(dictionaryRepository.updateDictionary(any())).thenReturn()
        return DictionaryListViewModel(dictionaryRepository, TestScheduler(), quizRepository)
    }
}