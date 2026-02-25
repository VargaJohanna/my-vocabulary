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
import com.vocabulary.myvocabulary.repositories.word.WordDao
import com.vocabulary.myvocabulary.repositories.word.WordEntry
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.dictionaries.toDictionaryEntry
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.toWordEntry
import com.vocabulary.myvocabulary.utils.DateTypeConverter
import java.util.*

@Database(entities = [DictionaryEntry::class, WordEntry::class], version = 5, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun wordDao(): WordDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // We add the column as NULLABLE so existing rows just get a 'null' value
                db.execSQL("ALTER TABLE dictionaries ADD COLUMN dictionary_last_practiced INTEGER")
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
                .addMigrations(MIGRATION_4_5)
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
                            dictionaryLastPracticed = null
                        ).toDictionaryEntry()

                    private fun getListOfDefaultWords(context: Context): List<WordEntry> {
                        return listOf(
                            Word(
                                1,
                                1,
                                context.getString(R.string.example_word_new),
                                context.getString(R.string.example_translation_novus),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                2,
                                1,
                                context.getString(R.string.example_word_body),
                                context.getString(R.string.example_translation_corpus),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                3,
                                1,
                                context.getString(R.string.example_word_day),
                                context.getString(R.string.example_translation_diem),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                4,
                                1,
                                context.getString(R.string.example_word_king),
                                context.getString(R.string.example_translation_rex),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                5,
                                1,
                                context.getString(R.string.example_word_god),
                                context.getString(R.string.example_translation_deus),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                6,
                                1,
                                context.getString(R.string.example_word_and),
                                context.getString(R.string.example_translation_et),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                7,
                                1,
                                context.getString(R.string.example_word_life),
                                context.getString(R.string.example_translation_vita),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                8,
                                1,
                                context.getString(R.string.example_word_peace),
                                context.getString(R.string.example_translation_pax),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                9,
                                1,
                                context.getString(R.string.example_word_house),
                                context.getString(R.string.example_translation_domo),
                                0,
                                0,
                                0,
                                Calendar.getInstance().time
                            ).toWordEntry(),
                            Word(
                                10,
                                1,
                                context.getString(R.string.example_word_sea),
                                context.getString(R.string.example_translation_mare),
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
