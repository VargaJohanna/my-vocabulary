package com.vocabulary.myvocabulary.repositories.dictionary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.TestScheduler
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.reactivex.Observable
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.Arrays.asList

class DictionaryRepositoryImplTest {
    val dictionaryTest = Dictionary(dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = Date(12),
        dictionaryLastResult = 0,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 100)

    val dictionaryList = listOf(
        Dictionary(
            dictionaryId = 0L,
            dictionaryName = "Test",
            dictionaryCreated = Date(12),
            dictionaryLastPracticed = Date(12),
            dictionaryLastResult = 0,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 100
        ),
        Dictionary(
            dictionaryId = 1L,
            dictionaryName = "Test2",
            dictionaryCreated = Date(12),
            dictionaryLastPracticed = Date(12),
            dictionaryLastResult = 0,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 100
        )
    )

    val entryList = listOf(
        DictionaryEntry(dictionaryId = 0L, dictionaryName = "Test", dictionaryCreated = Date(12)),
        DictionaryEntry(dictionaryId = 1L, dictionaryName = "Test2", dictionaryCreated = Date(12))
    )
        @Rule
        @JvmField
        var mockito = InstantTaskExecutorRule()
                private
                val dictionaryDao = mock<DictionaryDao>()

        @Test
        fun `should create dictionary when createDictionary() is called`() {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionary = dictionaryTest
            dictionaryRepository.createDictionary(dictionary)

            verify(dictionaryDao).insertDictionary(dictionary.toDictionaryEntry())
        }

        @Test
        fun `should delete dictionary when deleteDictionary() is called`() {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionary = dictionaryTest

            dictionaryRepository.deleteDictionary(dictionary)

            verify(dictionaryDao).deleteDictionary(dictionary.toDictionaryEntry())
        }

        @Test
        fun `should update dictionary when updateDictionary() is called`() {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionary = dictionaryTest

            dictionaryRepository.updateDictionary(dictionary)

            verify(dictionaryDao).updateDictionary(dictionary.toDictionaryEntry())
        }

        @Test
        fun `should return a list of dictionaries`() {
            val dictionaryRepository = givenDictionaryRepositoryWithDaoData()

            val testObserver = dictionaryRepository.allDictionaries.test()

            testObserver.assertValues(dictionaryList)
                .assertNotTerminated()
                .assertNoErrors()
                .dispose()
        }

        @Test
        fun `should find dictionary with given id when getDictionaryById() is called`() {
            val dictionaryRepository = givenDictionaryRepositoryWithDaoData()
            val dictionary = dictionaryTest

            val testObserver =
                dictionaryRepository.getDictionaryById(dictionary.dictionaryId).test()

            testObserver.assertValues(dictionary)
                .assertNoErrors()
                .dispose()

        }

        @Test
        fun `should not find dictionary with given id when getDictionaryById() is called`() {
            val dictionaryRepository = givenDictionaryRepositoryWithDaoData()
            val dictionary = dictionaryTest

            val testObserver =
                dictionaryRepository.getDictionaryById(dictionary.dictionaryId).test()

            testObserver.assertNever(dictionary)

        }

                private fun givenDictionaryRepository(): DictionaryRepository {
            whenever(dictionaryDao.getAllDictionaries()).thenReturn(Observable.never())
            return DictionaryRepositoryImpl(dictionaryDao, TestScheduler())
        }

                private fun givenDictionaryRepositoryWithDaoData(): DictionaryRepository {
            whenever(dictionaryDao.getAllDictionaries()).thenReturn(
                Observable.just(
                    entryList
                )
            )
            return DictionaryRepositoryImpl(dictionaryDao, TestScheduler())
        }
}