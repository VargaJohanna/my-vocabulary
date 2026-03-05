//package com.vocabulary.myvocabulary.ui.quizzes
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.nhaarman.mockitokotlin2.mock
//import com.nhaarman.mockitokotlin2.whenever
//import .TestScheduler
//import com.vocabulary.myvocabulary.repositories.quiz.QuizRepository
//import com.vocabulary.myvocabulary.ui.words.Word
//import io.reactivex.Observable
//import org.junit.Assert.*
//import org.junit.Rule
//import org.junit.Test
//import java.util.*
//import java.util.Arrays.asList
//
//class QuizViewModelTest {
//    @Rule
//    @JvmField
//    var mockito = InstantTaskExecutorRule()
//
//    private val quizRepository = mock<QuizRepository>()
//    private val dictionaryId = 1L
//    private val optionType = 1
//    private val date = Date(5)
//    private val quizList = asList(
//            Word(containerDictionaryId = dictionaryId, word = "a", translation = "translation", created = date, lastResult = true),
//            Word(containerDictionaryId = dictionaryId, word = "b", translation = "translation2", created = date, lastResult = true),
//            Word(containerDictionaryId = dictionaryId, word = "c", translation = "translation3", created = date, lastResult = false)
//    )
//    private val focusableWordListSize2 = asList(
//            QuizViewModel.FocusableWord(Word(containerDictionaryId = dictionaryId, word = "focus1", translation = "translation1", created = date), false),
//            QuizViewModel.FocusableWord(Word(containerDictionaryId = dictionaryId, word = "focus2", translation = "translation2", created = date), false)
//    )
//    private val focusableWordListSize3 = asList(
//            QuizViewModel.FocusableWord(Word(containerDictionaryId = dictionaryId, word = "focus1", translation = "translation1", created = date), false),
//            QuizViewModel.FocusableWord(Word(containerDictionaryId = dictionaryId, word = "focus2", translation = "translation2", created = date), false),
//            QuizViewModel.FocusableWord(Word(containerDictionaryId = dictionaryId, word = "focus3", translation = "translation3", created = date), false)
//    )
//    private val wordOtherDictionaryFailed = Word(containerDictionaryId = 2L, word = "a", translation = "translation", created = date, lastResult = false)
//    private val wordPassed = Word(containerDictionaryId = dictionaryId, word = "a", translation = "translation", created = date, lastResult = true)
//    private val wordFailed = Word(containerDictionaryId = dictionaryId, word = "a", translation = "translation", created = date, lastResult = false)
//    private var quizListToTest = mutableListOf<Word>()
//
//    @Test
//    fun `isDictionaryEmpty should be false when list is not empty`() {
//        quizListToTest = quizList
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//
//        assertEquals(false, quizViewModel.isDictionaryEmpty)
//    }
//
//    @Test
//    fun `isDictionaryEmpty should be true when list is empty`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//
//        assertTrue(quizViewModel.isDictionaryEmpty)
//    }
//
//    @Test
//    fun `should not add word when dictionaryId is different`() {
//        quizListToTest = asList(wordOtherDictionaryFailed)
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//
//        assertEquals(0, quizViewModel.getFocusableWordList().size)
//    }
//
//    @Test
//    fun `should add word when failedOnly and lastResult are false`() {
//        quizListToTest = asList(wordFailed)
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//
//        assertEquals(1, quizViewModel.getFocusableWordList().size)
//    }
//
//    @Test
//    fun `should not add word when failedOnly and lastResult are true`() {
//        quizListToTest = asList(wordPassed)
//        val quizViewModel = givenQuizViewModelFailedOnlyTrue()
//
//        assertEquals(0, quizViewModel.getFocusableWordList().size)
//    }
//
//    @Test
//    fun `should add word when failedOnly is true and lastResult is false`() {
//        quizListToTest = asList(wordFailed)
//        val quizViewModel = givenQuizViewModelFailedOnlyTrue()
//
//        assertEquals(1, quizViewModel.getFocusableWordList().size)
//    }
//
//    @Test
//    fun `should add word when failedOnly is false and lastResult is true`() {
//        quizListToTest = asList(wordPassed)
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//
//        assertEquals(1, quizViewModel.getFocusableWordList().size)
//    }
//
//    @Test
//    fun `should return empty list failedOnly and lastResult are true`() {
//        quizListToTest = asList(wordPassed)
//        val quizViewModel = givenQuizViewModelFailedOnlyTrue()
//
//        quizViewModel.getLiveWordList().observeForever(mock())
//
//        assertTrue(quizViewModel.getLiveWordList().value?.size == 0)
//    }
//
//    @Test
//    fun `should return the word to be asked when getLiveWordList() is called`() {
//        quizListToTest = quizList
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//
//        quizViewModel.getLiveWordList().observeForever(mock())
//
//        assertTrue(quizViewModel.getLiveWordList().value?.size != 0)
//    }
//
//    @Test
//    fun `should set listIsFinished to true when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize2)
//
//        quizViewModel.nextClicked()
//
//        assertTrue(quizViewModel.getListIsFinished())
//    }
//
//    @Test
//    fun `should set updateIcon to true when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize2)
//        quizViewModel.nextClicked()
//
//        quizViewModel.getUpdateIcon().observeForever(mock())
//
//        assertTrue(quizViewModel.getUpdateIcon().value!!)
//    }
//
//    @Test
//    fun `should set listIsFinished to false when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize3)
//
//        quizViewModel.nextClicked()
//
//        assertFalse(quizViewModel.getListIsFinished())
//    }
//
//    @Test
//    fun `should set updateIcon to false when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize3)
//
//        quizViewModel.nextClicked()
//        quizViewModel.getUpdateIcon().observeForever(mock())
//
//        assertFalse(quizViewModel.getUpdateIcon().value!!)
//    }
//
//    @Test
//    fun `isFocused should be true at index 0 and 1 when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize3)
//
//        quizViewModel.nextClicked()
//
//        assertTrue(quizViewModel.getFocusableWordList().get(0).isFocused)
//        assertTrue(quizViewModel.getFocusableWordList().get(1).isFocused)
//    }
//
//    @Test
//    fun `isFocused should be false at last index when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize3)
//
//        quizViewModel.nextClicked()
//
//        assertFalse(quizViewModel.getFocusableWordList().get(focusableWordListSize3.size - 1).isFocused)
//    }
//
//    @Test
//    fun `should update liveSubWordList when nextClicked() is called`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize2)
//        quizViewModel.nextClicked()
//
//        quizViewModel.getLiveWordList().observeForever(mock())
//
//        assertEquals(2, quizViewModel.getLiveWordList().value?.size)
//    }
//
//    @Test
//    fun `should update liveSubWordList when nextClicked() is called twice`() {
//        val quizViewModel = givenQuizViewModelFailedOnlyFalse()
//        quizViewModel.setFocusableWordList(focusableWordListSize3)
//        quizViewModel.nextClicked()
//        quizViewModel.nextClicked()
//
//        quizViewModel.getLiveWordList().observeForever(mock())
//
//        assertEquals(3, quizViewModel.getLiveWordList().value?.size)
//    }
//
//    private fun givenQuizViewModelFailedOnlyFalse(): QuizViewModel {
//        whenever(quizRepository.quizList).thenReturn(Observable.just(quizListToTest))
//        return QuizViewModel(dictionaryId = dictionaryId, optionType = optionType, failedOnly = false, rxSchedulers = TestScheduler(), quizRepository = quizRepository)
//    }
//
//    private fun givenQuizViewModelFailedOnlyTrue(): QuizViewModel {
//        whenever(quizRepository.quizList).thenReturn(Observable.just(quizListToTest))
//        return QuizViewModel(dictionaryId = dictionaryId, optionType = optionType, failedOnly = true, rxSchedulers = TestScheduler(), quizRepository = quizRepository)
//    }
//}