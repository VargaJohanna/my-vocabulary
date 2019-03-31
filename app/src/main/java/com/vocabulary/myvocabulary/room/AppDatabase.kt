package com.vocabulary.myvocabulary.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryDao
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import com.vocabulary.myvocabulary.room.wordData.WordDao
import com.vocabulary.myvocabulary.room.wordData.WordEntry
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.*

@Database(entities = [DictionaryEntry::class, WordEntry::class], version = 4, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun wordDao(): WordDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "appdatabase.db")
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                ioThread {
                                    getInstance(context).dictionaryDao().insertDictionary(defaultDictionary)
                                    getInstance(context).wordDao().insertWord(defaultWord)
                                }
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build()

        val defaultDictionary = Dictionary(dictionaryId = 1L, dictionaryName = "Example Dictionary", dictionaryCreated = Calendar.getInstance().time).toDictionaryEntry()
        val defaultWord = Word(1, 1, "new", "novus", 0, 0, 0, Calendar.getInstance().time).toWordEntry()
    }
}
