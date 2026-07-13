package com.vocabulary.myvocabulary.repositories

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryDao
import com.vocabulary.myvocabulary.repositories.dictionary.DictionaryEntry
import com.vocabulary.myvocabulary.repositories.quotes.QuoteDao
import com.vocabulary.myvocabulary.repositories.quotes.QuoteEntry
import com.vocabulary.myvocabulary.repositories.word.WordDao
import com.vocabulary.myvocabulary.repositories.word.WordEntry
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.*

@Database(entities = [DictionaryEntry::class, WordEntry::class, QuoteEntry::class], version = 8, exportSchema = true)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun wordDao(): WordDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        // Only support the real-world production path: 4 -> 7

        val MIGRATION_4_7 = object : Migration(4, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dictionaries ADD COLUMN dictionary_total_score INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE dictionaries ADD COLUMN dictionary_last_practiced INTEGER")
                db.execSQL("ALTER TABLE dictionaries ADD COLUMN dictionary_last_result INTEGER")
                db.execSQL("ALTER TABLE dictionaries ADD COLUMN dictionary_finished_count INTEGER NOT NULL DEFAULT 0")
            }
        }

        // NEW: Create the quotes table
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `quotes` (
                        `id` INTEGER PRIMARY KEY NOT NULL, 
                        `quote` TEXT NOT NULL, 
                        `author` TEXT NOT NULL, 
                        `work` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "appdatabase.db"
            )
                .addMigrations(MIGRATION_4_7, MIGRATION_7_8)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ioThread {
                            getInstance(context).dictionaryDao()
                                .insertDictionary(getDefaultDictionary(context))
                            getListOfDefaultWords(context).forEach {
                                getInstance(context).wordDao().insertWord(it)
                            }
                        }
                    }

                    private fun getDefaultDictionary(context: Context): DictionaryEntry =
                        Dictionary(
                            dictionaryId = 1L,
                            dictionaryName = context.getString(R.string.example_dictionary_title),
                            dictionaryCreated = Calendar.getInstance().time,
                            dictionaryLastPracticed = null,
                            dictionaryLastResult = null,
                            dictionaryFinishedCount = 0,
                            dictionaryTotalScore = 0
                        ).toDictionaryEntry()

                    private fun getListOfDefaultWords(context: Context): List<WordEntry> {
                        return listOf(
                            Word(
                                1,
                                1,
                                context.getString(R.string.example_word_hello),
                                context.getString(R.string.example_translation_hello),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                2,
                                1,
                                context.getString(R.string.example_word_thank_you),
                                context.getString(R.string.example_translation_thank_you),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                3,
                                1,
                                context.getString(R.string.example_word_friend),
                                context.getString(R.string.example_translation_friend),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                4,
                                1,
                                context.getString(R.string.example_word_family),
                                context.getString(R.string.example_translation_family),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                5,
                                1,
                                context.getString(R.string.example_word_water),
                                context.getString(R.string.example_translation_water),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                6,
                                1,
                                context.getString(R.string.example_word_coffee),
                                context.getString(R.string.example_translation_coffee),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                7,
                                1,
                                context.getString(R.string.example_word_money),
                                context.getString(R.string.example_translation_money),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                8,
                                1,
                                context.getString(R.string.example_word_travel),
                                context.getString(R.string.example_translation_travel),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                9,
                                1,
                                context.getString(R.string.example_word_home),
                                context.getString(R.string.example_translation_home),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                10,
                                1,
                                context.getString(R.string.example_word_school),
                                context.getString(R.string.example_translation_school),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry()
                        )
                    }
                })
                .fallbackToDestructiveMigrationOnDowngrade()
                        .build()
    }
}
