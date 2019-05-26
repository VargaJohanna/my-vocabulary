package com.vocabulary.myvocabulary.ui.dictionaries

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.ext.plusAssign
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.rx.RxSchedulers
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.Event
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class ShareDictionaryViewModel(
        private val dictionaryRepository: DictionaryRepository,
        private val rxSchedulers: RxSchedulers,
        private val wordRepository: WordRepository,
        private val shareDictionaryRepository: ShareDictionaryRepository
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val importedDictionaryDetails: MutableLiveData<Event<DictionaryDetails>> = MutableLiveData()
    private val isImport: MutableLiveData<Boolean> = MutableLiveData()

    init {
        observeIsImport()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun createDictionary(dictionary: Dictionary) {
        disposables += Single.fromCallable { dictionaryRepository.createDictionary(dictionary) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe { t: Long ->
                    importedDictionaryDetails.postValue(Event(DictionaryDetails(t, dictionary.dictionaryName)))
                }
    }

    fun fetchCsvUri(): Uri? = shareDictionaryRepository.getCsvData()

    fun parseDataAndCreateWords(dictionaryId: Long, context: Context) {
        if(shareDictionaryRepository.getCsvData() != null) {
            val scheme = shareDictionaryRepository.getCsvData()!!.scheme
            if (ContentResolver.SCHEME_CONTENT == scheme) {
                try {
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(shareDictionaryRepository.getCsvData()!!)

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val csvParser = CSVParser(reader, CSVFormat.DEFAULT)

                    for (csvRecord in csvParser) {
                        insertWordToDatabase(Word(
                                containerDictionaryId = dictionaryId,
                                word = csvRecord.get(0),
                                translation = csvRecord.get(1),
                                created = Calendar.getInstance().time
                        ))
                    }
                } catch (ex: Exception) {
                    Log.d("DEBUG", ex.message)
                }
            }

        }

    }

    private fun insertWordToDatabase(word: Word) {
        disposables += Completable.fromCallable { wordRepository.createWord(word) }
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe()
    }

    private fun observeIsImport() {
        disposables += shareDictionaryRepository.getImport()
                .subscribeOn(rxSchedulers.io())
                .observeOn(rxSchedulers.main())
                .subscribe{ isImport.postValue(it) }
    }

    fun setIsImport(isImport: Boolean) {
        shareDictionaryRepository.setIsImport(isImport)
    }

    fun saveCsvData(csv: Uri) {
        shareDictionaryRepository.storeCsvData(csv)
    }

    fun getImportedDictionaryDetails(): LiveData<Event<DictionaryDetails>> = importedDictionaryDetails
    fun getLiveIsImport(): LiveData<Boolean> = isImport
}