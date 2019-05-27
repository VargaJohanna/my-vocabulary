package com.vocabulary.myvocabulary.ui.dictionaries

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vocabulary.myvocabulary.R
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
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
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
                .subscribe { dictionaryId: Long ->
                    importedDictionaryDetails.postValue(Event(DictionaryDetails(dictionaryId, dictionary.dictionaryName)))
                }
    }

    fun fetchCsvUri(): Uri? = shareDictionaryRepository.getCsvUri()

    fun parseDataAndCreateWords(dictionaryId: Long, context: Context) {
        shareDictionaryRepository.getCsvUri()?.let {
            val scheme = it.scheme
            if (ContentResolver.SCHEME_CONTENT == scheme) {
                try {
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(it)

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val csvParser = CSVParser(reader, CSVFormat.DEFAULT)

                    for (csvRecord in csvParser) {
                        insertWordToDatabase(Word(
                                containerDictionaryId = dictionaryId,
                                translation = csvRecord.get(0),
                                word = csvRecord.get(1),
                                created = Calendar.getInstance().time
                        ))
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
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
                .subscribe { isImport.postValue(it) }
    }

    fun setIsImport(isImport: Boolean) {
        shareDictionaryRepository.setIsImport(isImport)
    }

    fun saveCsvData(csv: Uri) {
        shareDictionaryRepository.storeCsvData(csv)
    }

    private fun writeCsvFile(words: List<Word>, context: Context): File {
        val file = File("${context.filesDir.path}/export_dictionary.csv")
        try {
            file.createNewFile()
            val fileWriter = FileWriter(file)
            val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)

            for (word in words) {
                val data = Arrays.asList(
                        word.translation,
                        word.word
                )
                csvPrinter.printRecord(data)
            }

            fileWriter.flush()
            fileWriter.close()
            csvPrinter.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.message)
        }
        return file
    }

    fun shareDictionary(words: List<Word>, context: Context) {
        val file = writeCsvFile(words, context)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/csv"
        intent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".fileprovider",
                        file)
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(context, Intent.createChooser(intent, context.getString(R.string.share_file_title)), null)
    }

    fun getImportedDictionaryDetails(): LiveData<Event<DictionaryDetails>> = importedDictionaryDetails
    fun getLiveIsImport(): LiveData<Boolean> = isImport

}