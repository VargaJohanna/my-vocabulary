package com.vocabulary.myvocabulary.repositories.dictionary

import com.nhaarman.mockitokotlin2.*
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.util.*

class DictionaryRepositoryImplTest {
    val dictionaryTest = Dictionary(
        dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = null,
        dictionaryLastResult = null,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 0
    )

    val dictionaryList = listOf(
        Dictionary(
            dictionaryId = 0L,
            dictionaryName = "Test",
            dictionaryCreated = Date(12),
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        ),
        Dictionary(
            dictionaryId = 1L,
            dictionaryName = "Test2",
            dictionaryCreated = Date(12),
            dictionaryLastPracticed = null,
            dictionaryLastResult = null,
            dictionaryFinishedCount = 0,
            dictionaryTotalScore = 0
        )
    )

    val entryList = listOf(
        DictionaryEntry(dictionaryId = 0L, dictionaryName = "Test", dictionaryCreated = Date(12)),
        DictionaryEntry(dictionaryId = 1L, dictionaryName = "Test2", dictionaryCreated = Date(12))
    )

    private val dictionaryDao = mock<DictionaryDao>()

    @Test
    fun `should create dictionary when createDictionary() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionary = dictionaryTest
            dictionaryRepository.createDictionary(dictionary)

            verify(dictionaryDao).insertDictionary(dictionary.toDictionaryEntry())
        }
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionary = dictionaryTest

            dictionaryRepository.deleteDictionary(dictionary)

            verify(dictionaryDao).deleteDictionary(dictionary.toDictionaryEntry())
        }
    }

    @Test
    fun `should update dictionary when updateDictionary() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionary = dictionaryTest

            dictionaryRepository.updateDictionary(dictionary)

            verify(dictionaryDao).updateDictionary(dictionary.toDictionaryEntry())
        }
    }

    @Test
    fun `should return a list of dictionaries`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepositoryWithDaoData()

            val result = dictionaryRepository.allDictionaries.first()

            assertThat(result).isEqualTo(dictionaryList)
        }
    }

    @Test
    fun `should find dictionary with given id when getDictionaryById() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepositoryWithDaoData()
            val dictionary = dictionaryList[0]

            val result = dictionaryRepository.getDictionaryById(dictionary.dictionaryId)

            assertThat(result).isEqualTo(dictionary)
        }
    }

    @Test
    fun `should not find dictionary with given id when getDictionaryById() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepositoryWithDaoData()
            // Use an id that does not exist in entryList
            val dictionaryId = 99L

            assertThatThrownBy {
                runBlocking { dictionaryRepository.getDictionaryById(dictionaryId) }
            }.isInstanceOf(NoSuchElementException::class.java)
        }
    }

    @Test
    fun `should update last practiced when onQuizFinished() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionaryId = 42L

            dictionaryRepository.onQuizFinished(dictionaryId)

            verify(dictionaryDao).updateLastPracticed(eq(dictionaryId), any())
        }
    }

    @Test
    fun `should not call updateLastPracticed when onQuizFinished() is called with null id`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepository()

            dictionaryRepository.onQuizFinished(null)

            verify(dictionaryDao, never()).updateLastPracticed(any(), any())
        }
    }

    @Test
    fun `should save quiz stats when saveQuizStats() is called`() {
        runBlocking {
            val dictionaryRepository = givenDictionaryRepository()
            val dictionaryId = 7L
            val score = 85

            dictionaryRepository.saveQuizStats(dictionaryId, score)

            verify(dictionaryDao).updateDictionaryStats(eq(dictionaryId), any(), eq(score))
        }
    }

    private fun givenDictionaryRepository(): DictionaryRepository {
        whenever(dictionaryDao.getAllDictionaries()).thenReturn(flowOf(emptyList()))
        return DictionaryRepositoryImpl(dictionaryDao)
    }

    private fun givenDictionaryRepositoryWithDaoData(): DictionaryRepository {
        whenever(dictionaryDao.getAllDictionaries()).thenReturn(flowOf(entryList))
        return DictionaryRepositoryImpl(dictionaryDao)
    }
}
