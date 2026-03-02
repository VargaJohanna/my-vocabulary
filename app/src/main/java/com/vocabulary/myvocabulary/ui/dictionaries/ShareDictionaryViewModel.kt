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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
    private val _importedDictionaryDetailsFlow: MutableSharedFlow<Event<DictionaryDetails>> = MutableSharedFlow<Event<DictionaryDetails>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val importedDictionaryDetailsFlow: SharedFlow<Event<DictionaryDetails>> = _importedDictionaryDetailsFlow
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
                    _importedDictionaryDetailsFlow.tryEmit(Event(DictionaryDetails(dictionaryId, dictionary.dictionaryName)))
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

                    val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
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
    fun parseDataAndCreateWordsCompose(dictionaryId: Long, contentResolver: ContentResolver) {
        val uri = shareDictionaryRepository.getCsvUri() ?: return
        disposables += Completable.fromAction {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val csvParser = CSVParser(reader, CSVFormat.DEFAULT)

                for (csvRecord in csvParser) {
                    if (csvRecord.size() >= 2) {
                        insertWordToDatabase(Word(
                            containerDictionaryId = dictionaryId,
                            translation = csvRecord.get(0),
                            word = csvRecord.get(1),
                            created = Calendar.getInstance().time
                        ))
                    }
                }
            }
        }
            .subscribeOn(rxSchedulers.io())
            .observeOn(rxSchedulers.main())
            .subscribe(
                { Log.d("IMPORT", "CSV imported successfully") },
                { error -> Log.e("IMPORT", "Error parsing CSV", error) }
            )
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
                .subscribe {
                    isImport.postValue(it)
                }
    }

    fun setIsImport(isImport: Boolean) {
        shareDictionaryRepository.setIsImport(isImport)
    }

    fun saveCsvData(csv: Uri) {
        shareDictionaryRepository.storeCsvData(csv)
    }

    private fun writeCsvFile(words: List<Word>, context: Context, dictionaryName: String): File {
        val safeName = dictionaryName.replace("\\s+".toRegex(), "_")
        val file = File(context.cacheDir, "${safeName}_export.csv")
        try {
            FileWriter(file).use { fileWriter ->
                CSVPrinter(fileWriter, CSVFormat.DEFAULT).use { csvPrinter ->
                    for (word in words) {
                        csvPrinter.printRecord(word.translation, word.word)
                    }
                    fileWriter.flush()
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Write CSV failed: ${e.message}")
        }
        return file
    }

    fun shareDictionary(words: List<Word>, context: Context, name: String?) {
        val file = writeCsvFile(words, context, name!!)
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
    fun shareDictionaryCompose(words: List<Word>, context: Context, dictionaryName: String) {
        disposables += Completable.fromAction {
            val file = writeCsvFile(words, context, dictionaryName)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, context.getString(R.string.share_file_title))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
            .subscribeOn(rxSchedulers.io())
            .observeOn(rxSchedulers.main())
            .subscribe(
                { Log.d("SHARE", "Sharing started successfully") },
                { error -> Log.e("SHARE", "Failed to share dictionary", error) }
            )
    }

    fun getImportedDictionaryDetails(): LiveData<Event<DictionaryDetails>> = importedDictionaryDetails
    fun getLiveIsImport(): LiveData<Boolean> = isImport

}