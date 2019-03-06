package com.vocabulary.myvocabulary.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryDao
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry
import com.vocabulary.myvocabulary.room.wordData.WordDao
import com.vocabulary.myvocabulary.room.wordData.WordEntry
import com.vocabulary.myvocabulary.utils.DateTypeConverter

@Database(entities = arrayOf(DictionaryEntry::class, WordEntry::class), version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun wordDao(): WordDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext,
                            AppDatabase::class.java, "appdatabase.db")
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return instance
        }
    }
}
