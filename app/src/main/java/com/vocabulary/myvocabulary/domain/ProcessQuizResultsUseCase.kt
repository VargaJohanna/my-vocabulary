package com.vocabulary.myvocabulary.domain

import com.vocabulary.myvocabulary.DispatcherProvider
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.text.Normalizer
import kotlin.math.round


data class QuizResult(
    val processedWords: List<Word>,
    val percentage: Int,
    val numberOfPassed: Int,
    val allPassed: Boolean
)

class ProcessQuizResultsUseCase(
    private val wordRepository: WordRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(dictionaryId: Long, resultsMap: Map<Long, String>, quizDirection: Int): QuizResult = withContext(dispatchers.io) {
        val processedList = resultsMap.entries.map { entry ->
            val word = wordRepository.getWordById(entry.key).await()
            val updatedWord = evaluate(word, entry.value, quizDirection)
            wordRepository.updateWord(updatedWord)
            updatedWord
        }

        val percentage = if (processedList.isNotEmpty()) {
            round(((processedList.filter { it.lastResult }.size.toFloat() / processedList.size.toFloat()) * 100)).toInt()
        } else 0

        dictionaryRepository.saveQuizStats(dictionaryId, percentage)
        dictionaryRepository.onQuizFinished(dictionaryId)

        QuizResult(
            processedWords = processedList,
            percentage = percentage,
            numberOfPassed = processedList.count { it.lastResult },
            allPassed = processedList.all { it.lastResult }
        )

    }

    private fun evaluate(word: Word, guess: String, quizDirection: Int): Word {
        val isCorrect = if (quizDirection.toDirectionType() == QuizDirectionType.AskWord) {
            word.translation.normalize().equals(guess.normalize(), ignoreCase = true)
        } else {
            word.word.normalize().equals(guess.normalize(), ignoreCase = true)
        }

        return if (isCorrect) {
            word.copy(
                lastResult = true,
                lastGuess = guess,
                beenAsked = word.beenAsked + 1,
                passed = word.passed + 1
            )
        } else {
            word.copy(
                lastResult = false,
                lastGuess = guess,
                beenAsked = word.beenAsked + 1,
                failed = word.failed + 1
            )
        }
    }

    private fun String.normalize(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFC).trim()
    }
}