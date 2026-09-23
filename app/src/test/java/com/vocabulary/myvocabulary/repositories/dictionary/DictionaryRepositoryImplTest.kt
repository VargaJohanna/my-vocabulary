package com.vocabulary.myvocabulary.repositories.dictionary

import android.util.Log
import androidx.work.WorkManager
import com.vocabulary.myvocabulary.repositories.AppDatabase
import com.vocabulary.myvocabulary.repositories.sync.CloudSyncRepository
import com.vocabulary.myvocabulary.repositories.word.WordDao
import com.vocabulary.myvocabulary.testing.TestDispatchers
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryRepositoryImplTest {

    private val dictionaryDao: DictionaryDao = mockk(relaxed = true)
    private val wordDao: WordDao = mockk(relaxed = true)
    private val cloudSyncRepository: CloudSyncRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val dispatchers = TestDispatchers(UnconfinedTestDispatcher())

    private val dictionaryTest = Dictionary(
        dictionaryName = "Test",
        dictionaryCreated = Date(12),
        dictionaryLastPracticed = null,
        dictionaryLastResult = null,
        dictionaryFinishedCount = 0,
        dictionaryTotalScore = 0
    )

    private val dictionaryList = listOf(
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

    private val entryList = listOf(
        DictionaryEntry(dictionaryId = 0L, dictionaryName = "Test", dictionaryCreated = Date(12)),
        DictionaryEntry(dictionaryId = 1L, dictionaryName = "Test2", dictionaryCreated = Date(12))
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `should create dictionary when createDictionary() is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepository()
        coEvery { dictionaryDao.insertDictionary(any()) } returns 1L

        dictionaryRepository.createDictionary(dictionaryTest)

        coVerify { dictionaryDao.insertDictionary(dictionaryTest.toDictionaryEntry()) }
    }

    @Test
    fun `should delete dictionary when deleteDictionary() is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepository()

        dictionaryRepository.deleteDictionary(dictionaryTest)

        coVerify { dictionaryDao.deleteDictionary(dictionaryTest.toDictionaryEntry()) }
    }

    @Test
    fun `should update dictionary when updateDictionary() is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepository()

        dictionaryRepository.updateDictionary(dictionaryTest)

        coVerify { dictionaryDao.updateDictionary(dictionaryTest.toDictionaryEntry()) }
    }

    @Test
    fun `should return a list of dictionaries`() = runTest {
        val dictionaryRepository = givenDictionaryRepositoryWithDaoData()

        val result = dictionaryRepository.allDictionaries.first()

        assertThat(result).isEqualTo(dictionaryList)
    }

    @Test
    fun `should find dictionary with given id when getDictionaryById() is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepositoryWithDaoData()
        val dictionary = dictionaryList[0]

        val result = dictionaryRepository.getDictionaryById(dictionary.dictionaryId)

        assertThat(result).isEqualTo(dictionary)
    }

    @Test
    fun `should update last practiced when onQuizFinished() is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepository()
        val dictionaryId = 5L

        dictionaryRepository.onQuizFinished(dictionaryId)

        coVerify { dictionaryDao.updateLastPracticed(eq(dictionaryId), any()) }
    }

    @Test
    fun `should save quiz stats when saveQuizStats() is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepository()
        val dictionaryId = 7L
        val score = 85

        dictionaryRepository.saveQuizStats(dictionaryId, score)

        coVerify { dictionaryDao.updateDictionaryStats(eq(dictionaryId), any(), eq(score)) }
    }

    @Test
    fun `should download dictionaries from cloud when syncFromCloud is called`() = runTest {
        val dictionaryRepository = givenDictionaryRepository()
        coEvery { cloudSyncRepository.downloadDictionaries("user_123") } returns Result.success(emptyList())

        val result = dictionaryRepository.syncFromCloud("user_123", requireWifi = false)

        assertThat(result.isSuccess).isTrue
        coVerify { cloudSyncRepository.downloadDictionaries("user_123") }
    }

    private fun givenDictionaryRepository(): DictionaryRepository {
        every { dictionaryDao.getAllDictionaries() } returns flowOf(emptyList())
        return DictionaryRepositoryImpl(dictionaryDao, wordDao, cloudSyncRepository, workManager, appDatabase, dispatchers)
    }

    private fun givenDictionaryRepositoryWithDaoData(): DictionaryRepository {
        every { dictionaryDao.getAllDictionaries() } returns flowOf(entryList)
        return DictionaryRepositoryImpl(dictionaryDao, wordDao, cloudSyncRepository, workManager, appDatabase, dispatchers)
    }
}
