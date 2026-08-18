package com.vocabulary.myvocabulary.repositories.quiz

import androidx.annotation.VisibleForTesting
import com.vocabulary.myvocabulary.Constants
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizTypes
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asFlow

class QuizRepositoryImpl(
        private val wordRepository: WordRepository,
        private val customQuizRepository: CustomQuizRepository
) : QuizRepository {
    private val _quizList = MutableStateFlow<List<Word>> (emptyList())
    override val quizList: Flow<List<Word>> = _quizList.asStateFlow()

//    override fun resetQuizList(dictionaryId: Long, quizType: QuizTypes): Completable {
//        return when (quizType) {
//            QuizTypes.FullQuiz -> getFullQuizList(dictionaryId)
//            QuizTypes.QuickQuiz -> getQuickQuizList(dictionaryId)
//            QuizTypes.WeakestQuiz -> getWeakestFive(dictionaryId)
//            QuizTypes.CustomQuiz -> resetCustomQuizList(dictionaryId)
//        }.ignoreElement()
//    }

    override suspend fun setQuizList(dictionaryId: Long, quizType: QuizTypes) {
        val list = when (quizType) {
            QuizTypes.FullQuiz -> getFullQuizList(dictionaryId)
            QuizTypes.QuickQuiz -> getQuickQuizList(dictionaryId)
            QuizTypes.WeakestQuiz -> getWeakestFive(dictionaryId)
            QuizTypes.CustomQuiz -> getCustomQuizList(dictionaryId)
        }
        _quizList.value = list
    }

    private suspend fun getFullQuizList(dictionaryId: Long): List<Word> {
        return wordRepository.getObservableWordList(dictionaryId)
            .asFlow()
            .first()
            .filter { it.word.isNotEmpty() }
    }

    private suspend fun getQuickQuizList(dictionaryId: Long): List<Word> {
       return getFullQuizList(dictionaryId)
            .shuffled()
            .take(Constants.QUICK_QUIZ_SIZE)
    }
    @VisibleForTesting
    private suspend fun getWeakestFive(dictionaryId: Long): List<Word> {
        val fullList = getFullQuizList(dictionaryId)
       return sortWeaknessesList(fullList).take(Constants.WEAKEST_QUIZ_SIZE)
    }

    private suspend fun getCustomQuizList(dictionaryId: Long): List<Word> {
        val fullList = getFullQuizList(dictionaryId)
        return if(customQuizRepository.quizSize > fullList.size) {
            fullList.shuffled()
        } else {
            fullList.shuffled().take(customQuizRepository.quizSize)
        }
    }

    override fun updateQuizList(list: List<Word>) {
        _quizList.value = list
    }

    /*
    Sort by failed / beenAsked ratio. If it's the same then take the one where beenAsked is more
     */
    private fun sortWeaknessesList(list: List<Word>): List<Word> {
        return list.map {
            when {
                it.beenAsked != 0 -> it to (it.failed.toFloat() / it.beenAsked.toFloat())
                else -> it to 0f
            }
        }.sortedWith(compareBy({ (_, value) -> value }, { (key, _) -> key.beenAsked })).toMap().keys.reversed()
    }
}