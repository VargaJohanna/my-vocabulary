package com.vocabulary.myvocabulary.room.dictionaryData

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.TestScheduler
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Observable
import org.junit.Rule
import org.junit.Test
import java.util.*

class DictionaryRepositoryImplTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()
    private val dictionaryDao = mock<DictionaryDao>()


    @Test
    fun `should create dictionary when createDictionary() is called`() {
        val dictionaryRepository = givenDictionaryRepository()
        val dictionary = Dictionary(dictionaryName = "Test", dictionaryCreated = Date(12))

        dictionaryRepository.createDictionary(dictionary)

        verify(dictionaryDao).insertDictionary(dictionary.toDictionaryEntry())
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() {
        val dictionaryRepository = givenDictionaryRepository()
        val dictionary = Dictionary(dictionaryName = "Test", dictionaryCreated = Date(12))

        dictionaryRepository.deleteDictionary(dictionary)

        verify(dictionaryDao).deleteDictionary(dictionary.toDictionaryEntry())
    }

    @Test
    fun `should update dictionary when updateDictionary() is called`() {
        val dictionaryRepository = givenDictionaryRepository()
        val dictionary = Dictionary(dictionaryName = "Test", dictionaryCreated = Date(12))

        dictionaryRepository.updateDictionary(dictionary)

        verify(dictionaryDao).updateDictionary(dictionary.toDictionaryEntry())
    }



    private fun givenDictionaryRepository(): DictionaryRepository {
        whenever(dictionaryDao.getAllDictionaries()).thenReturn(Observable.never())
        return DictionaryRepositoryImpl(dictionaryDao, TestScheduler())
    }
}