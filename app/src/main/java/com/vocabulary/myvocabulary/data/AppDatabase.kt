package com.vocabulary.myvocabulary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vocabulary.myvocabulary.data.dictionaryDatabase.DictionaryDao
import com.vocabulary.myvocabulary.data.dictionaryDatabase.DictionaryEntry
import com.vocabulary.myvocabulary.data.wordDatabase.WordDao
import com.vocabulary.myvocabulary.data.wordDatabase.WordEntry

@Database(entities = arrayOf(DictionaryEntry::class, WordEntry::class), version = 1)
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
