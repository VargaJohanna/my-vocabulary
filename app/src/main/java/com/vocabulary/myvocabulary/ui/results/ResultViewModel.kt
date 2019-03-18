package com.vocabulary.myvocabulary.ui.results

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.room.wordData.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.quizzes.QuizDirectionType
import com.vocabulary.myvocabulary.ui.quizzes.toDirectionType
import com.vocabulary.myvocabulary.ui.words.Word
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class ResultViewModel(
        val directionType: Int,
        val dictionaryId: Long,
        private val wordRepository: WordRepository,
        private val rxSchedulers: RxSchedulers
) : ViewModel() {
    private val disposables = CompositeDisposable()
    var guessedWordMap: MutableMap<Long, String> = emptyMap<Long, String>().toMutableMap()
    private val liveWordList: MutableLiveData<List<Word>> = MutableLiveData()
    private lateinit var currentWord: Word

    init {
        observeList()
        updateAllWords()
    }
    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    private fun updateWord(word: Word) {
        disposables += Completable.fromCallable {
            wordRepository.updateWord(word)
        }.subscribeOn(rxSchedulers.io())
                .subscribe()
    }

    private fun updateAllWords() {
        for((k, _) in guessedWordMap) {
            getWordById(k)
            updateWord(currentWord.copy(lastResult = evaluateGuess(k)))
        }
    }

    private fun observeList() {
        disposables += wordRepository.getObservableWordList(dictionaryId)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { liveWordList.postValue(it) }
    }

    private fun getWordById(id: Long){
        disposables += wordRepository.getWordById(id)
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .map {
                    currentWord = it
                }
                .subscribe()
    }

    private fun evaluateGuess(wordId: Long) : Boolean {
        getWordById(wordId)
        return if(directionType.toDirectionType() == QuizDirectionType.AskMeaning) {
            currentWord.translation == guessedWordMap[wordId]
        } else {
            currentWord.word == guessedWordMap[wordId]
        }
    }

}