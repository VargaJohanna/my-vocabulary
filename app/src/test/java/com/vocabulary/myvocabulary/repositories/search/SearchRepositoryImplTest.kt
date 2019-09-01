package com.vocabulary.myvocabulary.repositories.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SearchRepositoryImplTest {
    @Rule
    @JvmField
    var mockito = InstantTaskExecutorRule()

    @Test
    fun `should reset searched term when setSearchedTerm() is called`() {
        val searchRepository = givenSearchRepository()
        searchRepository.setSearchedTerm("test")

        val testObserver = searchRepository.searchedTerm.test()

        testObserver.assertValue ("test")
    }

    @Test
    fun `should reset search bar status when saveSearchBarStatus() is called`() {
        val searchRepository = givenSearchRepository()
        searchRepository.saveSearchBarStatus(true)

        val testObserver = searchRepository.showSearchBar().test()

        testObserver.assertValue (true)
    }

    private fun givenSearchRepository() = SearchRepositoryImpl()
}