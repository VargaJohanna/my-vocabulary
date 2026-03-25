package com.vocabulary.myvocabulary.ui.dictionaries

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryRepository
import com.vocabulary.myvocabulary.repositories.share.ShareDictionaryRepository
import com.vocabulary.myvocabulary.repositories.word.WordRepository
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val wordRepository: WordRepository,
    private val shareDictionaryRepository: ShareDictionaryRepository
) : ViewModel() {
    private val _importedDictionaryDetailsFlow: MutableSharedFlow<Event<DictionaryDetails>> =
        MutableSharedFlow<Event<DictionaryDetails>>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    val importedDictionaryDetailsFlow: SharedFlow<Event<DictionaryDetails>> =
        _importedDictionaryDetailsFlow

    fun parseDataAndCreateWordsCompose(dictionaryId: Long, contentResolver: ContentResolver) {
        val uri = shareDictionaryRepository.getCsvUri() ?: return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                        val csvParser = CSVParser(reader, CSVFormat.DEFAULT)

                        for (csvRecord in csvParser) {
                            if (csvRecord.size() >= 2) {
                                insertWordToDatabase(
                                    Word(
                                        containerDictionaryId = dictionaryId,
                                        translation = csvRecord.get(0),
                                        word = csvRecord.get(1),
                                        created = Calendar.getInstance().time
                                    )
                                )
                            }
                        }
                    }
                }
                Log.d("IMPORT", "CSV imported successfully")
            } catch (e: Exception) {
                Log.e("IMPORT", "Error parsing CSV", e)
            }
        }
    }

    fun createDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    dictionaryRepository.createDictionary(dictionary)
                }
                val details = DictionaryDetails(id, dictionary.dictionaryName)
                _importedDictionaryDetailsFlow.tryEmit(Event(details))
            } catch (e: Exception) {
                Log.e("CREATE_DICT", "Failed to create dictionary", e)
            }

        }
    }

    private suspend fun insertWordToDatabase(word: Word) {
        withContext(Dispatchers.IO) {
            wordRepository.createWord(word)
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

    fun shareDictionaryCompose(words: List<Word>, context: Context, dictionaryName: String) {
        viewModelScope.launch {
            try {
                val intent = withContext(Dispatchers.IO) {
                    val file = writeCsvFile(words, context, dictionaryName)
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                val chooser =
                    Intent.createChooser(intent, context.getString(R.string.share_file_title))
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                Log.d("SHARE", "Sharing started successfully")
            } catch (e: Exception) {
                Log.e("SHARE", "Failed to share dictionary", e)
            }
        }
    }
}