package com.vocabulary.myvocabulary.repositories.search

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchRepositoryImplTest {

    @Test
    fun `should reset searched term when setSearchedTerm() is called`() = runTest {
        val searchRepository = givenSearchRepository()
        
        searchRepository.searchedTerm.test {
            assertEquals("", awaitItem())
            searchRepository.setSearchedTerm("test")
            assertEquals("test", awaitItem())
        }
    }

    @Test
    fun `should reset search bar status when saveSearchBarStatus() is called`() = runTest {
        val searchRepository = givenSearchRepository()
        
        searchRepository.showSearchBar().test {
            assertEquals(false, awaitItem())
            searchRepository.saveSearchBarStatus(true)
            assertEquals(true, awaitItem())
        }
    }

    private fun givenSearchRepository() = SearchRepositoryImpl()
}
